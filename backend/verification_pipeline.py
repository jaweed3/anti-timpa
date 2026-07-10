"""
Verification pipeline: claim detection → search → LLM verdict.
"""

import json
import re
import logging
from typing import List
from functools import lru_cache
from hashlib import sha256

from models import Source, VerificationResponse
from llm_client import LLMClient
from search_engine import SearchEngine
from ranker import rank_sources
from claim_detector import extract_claim

logger = logging.getLogger("factlens.pipeline")


class VerificationPipeline:
    """Orchestrates the end-to-end verification process."""

    def __init__(self, llm: LLMClient, search: SearchEngine):
        self.llm = llm
        self.search = search

    async def verify(self, text: str) -> VerificationResponse:
        # 1. Detect claim
        claim = extract_claim(text) or text[:500]
        logger.info("Claim detected: %s", claim[:80])

        # 2. Search for evidence
        sources = await self._retrieve_evidence(claim)
        logger.info("Evidence retrieved: %d sources", len(sources))

        # 3. LLM verdict
        verdict = self._llm_verdict(claim, sources)
        logger.info("Verdict: %s (confidence: %.2f)", verdict.verdict, verdict.confidence)

        return verdict

    async def _retrieve_evidence(self, claim: str) -> List[Source]:
        # Generate search queries
        queries = self._generate_queries(claim)
        all_sources = []
        for q in queries:
            sources = await self.search.search(q, max_results=5)
            all_sources.extend(sources)
            if len(all_sources) >= 5:
                break

        return rank_sources(all_sources, claim)

    def _generate_queries(self, claim: str) -> List[str]:
        claim_lower = claim.lower()
        queries = [claim]
        if any(w in claim_lower for w in ["true", "real", "fact", "hoax", "fake", "misleading"]):
            queries.append(f"fact check {claim}")
        queries.append(f"debunked {claim}")
        queries.append(f"evidence {claim}")
        return queries[:4]

    def _llm_verdict(self, claim: str, sources: List[Source]) -> VerificationResponse:
        if not self.llm.available:
            logger.warning("LLM not available, using fallback verdict")
            return self._fallback_verdict(claim, sources)

        system_prompt = """You are FactLens, an AI fact-verification assistant.

Your task is to evaluate a claim against provided evidence sources and return a JSON verdict.

Output ONLY valid JSON with these fields:
- claim: the extracted claim
- verdict: one of ["Supported", "Contradicted", "Misleading", "Mixed", "Insufficient Evidence"]
- confidence: float between 0.0 and 1.0
- explanation: concise 2-4 sentence explanation
- sources: array of {title, url, snippet} from the evidence

Rules:
- Never claim 100% certainty. Max confidence is 0.95.
- If evidence contradicts, use "Contradicted" with high confidence.
- If evidence partially supports or cherry-picks, use "Misleading".
- If evidence is mixed, use "Mixed".
- If insufficient evidence exists, use "Insufficient Evidence".
- Always prioritize authoritative sources (gov, edu, major news, scientific).
"""

        evidence_text = "\n\n".join(
            f"Source {i+1}: {s.title}\nURL: {s.url}\nContent: {s.snippet}"
            for i, s in enumerate(sources)
        )

        user_prompt = f"""Claim: {claim}

Evidence:
{evidence_text}

Return the verdict as JSON."""

        try:
            response = self.llm.chat(system_prompt, user_prompt)
            logger.debug("LLM response: %s", response[:200])

            # Extract JSON from response (handle markdown fences, preamble text, etc.)
            json_str = response.strip()

            # Try to find JSON object in the response
            json_match = re.search(r'\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', json_str, re.DOTALL)
            if json_match:
                json_str = json_match.group(0)

            # Also handle markdown code blocks
            if json_str.startswith("```"):
                json_str = json_str.split("```")[1]
                if json_str.startswith("json"):
                    json_str = json_str[4:]
                json_str = json_str.strip()

            data = json.loads(json_str)
            return VerificationResponse(
                claim=data.get("claim", claim),
                verdict=data.get("verdict", "Unknown"),
                confidence=min(data.get("confidence", 0.5), 0.95),
                explanation=data.get("explanation", "No explanation provided."),
                sources=sources if sources else [],
            )
        except json.JSONDecodeError as e:
            logger.error("Failed to parse LLM JSON response: %s", e)
            return self._fallback_verdict(claim, sources)
        except Exception as e:
            logger.exception("LLM verdict failed: %s", e)
            return self._fallback_verdict(claim, sources)

    def _fallback_verdict(self, claim: str, sources: List[Source]) -> VerificationResponse:
        """Basic fallback when LLM is unavailable."""
        if not sources:
            return VerificationResponse(
                claim=claim,
                verdict="Insufficient Evidence",
                confidence=0.0,
                explanation="No evidence sources could be retrieved. Try searching manually.",
                sources=[],
            )

        return VerificationResponse(
            claim=claim,
            verdict="Insufficient Evidence",
            confidence=0.3,
            explanation=f"Found {len(sources)} potential sources. Configure an LLM API key for full AI-powered analysis.",
            sources=sources,
        )
