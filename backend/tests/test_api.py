# python -m pytest tests/ -v

import pytest
from httpx import AsyncClient, ASGITransport
from main import app


@pytest.fixture
def client():
    transport = ASGITransport(app=app)
    return AsyncClient(transport=transport, base_url="http://test")


@pytest.mark.asyncio
async def test_health(client):
    resp = await client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_verify_short_text(client):
    resp = await client.post("/verify", json={"text": "hi"})
    assert resp.status_code == 400


@pytest.mark.asyncio
async def test_verify_get(client):
    resp = await client.get("/verify", params={"q": "The earth is flat"})
    assert resp.status_code == 200
    data = resp.json()
    assert "claim" in data
    assert "verdict" in data
    assert "confidence" in data
    assert "explanation" in data
    assert "sources" in data
