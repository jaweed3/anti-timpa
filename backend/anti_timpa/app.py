from __future__ import annotations

import time
import logging
from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

from anti_timpa.config import CFG
from anti_timpa.models import VerificationRequest, VerificationResponse, ScamCheckRequest, ScamCheckResponse, FlaggedItem
from anti_timpa import extractor, checker, scorer as risk
from anti_timpa.extractor import ExtractedData
from anti_timpa.checker import UrlFlag, PatternMatch
from anti_timpa.scorer import RiskResult

logger = logging.getLogger("antitimpa")

limiter = Limiter(key_func=get_remote_address)


class AppState:
    def __init__(self) -> None:
        self._pipeline = None

    @property
    def pipeline(self):
        if self._pipeline is None:
            from anti_timpa.llm import LLMClient
            from anti_timpa.search import SearchEngine
            from anti_timpa.pipeline import VerificationPipeline
            llm = LLMClient()
            search = SearchEngine()
            self._pipeline = VerificationPipeline(llm=llm, search=search)
        return self._pipeline

    async def close(self) -> None:
        p = self._pipeline
        if p and hasattr(p, '_search'):
            await p._search.close()


_state = AppState()


@asynccontextmanager
async def lifespan(_app: FastAPI) -> AsyncIterator[None]:
    _ = _state.pipeline
    yield
    await _state.close()


app = FastAPI(
    title="AntiTimpa API",
    version="1.0.0",
    description="On-device scam/gambling/phishing detection backend for Android overlay",
    lifespan=lifespan,
)

app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)
app.add_middleware(
    CORSMiddleware,
    allow_origins=CFG.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------------------------------------------------------------------------
# Error helpers
# ---------------------------------------------------------------------------
def _err(status: int, detail: str, code: str = "BAD_REQUEST") -> JSONResponse:
    return JSONResponse(status_code=status, content={"error": detail, "code": code})


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------
@app.get("/health")
async def health():
    p = _state.pipeline
    return {"status": "ok", "llm": p._llm.available if p else False, "model": CFG.nara_model}


@app.post("/verify", response_model=VerificationResponse)
@limiter.limit(f"{CFG.rate_limit_per_minute}/minute")
async def verify(request: Request, body: VerificationRequest):
    text = body.text.strip()
    if len(text) < 5:
        return _err(400, "Text too short (min 5 characters)")

    p = _state.pipeline
    start = time.time()
    result = await p.verify(text[:5000])
    logger.info("POST /verify %.2fs — %s", time.time() - start, result.verdict)
    return result


@app.get("/verify", response_model=VerificationResponse)
@limiter.limit(f"{CFG.rate_limit_per_minute}/minute")
async def verify_get(request: Request, q: str = ""):
    text = q.strip()
    if len(text) < 5:
        return _err(400, "Query too short (min 5 characters)")

    p = _state.pipeline
    start = time.time()
    result = await p.verify(text[:5000])
    logger.info("GET /verify %.2fs — %s", time.time() - start, result.verdict)
    return result


@app.post("/check-scam", response_model=ScamCheckResponse)
async def check_scam(body: ScamCheckRequest):
    text = body.text.strip()
    if not text:
        return _err(400, "Text is empty")

    data = extractor.extract_all(text[:5000])
    ojk = checker.check_ojk(text[:5000])
    url_flags = checker.check_urls(data.urls)
    patterns = checker.match_patterns(text[:5000])
    result = risk.calculate_score(ojk, url_flags, patterns)

    return ScamCheckResponse(
        verdict=result.verdict,
        risk_score=result.score,
        flagged_items=_build_flags(data, url_flags, result),
        explanation=_explain(result),
        sources=list(ojk),
        should_blur=result.verdict in ("Terindikasi Penipuan", "Mencurigakan"),
    )


# ---------------------------------------------------------------------------
# Response builders
# ---------------------------------------------------------------------------
def _build_flags(data: ExtractedData, url_flags: tuple[UrlFlag, ...], result: RiskResult) -> list[FlaggedItem]:
    items: list[FlaggedItem] = []
    for a in data.accounts:
        items.append(FlaggedItem(type="account", value=a, reason="Nomor rekening terdeteksi"))
    for p in data.phones:
        items.append(FlaggedItem(type="phone", value=p, reason="Nomor telepon terdeteksi"))
    for u in url_flags:
        items.append(FlaggedItem(type="url", value=u.url, reason="; ".join(u.reasons)))
    for kw in result.patterns.scam_keywords:
        items.append(FlaggedItem(type="pattern", value=kw, reason="Pola bahasa penipuan"))
    return items


def _explain(result: RiskResult) -> str:
    if result.reasons:
        return result.reasons[0]
    return "Tidak ditemukan indikasi penipuan yang jelas."
