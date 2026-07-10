"""
FactLens Backend Configuration

Environment variables are loaded from .env file.
"""

import os
from dotenv import load_dotenv

load_dotenv()

# --- NaraRouter API ---
NARA_ROUTER_API_KEY: str = os.getenv("NARA_ROUTER_API", "")
NARA_BASE_URL: str = "https://router.bynara.id/v1"
NARA_MODEL: str = "mimo-v2.5-hermes"

# --- Server ---
SERVER_HOST: str = os.getenv("SERVER_HOST", "0.0.0.0")
SERVER_PORT: int = int(os.getenv("SERVER_PORT", "8000"))

# --- CORS ---
# Android emulator uses 10.0.2.2 to reach host machine
# Physical device uses laptop's LAN IP (e.g. 192.168.x.x)
ALLOWED_ORIGINS: list[str] = ["*"]
