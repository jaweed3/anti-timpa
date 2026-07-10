"""
Search engine for evidence retrieval via DuckDuckGo.
"""

import logging
import urllib.parse
from typing import List

import httpx
from bs4 import BeautifulSoup

import config
from models import Source

logger = logging.getLogger("factlens.search")


class SearchEngine:
    """Retrieve evidence from trusted sources."""

    def __init__(self):
        self.client = httpx.AsyncClient(
            timeout=15.0,
            follow_redirects=True,
            headers={
                "User-Agent": "Mozilla/5.0 (Linux; Android 14) FactLens/1.0",
            }
        )

    async def search(self, query: str, max_results: int = 5) -> List[Source]:
        logger.info("Searching for: %s", query[:80])
        sources = await self._duckduckgo_search(query, max_results)

        # Deduplicate by URL
        seen = set()
        unique = []
        for s in sources:
            if s.url not in seen:
                seen.add(s.url)
                unique.append(s)

        # Sort: trusted domains first
        trusted = [s for s in unique if any(d in s.url for d in config.TRUSTED_DOMAINS)]
        others = [s for s in unique if s not in trusted]
        result = (trusted + others)[:max_results]

        logger.info("Found %d results (%d trusted, %d other)", len(result), len(trusted), len(others))
        return result

    async def _duckduckgo_search(self, query: str, max_results: int) -> List[Source]:
        """Search using DuckDuckGo's HTML interface (no API key needed)."""
        url = f"https://html.duckduckgo.com/html/?q={urllib.parse.quote(query)}"

        try:
            resp = await self.client.get(url)
            resp.raise_for_status()
            soup = BeautifulSoup(resp.text, "lxml")
            results = []

            for result in soup.select(".result")[:max_results]:
                title_el = result.select_one(".result__title a")
                snippet_el = result.select_one(".result__snippet")

                if title_el and snippet_el:
                    title = title_el.get_text(strip=True)
                    href = title_el.get("href", "")
                    # DuckDuckGo redirects through their own URL
                    if "uddg=" in str(href):
                        from urllib.parse import parse_qs, urlparse
                        parsed = urlparse(str(href))
                        qs = parse_qs(parsed.query)
                        href = qs.get("uddg", [""])[0]
                    snippet = snippet_el.get_text(strip=True)
                    results.append(Source(title=title, url=href, snippet=snippet))

            logger.debug("DuckDuckGo returned %d raw results", len(results))
            return results
        except httpx.HTTPStatusError as e:
            logger.error("DuckDuckGo HTTP error %s for query: %s", e.response.status_code, query[:60])
            return []
        except Exception as e:
            logger.exception("DuckDuckGo search failed: %s", e)
            return []

    async def close(self):
        await self.client.aclose()
