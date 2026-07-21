from __future__ import annotations

from typing import List

from anti_timpa.config import CFG
from anti_timpa.models import Source

__all__ = ["rank_sources"]


def rank_sources(sources: List[Source], claim: str) -> List[Source]:
    if not sources:
        return sources

    def _score(s: Source) -> float:
        s_ = 0.0
        for d in CFG.trusted_domains:
            if d in s.url:
                s_ += 3.0
                break
        if ".gov" in s.url:
            s_ += 2.0
        elif ".edu" in s.url:
            s_ += 1.5
        elif ".org" in s.url:
            s_ += 0.5
        overlap = len(set(claim.lower().split()) & set(s.title.lower().split()))
        s_ += overlap * 0.5
        return s_

    return sorted(sources, key=_score, reverse=True)
