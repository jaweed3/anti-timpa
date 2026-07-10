"""
Pydantic models for API request/response.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class VerificationRequest(BaseModel):
    """Request body for /verify endpoint."""
    text: str = Field(
        ...,
        min_length=5,
        max_length=5000,
        description="Text or claim to verify",
        examples=["Vaksin mRNA bisa mengubah DNA manusia"],
    )
    language: str = Field(
        default="en",
        description="Language code (en, id, etc.)",
        examples=["id"],
    )


class Source(BaseModel):
    """A source used for verification."""
    title: str
    url: str
    snippet: str


class VerificationResponse(BaseModel):
    """Response body from /verify endpoint."""
    claim: str
    verdict: str  # Supported | Contradicted | Misleading | Mixed | Insufficient Evidence | Unknown
    confidence: float = Field(ge=0.0, le=1.0)
    explanation: str
    sources: List[Source]
