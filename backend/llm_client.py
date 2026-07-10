"""
LLM client for NaraRouter API (OpenAI-compatible).
"""

import logging
import time
from typing import Optional, cast
from openai import OpenAI, APIError, APIConnectionError, RateLimitError
from openai.types.chat import ChatCompletionMessageParam
import config

logger = logging.getLogger("factlens.llm")

# Retry settings
MAX_RETRIES = 3
RETRY_DELAY_BASE = 1.0  # seconds


class LLMClient:
    """Unified client for NaraRouter API (OpenAI-compatible)."""

    def __init__(self):
        self.api_key = config.NARA_ROUTER_API_KEY
        self._client: Optional[OpenAI] = None
        self._init_client()

    def _init_client(self):
        if self.api_key:
            self._client = OpenAI(
                api_key=self.api_key,
                base_url=config.NARA_BASE_URL,
                timeout=30.0,
            )

    @property
    def available(self) -> bool:
        return bool(self.api_key)

    def _retry_call(self, func, *args, **kwargs):
        """Execute a function with retry logic for transient errors."""
        last_error = None
        for attempt in range(MAX_RETRIES):
            try:
                return func(*args, **kwargs)
            except RateLimitError as e:
                last_error = e
                delay = RETRY_DELAY_BASE * (2 ** attempt)
                logger.warning("Rate limited (attempt %d/%d), retrying in %.1fs", attempt + 1, MAX_RETRIES, delay)
                time.sleep(delay)
            except APIConnectionError as e:
                last_error = e
                delay = RETRY_DELAY_BASE * (2 ** attempt)
                logger.warning("Connection error (attempt %d/%d), retrying in %.1fs: %s", attempt + 1, MAX_RETRIES, delay, e)
                time.sleep(delay)
            except APIError as e:
                if e.status_code and e.status_code >= 500:
                    last_error = e
                    delay = RETRY_DELAY_BASE * (2 ** attempt)
                    logger.warning("Server error %d (attempt %d/%d), retrying in %.1fs", e.status_code, attempt + 1, MAX_RETRIES, delay)
                    time.sleep(delay)
                else:
                    raise
        raise last_error

    def chat(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float = 0.3,
        max_tokens: int = 1024,
    ) -> str:
        if not self._client:
            raise RuntimeError("No NARA_ROUTER_API key configured.")

        def _call():
            return self._client.chat.completions.create(
                model=config.NARA_MODEL,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                temperature=temperature,
                max_tokens=max_tokens,
            )

        resp = self._retry_call(_call)
        return resp.choices[0].message.content or ""

    def chat_with_images(
        self,
        system_prompt: str,
        text: str,
        image_base64: str,
        mime_type: str = "image/jpeg",
        temperature: float = 0.3,
        max_tokens: int = 1024,
    ) -> str:
        """Send a multimodal request with text + image (for vision-capable models)."""
        if not self._client:
            raise RuntimeError("No NARA_ROUTER_API key configured.")

        user_content = [
            {"type": "text", "text": text},
            {
                "type": "image_url",
                "image_url": {
                    "url": f"data:{mime_type};base64,{image_base64}",
                },
            },
        ]

        messages = cast(
            list[ChatCompletionMessageParam],
            [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_content},
            ],
        )

        def _call():
            return self._client.chat.completions.create(
                model=config.NARA_MODEL,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
            )

        resp = self._retry_call(_call)
        return resp.choices[0].message.content or ""
