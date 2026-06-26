import json
from typing import List, Optional

from models import Source, VerificationResponse
from llm_client import LLMClient
from search_engine import SearchEngine
from ranker import rank_sources


class VerificationPipeline:
    """Orchestrates the end-to-end verification process."""

    def __init__(self, llm: LLMClient, search: SearchEngine):
        self.llm = llm
        self.search = search

    async def verify(self, text: str) -> VerificationResponse:
        # 1. Detect claim
        claim = self._detect_claim(text)

        # 2. Search for evidence
        sources = await self._retrieve_evidence(claim)

        # 3. LLM verdict
        verdict = self._llm_verdict(claim, sources)

        return verdict

    def _detect_claim(self, text: str) -> str:
        from claim_detector import extract_claim
        return extract_claim(text) or text[:500]

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
        # Try direct claim, then debunk-oriented queries
        queries = [claim]
        if any(w in claim_lower for w in ["true", "real", "fact", "hoax", "fake", "misleading"]):
            queries.append(f"fact check {claim}")
        queries.append(f"debunked {claim}")
        queries.append(f"evidence {claim}")
        return queries[:4]

    def _llm_verdict(self, claim: str, sources: List[Source]) -> VerificationResponse:
        if not self.llm.available:
            # Fallback: no LLM available — basic keyword-based response
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
            # Parse JSON from response
            response = response.strip()
            if response.startswith("```"):
                response = response.split("```")[1]
                if response.startswith("json"):
                    response = response[4:]
            data = json.loads(response)
            return VerificationResponse(
                claim=data.get("claim", claim),
                verdict=data.get("verdict", "Unknown"),
                confidence=min(data.get("confidence", 0.5), 0.95),
                explanation=data.get("explanation", "No explanation provided."),
                sources=sources if sources else [],
            )
        except Exception:
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
