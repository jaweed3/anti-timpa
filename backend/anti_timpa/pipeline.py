from __future__ import annotations

import json
import re
import logging
from typing import List

from anti_timpa.llm import TextLLM
from anti_timpa.search import Searcher
from anti_timpa.models import Source, VerificationResponse
from anti_timpa.ranker import rank_sources, label_source
from anti_timpa.config import CFG
from anti_timpa.claim import extract_claim

__all__ = ["VerificationPipeline"]

logger = logging.getLogger("antitimpa.pipeline")

_SYSTEM_PROMPT = """You are AntiTimpa, an AI scam-detection and fact-verification assistant for Indonesia.

CONTEXT: The text below was extracted via OCR from a user's device.
OCR may introduce typos or garbled characters. Focus on the underlying
factual claim, not surface-level OCR artifacts.

Your task:
1. Extract the core factual claim(s) from the text.
2. Evaluate each claim against the web evidence provided below.
3. Return a JSON verdict.

Output ONLY valid JSON with these fields:
- claim: the extracted claim
- verdict: one of ["Supported", "Contradicted", "Misleading", "Mixed", "Insufficient Evidence"]
- confidence: float 0.0-1.0
- explanation: concise 2-4 sentence explanation in Bahasa Indonesia
- sources: array of {title, url, snippet} from the evidence used

Rules:
- Max confidence is 0.95. Never 100%.
- If the evidence clearly and directly supports the claim → "Supported" with high confidence.
- If authoritative evidence contradicts the claim → "Contradicted" with high confidence.
- If evidence partially supports or context is cherry-picked → "Misleading".
- If evidence is evenly split → "Mixed".
- If no credible evidence supports or refutes → "Insufficient Evidence".
  Do NOT default to "Supported" just because vaguely related pages exist.
  Do NOT default to "Contradicted" just because the text uses strong language.
  Ordinary news reporting often uses direct language — that is not a sign of fabrication.
- Prioritize [TRUSTED] sources (major news outlets, academic, government) over [UNKNOWN] sources.
  When TRUSTED sources directly confirm the claim, that is strong evidence for "Supported".
- Prioritize authoritative Indonesian sources: go.id, Kominfo, Turnbackhoax.id, Kompas, Tempo, BBC Indonesia.
"""


class VerificationPipeline:
    def __init__(self, llm: TextLLM, search: Searcher) -> None:
        self._llm = llm
        self._search = search

    async def verify(self, text: str) -> VerificationResponse:
        claim = extract_claim(text) or text[:500]
        logger.info("Claim: %s", claim[:80])

        sources = await self._retrieve(claim)
        logger.info("Evidence: %d sources", len(sources))

        result = self._verdict(claim, sources)
        logger.info("Verdict: %s (%.2f)", result.verdict, result.confidence)
        return result

    async def _retrieve(self, claim: str) -> List[Source]:
        queries = _gen_queries(claim)
        all_sources: list[Source] = []
        for q in queries:
            sources = await self._search.search(q, max_results=5)
            all_sources.extend(sources)
            if len(all_sources) >= 5:
                break
        return rank_sources(all_sources, claim)

    def _verdict(self, claim: str, sources: List[Source]) -> VerificationResponse:
        if not self._llm.available:
            logger.warning("LLM unavailable, fallback")
            return _fallback(claim, sources)

        trusted = [s for s in sources if label_source(s) in ("TRUSTED", "FACT-CHECK")]
        labels = {id(s): label_source(s) for s in sources}
        evidence = "\n\n".join(
            f"Source {i+1} [{labels[id(s)]}]: {s.title}\nURL: {s.url}\nContent: {s.snippet}"
            for i, s in enumerate(sources)
        )
        prompt = (
            f"Claim text (extracted via OCR, may contain typos):\n{claim}\n\n"
            f"Web search evidence:\n{evidence}\n\n"
            f"Evaluate whether the claim is supported or contradicted by the evidence. "
            f"If TRUSTED sources directly confirm the claim, return Supported. "
            f"Return the verdict as JSON."
        )

        try:
            raw = self._llm.chat(_SYSTEM_PROMPT, prompt)
            result = _parse_llm_response(raw, claim, sources)
            if result.verdict in ("Supported",) and trusted:
                result.confidence = min(result.confidence + 0.1, 0.95)
            return result
        except Exception as e:
            logger.exception("LLM verdict failed: %s", e)
            return _fallback(claim, sources)


def _gen_queries(claim: str) -> list[str]:
    cl = claim.lower()
    flags = {"true", "real", "fact", "hoax", "fake", "misleading"}
    queries = [claim]
    if any(w in cl for w in flags):
        queries.append(f"fact check {claim}")

    for domain in CFG.fact_check_domains:
        queries.append(f"site:{domain} {claim}")

    for domain in CFG.trusted_domains[:3]:
        queries.append(f"site:{domain} {claim[:100]}")

    return queries[:6]


def _parse_llm_response(raw: str, claim: str, sources: List[Source]) -> VerificationResponse:
    js = raw.strip()
    m = re.search(r'\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', js, re.DOTALL)
    if m:
        js = m.group(0)
    if js.startswith("```"):
        js = js.split("```")[1]
        js = js.removeprefix("json").strip()
    data = json.loads(js)
    return VerificationResponse(
        claim=data.get("claim", claim),
        verdict=data.get("verdict", "Unknown"),
        confidence=min(data.get("confidence", 0.5), 0.95),
        explanation=data.get("explanation", "No explanation provided."),
        sources=sources or [],
    )


def _fallback(claim: str, sources: List[Source]) -> VerificationResponse:
    if not sources:
        return VerificationResponse(claim=claim, verdict="Insufficient Evidence", confidence=0.0, explanation="No evidence sources could be retrieved.", sources=[])
    return VerificationResponse(claim=claim, verdict="Insufficient Evidence", confidence=0.3, explanation=f"Found {len(sources)} sources. Configure LLM API key for full analysis.", sources=sources)
