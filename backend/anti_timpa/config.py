from __future__ import annotations

import os
import logging
from dataclasses import dataclass
from dotenv import load_dotenv

__all__ = ["CFG"]

load_dotenv()

logger = logging.getLogger("antitimpa.config")


def _int_env(key: str, default: int) -> int:
    raw = os.getenv(key)
    if raw is None:
        return default
    try:
        return int(raw)
    except ValueError:
        logger.error("Invalid %s='%s', fallback to %d", key, raw, default)
        return default


@dataclass(frozen=True)
class AppConfig:
    server_host: str = os.getenv("SERVER_HOST", "0.0.0.0")
    server_port: int = _int_env("SERVER_PORT", 8000)
    allowed_origins: tuple[str, ...] = ("*",)
    rate_limit_per_minute: int = _int_env("RATE_LIMIT_PER_MINUTE", 30)

    nara_api_key: str = os.getenv("NARA_ROUTER_API", "")
    nara_base_url: str = "https://router.bynara.id/v1"
    nara_model: str = "agnes-2.0-flash"

    cache_ttl_seconds: int = _int_env("CACHE_TTL_SECONDS", 3600)
    cache_max_size: int = _int_env("CACHE_MAX_SIZE", 1000)

    search_engine: str = os.getenv("SEARCH_ENGINE", "google")

    trusted_domains: tuple[str, ...] = (
        "reuters.com", "apnews.com", "bbc.com", "bbc.co.uk",
        "nytimes.com", "theguardian.com", "washingtonpost.com",
        "nature.com", "science.org", "who.int", "cdc.gov",
        "un.org", "worldbank.org", "sciencedirect.com",
        "scholar.google.com", "pubmed.ncbi.nlm.nih.gov",
        "kemkes.go.id", "kmov.id", "turnbackhoax.id",
        "komdigi.go.id", "liputan6.com", "medcom.id",
    )

    fact_check_domains: tuple[str, ...] = (
        "turnbackhoax.id", "komdigi.go.id", "kmov.id",
    )


CFG = AppConfig()
