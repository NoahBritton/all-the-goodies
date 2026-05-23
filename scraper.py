"""Fetch petition signature counts from Change.org by scraping inline JSON."""
from __future__ import annotations

import re
import time
from datetime import datetime, timezone
from typing import Optional

import httpx

DEFAULT_SLUG = "petition-sony-to-develop-destiny-3"
BASE_URL = "https://www.change.org/p/{slug}"

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/124.0.0.0 Safari/537.36"
)

HEADERS = {
    "User-Agent": USER_AGENT,
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "Accept-Encoding": "gzip, deflate, br",
    "Connection": "keep-alive",
    "Upgrade-Insecure-Requests": "1",
}

SIGNATURE_RE = re.compile(
    r'"signatureCount"\s*:\s*\{'
    r'\s*"displayed"\s*:\s*(?P<displayed>\d+)\s*,'
    r'\s*"total"\s*:\s*(?P<total>\d+)\s*,'
    r'\s*"goal"\s*:\s*(?P<goal>\d+)'
)
WEEKLY_RE = re.compile(r'"weeklySignatureCount"\s*:\s*(?P<weekly>\d+)')


class ScrapeError(RuntimeError):
    """Raised when the petition page can't be fetched or parsed."""


def _parse(html: str) -> dict:
    m = SIGNATURE_RE.search(html)
    if not m:
        raise ScrapeError(
            "signatureCount block not found in HTML — page structure may have changed"
        )
    weekly_match = WEEKLY_RE.search(html)
    weekly: Optional[int] = int(weekly_match.group("weekly")) if weekly_match else None
    return {
        "displayed": int(m.group("displayed")),
        "total": int(m.group("total")),
        "goal": int(m.group("goal")),
        "weekly": weekly,
    }


def fetch_counts(
    slug: str = DEFAULT_SLUG,
    *,
    max_retries: int = 3,
    timeout: float = 20.0,
) -> dict:
    """Fetch current signature counts. Returns dict with timestamp + counts."""
    url = BASE_URL.format(slug=slug)
    last_exc: Optional[Exception] = None

    for attempt in range(max_retries):
        try:
            with httpx.Client(
                headers=HEADERS,
                timeout=timeout,
                follow_redirects=True,
                http2=False,
            ) as client:
                resp = client.get(url)
            if resp.status_code in (403, 503, 429):
                raise ScrapeError(f"HTTP {resp.status_code} for {url}")
            resp.raise_for_status()
            parsed = _parse(resp.text)
        except (httpx.HTTPError, ScrapeError) as exc:
            last_exc = exc
            if attempt < max_retries - 1:
                sleep_s = 2 ** attempt
                time.sleep(sleep_s)
            continue
        else:
            return {
                "timestamp": datetime.now(timezone.utc).isoformat(),
                **parsed,
            }

    raise ScrapeError(f"Failed after {max_retries} attempts: {last_exc}")


if __name__ == "__main__":
    import json
    print(json.dumps(fetch_counts(), indent=2))
