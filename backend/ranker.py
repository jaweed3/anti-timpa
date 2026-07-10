"""
Source ranking by relevance and authority.
"""

import logging
from typing import List

import config
from models import Source

logger = logging.getLogger("factlens.ranker")


def rank_sources(sources: List[Source], claim: str) -> List[Source]:
    """Rank sources by relevance and authority."""
    if not sources:
        return sources

    def score(source: Source) -> float:
        s = 0.0

        # Domain authority
        for d in config.TRUSTED_DOMAINS:
            if d in source.url:
                s += 3.0
                break

        # .gov / .edu / .org
        if ".gov" in source.url:
            s += 2.0
        elif ".edu" in source.url:
            s += 1.5
        elif ".org" in source.url:
            s += 0.5

        # Keyword overlap with claim
        claim_words = set(claim.lower().split())
        title_words = set(source.title.lower().split())
        overlap = len(claim_words & title_words)
        s += overlap * 0.5

        return s

    ranked = sorted(sources, key=score, reverse=True)
    logger.info("Ranked %d sources, top: %s", len(ranked), ranked[0].title[:50] if ranked else "none")
    return ranked
