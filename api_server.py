"""JSON API for the petition tracker, designed to run on a VPS.

Background thread polls Change.org + news on a fixed cadence and writes to
SQLite. HTTP server exposes:

  GET  /api/state.json    — current readings + news, with CORS for the frontend
  GET  /healthz           — liveness probe (200 OK)

The frontend at https://axogames.dev/d3-petition-tracker/ fetches /api/state.json
and renders the dashboard client-side.
"""
from __future__ import annotations

import argparse
import json
import logging
import os
import signal
import sqlite3
import sys
import threading
import time
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from db import DB_PATH, connect, insert_reading, latest_total
from news import refresh_news
from scraper import DEFAULT_SLUG, ScrapeError, fetch_counts

DEFAULT_POLL_INTERVAL_S = 60
DEFAULT_NEWS_INTERVAL_S = 300
DEFAULT_ALLOWED_ORIGINS = [
    "https://axogames.dev",
    "https://www.axogames.dev",
    "http://localhost:8080",  # for local frontend dev
    "http://127.0.0.1:8080",
]

_state_lock = threading.Lock()
_stop = threading.Event()


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


# ---------- Background poller ----------

def poll_once(slug: str) -> dict | None:
    """One scrape + insert. Returns the reading dict, or None on failure."""
    try:
        reading = fetch_counts(slug)
    except ScrapeError as exc:
        logging.warning("scrape failed: %s", exc)
        return None
    with _state_lock:
        conn = connect()
        try:
            prev = latest_total(conn)
            if prev != reading["total"]:
                insert_reading(conn, reading)
                logging.info(
                    "inserted total=%s delta=%+d",
                    reading["total"], reading["total"] - (prev or 0),
                )
            else:
                logging.debug("total unchanged: %s", reading["total"])
        finally:
            conn.close()
    return reading


def poller_loop(slug: str, interval_s: int, news_interval_s: int) -> None:
    logging.info(
        "poller started: petition slug=%s, every %ds (news every %ds)",
        slug, interval_s, news_interval_s,
    )
    last_news_attempt = 0.0
    while not _stop.is_set():
        poll_once(slug)
        now = time.time()
        if now - last_news_attempt >= news_interval_s:
            try:
                result = refresh_news(min_interval_seconds=0)
                logging.info("news refresh: %s", result)
            except Exception as exc:
                logging.warning("news refresh failed: %s", exc)
            last_news_attempt = now
        # Sleep in chunks so shutdown is responsive
        for _ in range(interval_s):
            if _stop.is_set():
                break
            time.sleep(1)
    logging.info("poller stopped")


# ---------- State snapshot ----------

def _load_state() -> dict:
    """Build the state payload returned by /api/state.json."""
    conn = connect()
    try:
        readings = conn.execute(
            "SELECT ts, displayed, total, goal, weekly "
            "FROM readings ORDER BY ts ASC"
        ).fetchall()
        news = conn.execute(
            "SELECT source, title, url, published "
            "FROM news_items ORDER BY published DESC LIMIT 40"
        ).fetchall()
    finally:
        conn.close()

    # Strip leading zero-total readings (the launch ramp noise)
    pruned = []
    seen_nonzero = False
    for ts, displayed, total, goal, weekly in readings:
        if not seen_nonzero and (total or 0) <= 0:
            continue
        seen_nonzero = True
        pruned.append({
            "ts": ts, "displayed": displayed, "total": total,
            "goal": goal, "weekly": weekly,
        })

    return {
        "generated_at": _now_iso(),
        "petition": {
            "url": "https://www.change.org/p/petition-sony-to-develop-destiny-3",
            "slug": DEFAULT_SLUG,
        },
        "readings": pruned,
        "news": [
            {"source": s, "title": t, "url": u, "published": p}
            for (s, t, u, p) in news
        ],
    }


# ---------- HTTP server ----------

class APIHandler(BaseHTTPRequestHandler):
    server_version = "PetitionTracker/0.1"

    # Set per-process by main()
    allowed_origins: list[str] = []

    def log_message(self, fmt, *args):
        logging.info("%s - %s", self.address_string(), fmt % args)

    def _origin_allowed(self) -> str | None:
        origin = self.headers.get("Origin", "")
        if origin in self.allowed_origins:
            return origin
        return None

    def _cors_headers(self) -> None:
        origin = self._origin_allowed()
        if origin:
            self.send_header("Access-Control-Allow-Origin", origin)
            self.send_header("Vary", "Origin")
            self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
            self.send_header("Access-Control-Allow-Headers", "Content-Type")
            self.send_header("Access-Control-Max-Age", "600")

    def do_OPTIONS(self):
        self.send_response(204)
        self._cors_headers()
        self.send_header("Content-Length", "0")
        self.end_headers()

    def do_GET(self):
        if self.path == "/healthz":
            return self._send(200, b'{"ok":true}', "application/json")
        if self.path == "/api/state.json":
            try:
                payload = json.dumps(_load_state()).encode("utf-8")
            except Exception:
                logging.exception("state build failed")
                return self._send(500, b'{"error":"state_build_failed"}', "application/json")
            return self._send(200, payload, "application/json", cache_seconds=5)
        if self.path == "/":
            return self._send(
                200,
                b"petition-tracker API. See /api/state.json or /healthz.\n",
                "text/plain",
            )
        return self._send(404, b'{"error":"not_found"}', "application/json")

    def _send(self, status: int, body: bytes, content_type: str, cache_seconds: int = 0) -> None:
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        if cache_seconds > 0:
            self.send_header("Cache-Control", f"public, max-age={cache_seconds}")
        else:
            self.send_header("Cache-Control", "no-store")
        self._cors_headers()
        self.end_headers()
        self.wfile.write(body)


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--host", default=os.environ.get("HOST", "127.0.0.1"))
    p.add_argument("--port", type=int, default=int(os.environ.get("PORT", "8765")))
    p.add_argument("--slug", default=os.environ.get("PETITION_SLUG", DEFAULT_SLUG))
    p.add_argument("--poll-interval", type=int,
                   default=int(os.environ.get("POLL_INTERVAL_S", str(DEFAULT_POLL_INTERVAL_S))))
    p.add_argument("--news-interval", type=int,
                   default=int(os.environ.get("NEWS_INTERVAL_S", str(DEFAULT_NEWS_INTERVAL_S))))
    p.add_argument("--allowed-origins", default=os.environ.get("ALLOWED_ORIGINS", ""),
                   help="Comma-separated list of allowed CORS origins (overrides defaults)")
    args = p.parse_args()

    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(message)s",
        stream=sys.stdout,
    )

    APIHandler.allowed_origins = (
        [o.strip() for o in args.allowed_origins.split(",") if o.strip()]
        if args.allowed_origins
        else list(DEFAULT_ALLOWED_ORIGINS)
    )
    logging.info("CORS allowed origins: %s", APIHandler.allowed_origins)

    # Ensure the DB exists (and creates news_items table on first start)
    connect().close()

    poller = threading.Thread(
        target=poller_loop,
        args=(args.slug, args.poll_interval, args.news_interval),
        name="poller",
        daemon=True,
    )
    poller.start()

    httpd = ThreadingHTTPServer((args.host, args.port), APIHandler)
    logging.info("listening on http://%s:%d", args.host, args.port)

    def _shutdown(signum, frame):
        logging.info("shutdown signal %d", signum)
        _stop.set()
        httpd.shutdown()

    signal.signal(signal.SIGTERM, _shutdown)
    signal.signal(signal.SIGINT, _shutdown)

    try:
        httpd.serve_forever()
    finally:
        _stop.set()
        httpd.server_close()
        poller.join(timeout=5)
        logging.info("bye")


if __name__ == "__main__":
    main()
