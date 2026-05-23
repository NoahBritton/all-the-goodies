"""Aggregate Destiny-3-related news from RSS feeds and Reddit JSON endpoints.

Filters items by keyword on title (+ description for RSS) and stores hits in
the `news_items` table. Designed to be called periodically by serve.py /
poll.py with a cooldown so we don't pound public feeds.
"""
from __future__ import annotations

import logging
import re
import sqlite3
import time
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from typing import Iterable, Optional
from xml.etree import ElementTree as ET

import httpx

from db import connect

UA = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 "
    "petition-tracker/0.1"
)

# RSS feeds — title + description are matched against KEYWORDS.
RSS_FEEDS = [
    ("IGN", "https://feeds.feedburner.com/ign/games-all"),
    ("Kotaku", "https://kotaku.com/rss"),
    ("Polygon", "https://www.polygon.com/rss/index.xml"),
    ("GameRant", "https://gamerant.com/feed/"),
    ("PC Gamer", "https://www.pcgamer.com/rss/"),
    ("Eurogamer", "https://www.eurogamer.net/?format=rss"),
    ("Push Square", "https://www.pushsquare.com/feeds/latest"),
    ("Dexerto", "https://www.dexerto.com/feed/"),
    ("VGC", "https://www.videogameschronicle.com/feed/"),
    ("The Verge", "https://www.theverge.com/rss/index.xml"),
]

# Reddit subreddits — searched via /new.json, filtered by title against KEYWORDS.
REDDIT_SUBS = [
    "DestinyTheGame",
    "destiny2",
    "Games",
    "Bungie",
    "patientgamers",
]

# Keyword groups: an item matches if ANY group matches.
# A group matches if ALL its keywords appear in the haystack (case-insensitive).
KEYWORD_GROUPS = [
    ["destiny 3"],
    ["destiny iii"],
    ["destiny three"],
    ["destiny", "sequel"],
    ["destiny", "petition"],
    ["petition", "sony"],
    ["bungie", "sony"],
    ["sony", "bungie"],
    ["destiny", "future of"],
    ["next destiny"],
    ["bungie", "layoffs"],
    ["bungie", "marathon"],
]

EXCLUDE_PATTERNS = [
    r"\bdestiny\s*2\b",  # don't surface generic D2 news unless another keyword also hit
]


def _matches(text: str) -> bool:
    t = text.lower()
    for group in KEYWORD_GROUPS:
        if all(kw in t for kw in group):
            return True
    return False


def _is_excluded_only(text: str) -> bool:
    """If text matches our exclude patterns AND has no other strong signal."""
    t = text.lower()
    if any(re.search(p, t) for p in EXCLUDE_PATTERNS):
        # excluded unless one of the petition-/d3-specific groups matches
        strong = [
            ["destiny 3"], ["destiny iii"], ["destiny three"],
            ["petition", "sony"], ["destiny", "petition"],
            ["bungie", "sony"], ["sony", "bungie"],
        ]
        return not any(all(kw in t for kw in g) for g in strong)
    return False


def _parse_rss(xml_text: str, source: str) -> list[dict]:
    items: list[dict] = []
    try:
        root = ET.fromstring(xml_text)
    except ET.ParseError as exc:
        logging.warning("RSS parse failed for %s: %s", source, exc)
        return items

    # Handle both RSS 2.0 (<rss><channel><item>) and Atom (<feed><entry>)
    ns_atom = "{http://www.w3.org/2005/Atom}"
    rss_items = root.findall(".//item")
    atom_items = root.findall(f".//{ns_atom}entry")

    for item in rss_items:
        title_el = item.find("title")
        link_el = item.find("link")
        desc_el = item.find("description")
        pub_el = item.find("pubDate")
        guid_el = item.find("guid")
        if title_el is None or link_el is None:
            continue
        title = (title_el.text or "").strip()
        link = (link_el.text or "").strip()
        desc = (desc_el.text or "").strip() if desc_el is not None else ""
        guid = (guid_el.text or link).strip() if guid_el is not None else link
        ts = _parse_rss_date(pub_el.text if pub_el is not None else None)
        items.append({
            "id": f"{source}:{guid}",
            "source": source,
            "title": title,
            "url": link,
            "published": ts,
            "snippet": _strip_html(desc)[:300],
        })

    for entry in atom_items:
        title_el = entry.find(f"{ns_atom}title")
        link_el = entry.find(f"{ns_atom}link")
        summary_el = entry.find(f"{ns_atom}summary")
        updated_el = entry.find(f"{ns_atom}updated")
        if updated_el is None:
            updated_el = entry.find(f"{ns_atom}published")
        if title_el is None or link_el is None:
            continue
        title = (title_el.text or "").strip()
        link = link_el.get("href", "").strip()
        summary = (summary_el.text or "").strip() if summary_el is not None else ""
        ts = _parse_iso_date(updated_el.text if updated_el is not None else None)
        items.append({
            "id": f"{source}:{link}",
            "source": source,
            "title": title,
            "url": link,
            "published": ts,
            "snippet": _strip_html(summary)[:300],
        })
    return items


def _parse_reddit_json(payload: dict, sub: str) -> list[dict]:
    items: list[dict] = []
    for child in payload.get("data", {}).get("children", []):
        d = child.get("data", {})
        title = d.get("title", "")
        if not title:
            continue
        permalink = d.get("permalink", "")
        url = f"https://www.reddit.com{permalink}" if permalink else d.get("url", "")
        created = d.get("created_utc")
        ts = datetime.fromtimestamp(created, tz=timezone.utc).isoformat() if created else _now_iso()
        items.append({
            "id": f"reddit:{d.get('id', url)}",
            "source": f"r/{sub}",
            "title": title,
            "url": url,
            "published": ts,
            "snippet": (d.get("selftext") or "")[:300],
        })
    return items


def _strip_html(s: str) -> str:
    return re.sub(r"<[^>]+>", "", s).strip()


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _parse_rss_date(raw: Optional[str]) -> str:
    if not raw:
        return _now_iso()
    try:
        return parsedate_to_datetime(raw).astimezone(timezone.utc).isoformat()
    except (TypeError, ValueError):
        return _now_iso()


def _parse_iso_date(raw: Optional[str]) -> str:
    if not raw:
        return _now_iso()
    try:
        s = raw.replace("Z", "+00:00")
        return datetime.fromisoformat(s).astimezone(timezone.utc).isoformat()
    except ValueError:
        return _now_iso()


def fetch_sources(timeout: float = 10.0) -> list[dict]:
    items: list[dict] = []
    with httpx.Client(headers={"User-Agent": UA}, timeout=timeout, follow_redirects=True) as client:
        for source, url in RSS_FEEDS:
            try:
                r = client.get(url)
                r.raise_for_status()
                items.extend(_parse_rss(r.text, source))
            except (httpx.HTTPError, Exception) as exc:
                logging.warning("RSS fetch failed for %s: %s", source, exc)
        for sub in REDDIT_SUBS:
            try:
                r = client.get(f"https://www.reddit.com/r/{sub}/new.json?limit=50")
                if r.status_code != 200:
                    logging.warning("Reddit %s returned %d", sub, r.status_code)
                    continue
                items.extend(_parse_reddit_json(r.json(), sub))
            except Exception as exc:
                logging.warning("Reddit fetch failed for %s: %s", sub, exc)
            time.sleep(0.5)  # be polite to reddit
    return items


def filter_items(items: Iterable[dict]) -> list[dict]:
    out = []
    for it in items:
        haystack = f"{it['title']} {it.get('snippet', '')}"
        if not _matches(haystack):
            continue
        if _is_excluded_only(haystack):
            continue
        out.append(it)
    return out


def store_items(conn: sqlite3.Connection, items: Iterable[dict]) -> int:
    n = 0
    now = _now_iso()
    for it in items:
        try:
            conn.execute(
                "INSERT INTO news_items (id, source, title, url, published, fetched_at, snippet) "
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                (it["id"], it["source"], it["title"], it["url"],
                 it["published"], now, it.get("snippet", "")),
            )
            n += 1
        except sqlite3.IntegrityError:
            pass
    conn.commit()
    return n


def get_last_news_fetch(conn: sqlite3.Connection) -> Optional[datetime]:
    row = conn.execute("SELECT value FROM meta WHERE key='news_last_fetch'").fetchone()
    if not row:
        return None
    try:
        return datetime.fromisoformat(row[0])
    except ValueError:
        return None


def set_last_news_fetch(conn: sqlite3.Connection, dt: datetime) -> None:
    conn.execute(
        "INSERT OR REPLACE INTO meta (key, value) VALUES ('news_last_fetch', ?)",
        (dt.isoformat(),),
    )
    conn.commit()


def refresh_news(min_interval_seconds: int = 300) -> dict:
    """Fetch + filter + store news, but only if the last fetch was more than
    min_interval_seconds ago. Returns a status dict."""
    conn = connect()
    try:
        last = get_last_news_fetch(conn)
        now = datetime.now(timezone.utc)
        if last is not None:
            elapsed = (now - last).total_seconds()
            if elapsed < min_interval_seconds:
                return {"status": "cooldown", "next_in_s": int(min_interval_seconds - elapsed)}
        items = fetch_sources()
        matched = filter_items(items)
        inserted = store_items(conn, matched)
        set_last_news_fetch(conn, now)
        return {
            "status": "ok",
            "fetched": len(items),
            "matched": len(matched),
            "inserted": inserted,
        }
    finally:
        conn.close()


def recent_news(conn: sqlite3.Connection, limit: int = 12) -> list[dict]:
    rows = conn.execute(
        "SELECT source, title, url, published FROM news_items "
        "ORDER BY published DESC LIMIT ?",
        (limit,),
    ).fetchall()
    return [
        {"source": s, "title": t, "url": u, "published": p}
        for (s, t, u, p) in rows
    ]


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    print(refresh_news(min_interval_seconds=0))
