from __future__ import annotations

import logging
import time
from typing import Protocol

from openai import OpenAI, APIError, APIConnectionError, RateLimitError

from anti_timpa.config import CFG

__all__ = ["TextLLM", "LLMClient"]

logger = logging.getLogger("antitimpa.llm")
_MAX_RETRIES = 3
_RETRY_DELAY = 1.0


class TextLLM(Protocol):
    @property
    def available(self) -> bool: ...

    def chat(self, system_prompt: str, user_prompt: str, temperature: float = 0.3, max_tokens: int = 1024) -> str: ...


class LLMClient:
    def __init__(self) -> None:
        self._client: OpenAI | None = None
        if CFG.nara_api_key:
            self._client = OpenAI(api_key=CFG.nara_api_key, base_url=CFG.nara_base_url, timeout=30.0)

    @property
    def available(self) -> bool:
        return self._client is not None

    def chat(self, system_prompt: str, user_prompt: str, temperature: float = 0.3, max_tokens: int = 1024) -> str:
        if not self._client:
            raise RuntimeError("No NARA_ROUTER_API key configured.")

        def _call():
            resp = self._client.chat.completions.create(
                model=CFG.nara_model,
                messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
                temperature=temperature,
                max_tokens=max_tokens,
            )
            return resp.choices[0].message.content or ""

        return _retry(_call)

    def chat_with_images(self, system_prompt: str, text: str, image_base64: str, mime_type: str = "image/jpeg", temperature: float = 0.3, max_tokens: int = 1024) -> str:
        if not self._client:
            raise RuntimeError("No NARA_ROUTER_API key configured.")

        def _call():
            resp = self._client.chat.completions.create(
                model=CFG.nara_model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": [{"type": "text", "text": text}, {"type": "image_url", "image_url": {"url": f"data:{mime_type};base64,{image_base64}"}}]},
                ],
                temperature=temperature,
                max_tokens=max_tokens,
            )
            return resp.choices[0].message.content or ""

        return _retry(_call)


def _retry(func, *args, **kwargs):
    last = None
    for attempt in range(_MAX_RETRIES):
        try:
            return func(*args, **kwargs)
        except RateLimitError as e:
            last = e
            d = _RETRY_DELAY * (2**attempt)
            logger.warning("Rate limited (%d/%d), wait %.1fs", attempt + 1, _MAX_RETRIES, d)
            time.sleep(d)
        except APIConnectionError as e:
            last = e
            d = _RETRY_DELAY * (2**attempt)
            logger.warning("Conn error (%d/%d), wait %.1fs: %s", attempt + 1, _MAX_RETRIES, d, e)
            time.sleep(d)
        except APIError as e:
            if e.status_code and e.status_code >= 500:
                last = e
                d = _RETRY_DELAY * (2**attempt)
                logger.warning("Server %d (%d/%d), wait %.1fs", e.status_code, attempt + 1, _MAX_RETRIES, d)
                time.sleep(d)
            else:
                raise
    raise last
