"""
Claim detection and extraction from OCR text.
"""

import logging
import re
from typing import Optional

logger = logging.getLogger("factlens.claim")


def extract_claim(text: str) -> Optional[str]:
    """Extract the most likely factual claim from OCR text."""
    if not text or len(text.strip()) < 5:
        logger.debug("Text too short for claim extraction")
        return None

    text = text.strip()

    # Remove common noise patterns
    text = re.sub(r'https?://\S+', '', text)
    text = re.sub(r'@\w+', '', text)
    text = re.sub(r'#\w+', '', text)
    text = re.sub(r'\s+', ' ', text).strip()

    if len(text) < 10:
        logger.debug("Cleaned text too short (%d chars)", len(text))
        return None

    logger.debug("Extracted claim: %s", text[:80])
    return text


def is_claim(text: str) -> bool:
    """Heuristic check: is this text likely a factual claim?"""
    opinion_indicators = [
        r'\bi think\b', r'\bfeel\b', r'\bbelieve\b',
        r'\bimo\b', r'\bimho\b', r'\bin my opinion\b',
        r'\bmaybe\b', r'\bprobably\b',
    ]
    for pattern in opinion_indicators:
        if re.search(pattern, text, re.IGNORECASE):
            return False
    return len(text.split()) >= 3
