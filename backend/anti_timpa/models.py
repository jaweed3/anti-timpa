from __future__ import annotations

from typing import NewType, List
from pydantic import BaseModel, Field, ConfigDict

__all__ = [
    "AccountNumber", "PhoneNumber", "BankName", "DomainName", "EntityName",
    "VerificationRequest", "Source", "VerificationResponse",
    "ScamCheckRequest", "FlaggedItem", "ScamCheckResponse", "ErrorResponse",
]

AccountNumber = NewType("AccountNumber", str)
PhoneNumber = NewType("PhoneNumber", str)
BankName = NewType("BankName", str)
DomainName = NewType("DomainName", str)
EntityName = NewType("EntityName", str)


class VerificationRequest(BaseModel):
    text: str = Field(..., min_length=5, max_length=5000, description="Claim text to verify", examples=["Vaksin mRNA bisa mengubah DNA manusia"])
    language: str = Field(default="en", description="Language code", examples=["id"])


class Source(BaseModel):
    title: str
    url: str
    snippet: str


class VerificationResponse(BaseModel):
    claim: str
    verdict: str = Field(description="Supported | Contradicted | Misleading | Mixed | Insufficient Evidence")
    confidence: float = Field(ge=0.0, le=1.0)
    explanation: str
    sources: List[Source]


class ScamCheckRequest(BaseModel):
    text: str = Field(..., min_length=0, max_length=5000, description="Text to scan for scam/gambling patterns")


class FlaggedItem(BaseModel):
    type: str = Field(description="account | phone | url | pattern")
    value: str
    reason: str = Field(description="Bahasa Indonesia explanation for this flag")


class ScamCheckResponse(BaseModel):
    verdict: str = Field(description="Terindikasi Penipuan | Mencurigakan | Aman")
    risk_score: int = Field(ge=0, le=100, description="0-100 risk score")
    flagged_items: List[FlaggedItem] = Field(description="All flagged items found")
    explanation: str = Field(description="Human-readable explanation in Bahasa Indonesia")
    sources: list[str] = Field(description="Source references for flagged items")
    should_blur: bool = Field(description="True if content should be blurred")

    model_config = ConfigDict(populate_by_name=True)


class ErrorResponse(BaseModel):
    error: str = Field(description="Error message")
    code: str = Field(description="Error code")
