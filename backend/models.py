from pydantic import BaseModel
from typing import List, Optional


class VerificationRequest(BaseModel):
    text: str
    language: str = "en"


class Source(BaseModel):
    title: str
    url: str
    snippet: str


class VerificationResponse(BaseModel):
    claim: str
    verdict: str  # Supported | Contradicted | Misleading | Mixed | Insufficient Evidence | Unknown
    confidence: float
    explanation: str
    sources: List[Source]
