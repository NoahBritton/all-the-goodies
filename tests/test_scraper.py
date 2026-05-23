"""Tests for the petition scraper parser."""
from __future__ import annotations

import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from scraper import ScrapeError, _parse  # noqa: E402


SAMPLE_OK = """
<html><head></head><body>
<script>window.__INITIAL_STATE__ = {"foo":1,"signatureCount":{"displayed":12345,"total":12350,"goal":15000},"bar":2,"weeklySignatureCount":482};</script>
</body></html>
"""

SAMPLE_NO_WEEKLY = """
<script>"signatureCount":{"displayed":7,"total":7,"goal":100}</script>
"""

SAMPLE_BAD = "<html><body>nothing here</body></html>"


def test_parse_full_block():
    out = _parse(SAMPLE_OK)
    assert out == {"displayed": 12345, "total": 12350, "goal": 15000, "weekly": 482}


def test_parse_without_weekly():
    out = _parse(SAMPLE_NO_WEEKLY)
    assert out["total"] == 7
    assert out["weekly"] is None


def test_parse_raises_when_missing():
    with pytest.raises(ScrapeError):
        _parse(SAMPLE_BAD)
