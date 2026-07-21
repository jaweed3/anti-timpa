from __future__ import annotations

import re
from dataclasses import dataclass

from anti_timpa.patterns import BANKS, URL_SHORTENERS
from anti_timpa.models import PhoneNumber, AccountNumber, BankName

__all__ = ["ExtractedUrl", "ExtractedData", "extract_all"]

_PHONE = re.compile(r"(?:\+62|62|0)8[1-9]\d{7,11}")
_ACCOUNT = re.compile(r"\b\d{10,16}\b")
_URL = re.compile(r"https?://[^\s]+|bit\.ly/\S+|tinyurl\.com/\S+|wa\.me/\S+")


@dataclass(frozen=True)
class ExtractedUrl:
    url: str
    domain: str
    is_shortener: bool


@dataclass(frozen=True)
class ExtractedData:
    phones: tuple[PhoneNumber, ...] = ()
    accounts: tuple[AccountNumber, ...] = ()
    urls: tuple[ExtractedUrl, ...] = ()
    banks: tuple[BankName, ...] = ()


def _parse_domain(url: str) -> str:
    m = re.match(r"https?://([^/\s]+)", url)
    return m.group(1) if m else ""


def _extract_phones(text: str) -> tuple[PhoneNumber, ...]:
    return tuple(sorted(set(_PHONE.findall(text))))


def _extract_accounts(text: str) -> tuple[AccountNumber, ...]:
    return tuple(sorted(set(_ACCOUNT.findall(text))))


def _extract_urls(text: str) -> tuple[ExtractedUrl, ...]:
    seen: set[str] = set()
    results: list[ExtractedUrl] = []
    for m in _URL.finditer(text):
        raw = m.group(0)
        if raw not in seen:
            seen.add(raw)
            results.append(
                ExtractedUrl(
                    url=raw,
                    domain=_parse_domain(raw),
                    is_shortener=any(s in raw.lower() for s in URL_SHORTENERS),
                )
            )
    return tuple(results)


def _extract_banks(text: str) -> tuple[BankName, ...]:
    lowered = text.lower()
    return tuple(sorted(set(name for alias, name in BANKS.items() if alias in lowered)))


def extract_all(text: str) -> ExtractedData:
    return ExtractedData(
        phones=_extract_phones(text),
        accounts=_extract_accounts(text),
        urls=_extract_urls(text),
        banks=_extract_banks(text),
    )
