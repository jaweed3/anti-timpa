"""
FactLens API — FastAPI application.

Routes: /health, /verify (POST + GET)
Pipeline: claim detection → search → LLM verdict
"""

import time
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

import config
from llm_client import LLMClient
from models import VerificationRequest, VerificationResponse
from search_engine import SearchEngine
from verification_pipeline import VerificationPipeline

logger = logging.getLogger("factlens")

# ---------------------------------------------------------------------------
# Rate limiter
# ---------------------------------------------------------------------------
limiter = Limiter(key_func=get_remote_address)

# ---------------------------------------------------------------------------
# Pipeline singleton
# ---------------------------------------------------------------------------
pipeline: VerificationPipeline | None = None


def get_pipeline() -> VerificationPipeline:
    global pipeline
    if pipeline is None:
        llm = LLMClient()
        search = SearchEngine()
        pipeline = VerificationPipeline(llm=llm, search=search)
        if not llm.available:
            logger.warning("No LLM API key found. Set NARA_ROUTER_API in .env")
    return pipeline


# ---------------------------------------------------------------------------
# Lifespan
# ---------------------------------------------------------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    get_pipeline()
    yield
    if pipeline and pipeline.search:
        await pipeline.search.close()


# ---------------------------------------------------------------------------
# App
# ---------------------------------------------------------------------------
app = FastAPI(
    title="FactLens API",
    version="1.0.0",
    description="AI-powered fact verification backend",
    lifespan=lifespan,
)

app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

app.add_middleware(
    CORSMiddleware,
    allow_origins=config.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------
@app.get("/health")
async def health():
    """Health check endpoint."""
    llm_available = pipeline.llm.available if pipeline else False
    return {
        "status": "ok",
        "llm": llm_available,
        "model": config.NARA_MODEL,
    }


@app.post("/verify", response_model=VerificationResponse)
@limiter.limit(f"{config.RATE_LIMIT_PER_MINUTE}/minute")
async def verify(request: Request, body: VerificationRequest):
    if not body.text or len(body.text.strip()) < 5:
        raise HTTPException(status_code=400, detail="Text too short (min 5 characters)")

    # Cap input length to prevent abuse
    text = body.text[:5000]

    p = get_pipeline()
    start = time.time()
    result = await p.verify(text)
    elapsed = time.time() - start
    logger.info("POST /verify completed in %.2fs — verdict: %s", elapsed, result.verdict)
    return result


@app.get("/verify", response_model=VerificationResponse)
@limiter.limit(f"{config.RATE_LIMIT_PER_MINUTE}/minute")
async def verify_get(request: Request, q: str = ""):
    if not q or len(q.strip()) < 5:
        raise HTTPException(status_code=400, detail="Query too short (min 5 characters)")

    # Cap input length to prevent abuse
    text = q[:5000]

    p = get_pipeline()
    start = time.time()
    result = await p.verify(text)
    elapsed = time.time() - start
    logger.info("GET /verify completed in %.2fs — verdict: %s", elapsed, result.verdict)
    return result
