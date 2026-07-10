"""
FactLens API — FastAPI application.

Routes: /health, /verify (POST + GET)
Pipeline: claim detection → search → LLM verdict
"""

import time
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

import config
from llm_client import LLMClient
from models import VerificationRequest, VerificationResponse
from search_engine import SearchEngine
from verification_pipeline import VerificationPipeline

logger = logging.getLogger("factlens")

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
    return {"status": "ok"}


@app.post("/verify", response_model=VerificationResponse)
async def verify(request: VerificationRequest):
    if not request.text or len(request.text.strip()) < 5:
        raise HTTPException(status_code=400, detail="Text too short")

    p = get_pipeline()
    start = time.time()
    result = await p.verify(request.text)
    elapsed = time.time() - start
    logger.info("Verification completed in %.2fs", elapsed)
    return result


@app.get("/verify", response_model=VerificationResponse)
async def verify_get(q: str = ""):
    if not q or len(q.strip()) < 5:
        raise HTTPException(status_code=400, detail="Query too short")

    p = get_pipeline()
    start = time.time()
    result = await p.verify(q)
    elapsed = time.time() - start
    logger.info("Verification completed in %.2fs", elapsed)
    return result
