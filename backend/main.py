import os
import time
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from llm_client import LLMClient
from models import VerificationRequest, VerificationResponse
from search_engine import SearchEngine
from verification_pipeline import VerificationPipeline

load_dotenv()

# Global pipeline instance
pipeline: VerificationPipeline = None


def get_pipeline() -> VerificationPipeline:
    global pipeline
    if pipeline is None:
        llm = LLMClient()
        search = SearchEngine()
        pipeline = VerificationPipeline(llm=llm, search=search)
        if not llm.available:
            print("WARNING: No LLM API key found. Set OPENAI_API_KEY or GEMINI_API_KEY.")
    return pipeline


@asynccontextmanager
async def lifespan(app: FastAPI):
    get_pipeline()
    yield
    if pipeline and pipeline.search:
        await pipeline.search.close()


app = FastAPI(
    title="FactLens API",
    version="1.0.0",
    description="AI-powered fact verification backend",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


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
    print(f"Verification completed in {elapsed:.2f}s")
    return result


@app.get("/verify", response_model=VerificationResponse)
async def verify_get(q: str = ""):
    if not q or len(q.strip()) < 5:
        raise HTTPException(status_code=400, detail="Query too short")

    p = get_pipeline()
    start = time.time()
    result = await p.verify(q)
    elapsed = time.time() - start
    print(f"Verification completed in {elapsed:.2f}s")
    return result
