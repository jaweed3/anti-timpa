import os
from typing import Optional, List, Dict, Any, Union

from openai import OpenAI


class LLMClient:
    """Unified client for NaraRouter API (OpenAI-compatible)."""

    BASE_URL = "https://router.bynara.id/v1"
    MODEL = "mimo-v2.5-hermes"

    def __init__(self):
        self.api_key = os.getenv("NARA_ROUTER_API", "")
        self._client: Optional[OpenAI] = None
        self._init_client()

    def _init_client(self):
        if self.api_key:
            self._client = OpenAI(
                api_key=self.api_key,
                base_url=self.BASE_URL,
            )

    @property
    def available(self) -> bool:
        return bool(self.api_key)

    def chat(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float = 0.3,
        max_tokens: int = 1024,
    ) -> str:
        if not self._client:
            raise RuntimeError("No NARA_ROUTER_API key configured.")

        resp = self._client.chat.completions.create(
            model=self.MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            temperature=temperature,
            max_tokens=max_tokens,
        )
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

        content: List[Dict[str, Any]] = [
            {"type": "text", "text": text},
            {
                "type": "image_url",
                "image_url": {
                    "url": f"data:{mime_type};base64,{image_base64}",
                },
            },
        ]

        resp = self._client.chat.completions.create(
            model=self.MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": content},
            ],
            temperature=temperature,
            max_tokens=max_tokens,
        )
        return resp.choices[0].message.content or ""
