import os
import re
from typing import Optional

import google.generativeai as genai
from openai import OpenAI


class LLMClient:
    """Unified client for LLM providers (OpenAI / Gemini)."""

    def __init__(self):
        self.openai_key = os.getenv("OPENAI_API_KEY", "")
        self.gemini_key = os.getenv("GEMINI_API_KEY", "")
        self._openai_client: Optional[OpenAI] = None
        self._gemini_configured = False
        self._init_clients()

    def _init_clients(self):
        if self.openai_key:
            self._openai_client = OpenAI(api_key=self.openai_key)
        if self.gemini_key:
            genai.configure(api_key=self.gemini_key)
            self._gemini_configured = True

    @property
    def available(self) -> bool:
        return bool(self.openai_key) or self._gemini_configured

    def chat(self, system_prompt: str, user_prompt: str, temperature: float = 0.3) -> str:
        if self.openai_key and self._openai_client:
            return self._chat_openai(system_prompt, user_prompt, temperature)
        if self._gemini_configured:
            return self._chat_gemini(system_prompt, user_prompt, temperature)
        raise RuntimeError("No LLM API key configured. Set OPENAI_API_KEY or GEMINI_API_KEY.")

    def _chat_openai(self, system: str, user: str, temperature: float) -> str:
        resp = self._openai_client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
            temperature=temperature,
        )
        return resp.choices[0].message.content or ""

    def _chat_gemini(self, system: str, user: str, temperature: float) -> str:
        model = genai.GenerativeModel(
            "gemini-1.5-flash",
            system_instruction=system,
            generation_config={"temperature": temperature},
        )
        resp = model.generate_content(user)
        return resp.text or ""
