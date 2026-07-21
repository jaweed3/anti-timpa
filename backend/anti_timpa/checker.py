from __future__ import annotations

from dataclasses import dataclass

from anti_timpa.extractor import ExtractedUrl
from anti_timpa.patterns import OJK_PINJOL_ILEGAL, SCAM_KEYWORDS, JUDOL_KEYWORDS, URGENCY_MARKERS
from anti_timpa.models import EntityName

__all__ = ["UrlFlag", "PatternMatch", "check_ojk", "check_urls", "match_patterns"]


@dataclass(frozen=True)
class UrlFlag:
    url: str
    domain: str
    reasons: tuple[str, ...]


@dataclass(frozen=True)
class PatternMatch:
    scam_keywords: tuple[str, ...] = ()
    judol_keywords: tuple[str, ...] = ()
    urgency_markers: tuple[str, ...] = ()

    @property
    def total(self) -> int:
        return len(self.scam_keywords) + len(self.judol_keywords) + len(self.urgency_markers)


def check_ojk(text: str) -> tuple[EntityName, ...]:
    lowered = text.lower()
    return tuple(EntityName(e) for e in OJK_PINJOL_ILEGAL if e in lowered)


def check_urls(urls: tuple[ExtractedUrl, ...]) -> tuple[UrlFlag, ...]:
    flags: list[UrlFlag] = []
    for u in urls:
        reasons: list[str] = []
        if u.is_shortener:
            reasons.append("URL shortener — sulit dilacak")
        if reasons:
            flags.append(UrlFlag(url=u.url, domain=u.domain, reasons=tuple(reasons)))
    return tuple(flags)


def match_patterns(text: str) -> PatternMatch:
    lowered = text.lower()
    return PatternMatch(
        scam_keywords=tuple(kw for kw in SCAM_KEYWORDS if kw in lowered),
        judol_keywords=tuple(kw for kw in JUDOL_KEYWORDS if kw in lowered),
        urgency_markers=tuple(m for m in URGENCY_MARKERS if m in lowered),
    )
