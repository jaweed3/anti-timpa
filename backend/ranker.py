from typing import List

from models import Source


def rank_sources(sources: List[Source], claim: str) -> List[Source]:
    """Rank sources by relevance and authority."""
    if not sources:
        return sources

    TRUSTED_DOMAINS = [
        "reuters.com", "apnews.com", "bbc.com", "bbc.co.uk",
        "nytimes.com", "theguardian.com", "washingtonpost.com",
        "nature.com", "science.org", "who.int", "cdc.gov",
        "un.org", "scholar.google.com", "pubmed.ncbi.nlm.nih.gov",
    ]

    def score(source: Source) -> float:
        s = 0.0

        # Domain authority
        for d in TRUSTED_DOMAINS:
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

    return sorted(sources, key=score, reverse=True)
