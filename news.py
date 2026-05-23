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
# A group matches if ALL its keywords appear in the TITLE (case-insensitive).
# Snippet/description is intentionally NOT matched — too noisy (e.g. a generic
# RSS digest with "...Bungie..." buried in the body would surface unrelated stuff).
KEYWORD_GROUPS = [
    # Strong: any of these phrases in the title is a hit
    ["destiny 3"],
    ["destiny iii"],
    ["destiny three"],
    ["d3 petition"],
    ["next destiny"],
    ["future destiny"],
    # Medium: pairs that together strongly imply on-topic
    ["destiny", "sequel"],
    ["destiny", "petition"],
    ["destiny", "future of"],
    ["bungie", "layoffs"],
    ["bungie", "marathon"],  # Bungie's other game, often discussed alongside D3 plans
    ["bungie", "sony"],
    ["sony", "destiny"],
    ["petition", "sony", "bungie"],  # require all three
]

# If title matches ANY of these patterns, drop the item.
# Use to kill known false positives that creep in.
EXCLUDE_REGEX = [
    r"\bsteam\s+controller\b",
    r"\bsteam\s+deck\b",
    r"\bplaystation\s+portal\b",
    r"\bdestiny\s*2\b",       # generic D2 patch notes etc.
    r"\bmarathon\s+(review|beta|alpha)\b",  # Marathon-specific without D3 angle
    r"\bdestiny\s+rising\b",  # mobile game
]


def _matches(title: str) -> bool:
    t = title.lower()
    for group in KEYWORD_GROUPS:
        if all(kw in t for kw in group):
            return True
    return False


def _is_excluded(title: str) -> bool:
    """Drop the item if its title matches a known-noise pattern, UNLESS it
    also has a high-confidence Destiny-3-specific phrase."""
    t = title.lower()
    if not any(re.search(p, t) for p in EXCLUDE_REGEX):
        return False
    strong = ["destiny 3", "destiny iii", "destiny three", "d3 petition", "destiny petition"]
    return not any(s in t for s in strong)


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
        title = it["title"]
        if not _matches(title):
            continue
        if _is_excluded(title):
            continue
        out.append(it)
    return out


def purge_excluded(conn: sqlite3.Connection) -> int:
    """Drop already-stored items whose titles now match the exclusion list.
    Useful after tightening the filter — sweeps out historical false positives."""
    n_before = conn.execute("SELECT COUNT(*) FROM news_items").fetchone()[0]
    cursor = conn.execute("SELECT id, title FROM news_items")
    bad_ids = []
    for row_id, title in cursor.fetchall():
        if not _matches(title) or _is_excluded(title):
            bad_ids.append(row_id)
    for bid in bad_ids:
        conn.execute("DELETE FROM news_items WHERE id = ?", (bid,))
    conn.commit()
    n_after = conn.execute("SELECT COUNT(*) FROM news_items").fetchone()[0]
    return n_before - n_after


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
