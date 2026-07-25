from __future__ import annotations

import logging
import urllib.parse
from urllib.parse import parse_qs, urlparse
from typing import List, Protocol
from typing_extensions import runtime_checkable

import httpx
from bs4 import BeautifulSoup

from anti_timpa.config import CFG
from anti_timpa.models import Source

__all__ = ["Searcher", "SearchEngine"]

logger = logging.getLogger("antitimpa.search")


class Searcher(Protocol):
    async def search(self, query: str, max_results: int = 5) -> List[Source]: ...
    async def close(self) -> None: ...


class SearchEngine:
    def __init__(self) -> None:
        self._client = httpx.AsyncClient(
            timeout=15.0,
            follow_redirects=True,
            headers={"User-Agent": "Mozilla/5.0 (Linux; Android 14) AntiTimpa/1.0"},
        )

    async def search(self, query: str, max_results: int = 5) -> List[Source]:
        engine = CFG.search_engine
        if engine == "google":
            raw = await self._google(query, max_results)
        else:
            raw = await self._duckduckgo(query, max_results)

        seen: set[str] = set()
        unique: list[Source] = []
        for s in raw:
            if s.url not in seen:
                seen.add(s.url)
                unique.append(s)

        trusted = [s for s in unique if any(d in s.url for d in CFG.trusted_domains)]
        others = [s for s in unique if s not in trusted]
        result = (trusted + others)[:max_results]
        logger.info("Found %d results (%d trusted) via %s", len(result), len(trusted), engine)
        return result

    async def _google(self, query: str, max_results: int) -> List[Source]:
        url = f"https://www.google.com/search?q={urllib.parse.quote(query)}&hl=en"
        try:
            resp = await self._client.get(url)
            resp.raise_for_status()
            soup = BeautifulSoup(resp.text, "lxml")
            results: list[Source] = []

            for el in soup.select("#search .g")[:max_results]:
                a = el.select_one("a[href^='/url?q=']") or el.select_one("a[href^='http']")
                h3 = el.select_one("h3")
                snippet_el = el.select_one(".VwiC3b, span.aCOpRe, .st")

                if a and h3:
                    href = str(a.get("href", ""))
                    if href.startswith("/url?q="):
                        href = parse_qs(urlparse(href).query).get("q", [""])[0]
                    snippet = snippet_el.get_text(strip=True) if snippet_el else ""
                    results.append(Source(title=h3.get_text(strip=True), url=href, snippet=snippet))

            return results
        except httpx.HTTPStatusError as e:
            logger.error("Google HTTP %s", e.response.status_code)
            return []
        except Exception:
            logger.exception("Google search failed")
            return []

    async def _duckduckgo(self, query: str, max_results: int) -> List[Source]:
        url = f"https://html.duckduckgo.com/html/?q={urllib.parse.quote(query)}"
        try:
            resp = await self._client.get(url)
            resp.raise_for_status()
            soup = BeautifulSoup(resp.text, "lxml")
            results: list[Source] = []
            for el in soup.select(".result")[:max_results]:
                a = el.select_one(".result__title a")
                s = el.select_one(".result__snippet")
                if a and s:
                    href = str(a.get("href", ""))
                    if "uddg=" in href:
                        href = parse_qs(urlparse(href).query).get("uddg", [""])[0]
                    results.append(Source(title=a.get_text(strip=True), url=href, snippet=s.get_text(strip=True)))
            return results
        except httpx.HTTPStatusError as e:
            logger.error("DDG HTTP %s", e.response.status_code)
            return []
        except Exception:
            logger.exception("DDG failed")
            return []

    async def close(self) -> None:
        await self._client.aclose()
