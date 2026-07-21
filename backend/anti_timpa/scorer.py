from __future__ import annotations

from dataclasses import dataclass

from anti_timpa.checker import UrlFlag, PatternMatch, match_patterns
from anti_timpa.patterns import JUDOL_DOMAINS
from anti_timpa.models import EntityName

__all__ = ["RiskResult", "calculate_score", "determine_verdict"]

# Scoring constants
_OJK_PINJOL_HIT = 40
_JUDOL_HIT = 30
_URL_SHORTENER = 20
_KEYWORDS_3PLUS = 20
_KEYWORDS_1_2 = 10
_URGENCY = 5

_VERDICT_SCAM = 51
_VERDICT_SUSPICIOUS = 21


@dataclass(frozen=True)
class RiskResult:
    score: int
    verdict: str
    reasons: tuple[str, ...]
    patterns: PatternMatch


def determine_verdict(score: int) -> str:
    match score:
        case _ if score >= _VERDICT_SCAM:
            return "Terindikasi Penipuan"
        case _ if score >= _VERDICT_SUSPICIOUS:
            return "Mencurigakan"
        case _:
            return "Aman"


def calculate_score(
    ojk_found: tuple[EntityName, ...],
    url_flagged: tuple[UrlFlag, ...],
    patterns: PatternMatch,
) -> RiskResult:
    reasons: list[str] = []

    score = 0
    score += _score_ojk(ojk_found, reasons)
    score += _score_judol(patterns, reasons)
    score += _score_urls(url_flagged, reasons)
    score += _score_keywords(patterns, reasons)
    score += _score_urgency(patterns, reasons)

    return RiskResult(
        score=min(score, 100),
        verdict=determine_verdict(min(score, 100)),
        reasons=tuple(reasons),
        patterns=patterns,
    )


def _score_ojk(found: tuple[EntityName, ...], reasons: list[str]) -> int:
    if not found:
        return 0
    reasons.append(f"Entitas terdaftar di OJK pinjol ilegal: {', '.join(found)}")
    return _OJK_PINJOL_HIT


def _score_judol(patterns: PatternMatch, reasons: list[str]) -> int:
    if patterns.judol_keywords:
        reasons.append("Mengandung kata kunci judi online")
        return _JUDOL_HIT
    return 0


def _score_urls(flagged: tuple[UrlFlag, ...], reasons: list[str]) -> int:
    if not flagged:
        return 0
    reasons.append("Menggunakan URL shortener yang mencurigakan")
    return _URL_SHORTENER


def _score_keywords(patterns: PatternMatch, reasons: list[str]) -> int:
    t = patterns.total
    match t:
        case _ if t >= 3:
            reasons.append(f"Mengandung {t} pola bahasa penipuan/judi")
            return _KEYWORDS_3PLUS
        case _ if t >= 1:
            reasons.append(f"Mengandung {t} pola bahasa penipuan/judi")
            return _KEYWORDS_1_2
    return 0


def _score_urgency(patterns: PatternMatch, reasons: list[str]) -> int:
    if patterns.urgency_markers:
        reasons.append("Mengandung urgensi palsu (batas waktu/segera)")
        return _URGENCY
    return 0
