import pytest
from httpx import AsyncClient, ASGITransport
from anti_timpa.app import app


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
    assert resp.status_code in (400, 422)


@pytest.mark.asyncio
async def test_check_scam_empty(client):
    resp = await client.post("/check-scam", json={"text": ""})
    assert resp.status_code == 400


@pytest.mark.asyncio
async def test_check_scam_clean(client):
    resp = await client.post("/check-scam", json={"text": "Halo apa kabar?"})
    assert resp.status_code == 200
    data = resp.json()
    assert data["verdict"] == "Aman"
    assert data["risk_score"] == 0
    assert data["should_blur"] is False
