"""
FactLens Backend Configuration

Environment variables are loaded from .env file.
"""

import os
import logging
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger("factlens.config")

# --- NaraRouter API ---
NARA_ROUTER_API_KEY: str = os.getenv("NARA_ROUTER_API", "")
NARA_BASE_URL: str = "https://router.bynara.id/v1"
NARA_MODEL: str = "mimo-v2.5-hermes"

# --- Server ---
SERVER_HOST: str = os.getenv("SERVER_HOST", "0.0.0.0")
try:
    SERVER_PORT: int = int(os.getenv("SERVER_PORT", "8000"))
except ValueError:
    logger.error("Invalid SERVER_PORT env var, defaulting to 8000")
    SERVER_PORT = 8000

# --- CORS ---
# For Android app, allow all origins (no browser-based auth)
# If adding web frontend later, restrict to specific domains
ALLOWED_ORIGINS: list[str] = ["*"]

# --- Rate Limiting ---
RATE_LIMIT_PER_MINUTE: int = int(os.getenv("RATE_LIMIT_PER_MINUTE", "30"))

# --- Cache ---
CACHE_TTL_SECONDS: int = int(os.getenv("CACHE_TTL_SECONDS", "3600"))  # 1 hour
CACHE_MAX_SIZE: int = int(os.getenv("CACHE_MAX_SIZE", "1000"))

# --- Trusted domains (single source of truth) ---
TRUSTED_DOMAINS: list[str] = [
    "reuters.com", "apnews.com", "bbc.com", "bbc.co.uk",
    "nytimes.com", "theguardian.com", "washingtonpost.com",
    "nature.com", "science.org", "who.int", "cdc.gov",
    "un.org", "worldbank.org", "sciencedirect.com",
    "scholar.google.com", "pubmed.ncbi.nlm.nih.gov",
    # Indonesian trusted sources
    "kemkes.go.id", "kmov.id", "turnbackhoax.id",
    "komdigi.go.id", "liputan6.com", "medcom.id",
]
