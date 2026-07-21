from __future__ import annotations

import re

__all__ = ["extract_claim", "is_claim"]

_OPINION = (
    r'\bi think\b', r'\bfeel\b', r'\bbelieve\b',
    r'\bimo\b', r'\bimho\b', r'\bin my opinion\b',
    r'\bmaybe\b', r'\bprobably\b',
)


def extract_claim(text: str) -> str | None:
    if not text or len(text.strip()) < 5:
        return None
    t = text.strip()
    t = re.sub(r'https?://\S+', '', t)
    t = re.sub(r'[@#]\w+', '', t)
    t = re.sub(r'\s+', ' ', t).strip()
    return t if len(t) >= 10 else None


def is_claim(text: str) -> bool:
    for p in _OPINION:
        if re.search(p, text, re.IGNORECASE):
            return False
    return len(text.split()) >= 3
