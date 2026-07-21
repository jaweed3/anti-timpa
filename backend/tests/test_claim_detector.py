import pytest
from anti_timpa.claim import extract_claim, is_claim


def test_extract_claim_removes_urls():
    t = "Check this https://example.com it's important"
    assert "https://" not in (extract_claim(t) or "")


def test_extract_claim_short():
    assert extract_claim("Hi") is None


def test_is_claim_true():
    assert is_claim("The earth orbits the sun") is True


def test_is_claim_opinion():
    assert is_claim("I think pizza is good") is False
