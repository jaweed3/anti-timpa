import pytest
from anti_timpa.extractor import extract_all, ExtractedUrl
from anti_timpa.checker import check_ojk, check_urls, match_patterns
from anti_timpa.scorer import calculate_score


def test_extract_phones():
    r = extract_all("Hubungi 08123456789 atau 628123456789")
    assert "08123456789" in r.phones
    assert "628123456789" in r.phones


def test_extract_accounts():
    r = extract_all("Rekening 1234567890123456")
    assert "1234567890123456" in r.accounts


def test_extract_urls_shortener():
    r = extract_all("Cek bit.ly/abc123")
    assert len(r.urls) == 1
    assert r.urls[0].is_shortener


def test_extract_banks():
    r = extract_all("Transfer ke BCA")
    assert "BCA" in r.banks


def test_extract_all():
    r = extract_all("BCA 08123456789 bit.ly/x rek 1234567890")
    assert "BCA" in r.banks
    assert len(r.phones) == 1
    assert len(r.urls) == 1


def test_check_ojk_hit():
    assert "danacepat" in check_ojk("Pinjam uang di danacepat sekarang")


def test_check_ojk_miss():
    assert check_ojk("Ini aman saja") == ()


def test_check_url_pattern():
    r = extract_all("https://bit.ly/x")
    flags = check_urls(r.urls)
    assert len(flags) == 1
    assert "URL shortener" in flags[0].reasons[0]


def test_match_patterns():
    r = match_patterns("Transfer sekarang juga! Batas waktu hari ini")
    assert "transfer sekarang" in r.scam_keywords
    assert "hari ini" in r.urgency_markers


def test_match_patterns_judol():
    r = match_patterns("Main slot gacor maxwin di sini")
    assert "gacor" in r.judol_keywords
    assert "slot" in r.judol_keywords


def test_risk_scorer_clean():
    p = match_patterns("Halo apa kabar?")
    r = calculate_score((), (), p)
    assert r.score == 0
    assert r.verdict == "Aman"


def test_risk_scorer_ojk():
    p = match_patterns("Pinjam di danacepat")
    r = calculate_score(("danacepat",), (), p)
    assert r.score >= 40
    assert r.verdict == "Mencurigakan"


def test_risk_scorer_scam_shortener():
    text = "transfer sekarang batas waktu hadiah terpilih bit.ly/x"
    data = extract_all(text)
    flags = check_urls(data.urls)
    p = match_patterns(text)
    r = calculate_score((), flags, p)
    assert r.score >= 25
    assert r.verdict in ("Mencurigakan", "Terindikasi Penipuan")


@pytest.mark.asyncio
async def test_check_scam_endpoint():
    from httpx import AsyncClient, ASGITransport
    from anti_timpa.app import app
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.post("/check-scam", json={"text": "Dapatkan jackpot slot gacor maxwin! Transfer sekarang ke BCA 1234567890 bit.ly/x"})
        assert resp.status_code == 200
        data = resp.json()
        assert "verdict" in data
        assert data["risk_score"] > 0
        assert data["should_blur"]
