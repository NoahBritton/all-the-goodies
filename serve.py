"""Lightweight HTTP server that hosts the dashboard and a manual /refresh endpoint."""
from __future__ import annotations

import argparse
import json
import logging
import os
import threading
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from db import DB_PATH, connect, insert_reading, latest_total
from news import refresh_news
from render_html import render as render_html
from scraper import DEFAULT_SLUG, ScrapeError, fetch_counts

NEWS_COOLDOWN_S = 300

ROOT = Path(__file__).parent
HTML_PATH = ROOT / "dashboard.html"

_refresh_lock = threading.Lock()


def do_refresh(slug: str) -> dict:
    """Fetch once, insert if new, refresh news (cooldown-gated), re-render HTML."""
    with _refresh_lock:
        reading = fetch_counts(slug)
        conn = connect()
        try:
            prev_total = latest_total(conn)
            total = reading["total"]
            if prev_total is not None and total == prev_total:
                status = "unchanged"
            else:
                insert_reading(conn, reading)
                status = "inserted"
        finally:
            conn.close()
        try:
            news_result = refresh_news(min_interval_seconds=NEWS_COOLDOWN_S)
            logging.info("news refresh: %s", news_result)
        except Exception as exc:
            logging.warning("news refresh failed: %s", exc)
        render_html(HTML_PATH, DB_PATH)
        return {
            "status": status,
            "total": reading["total"],
            "delta": (reading["total"] - prev_total) if prev_total is not None else None,
            "timestamp": reading["timestamp"],
        }


def make_handler(slug: str):
    class Handler(SimpleHTTPRequestHandler):
        def __init__(self, *args, **kw):
            super().__init__(*args, directory=str(ROOT), **kw)

        def log_message(self, fmt, *args):
            logging.info("%s - %s", self.address_string(), fmt % args)

        def do_GET(self):
            if self.path in ("/", "/index.html"):
                self.path = "/dashboard.html"
            return super().do_GET()

        def do_POST(self):
            if self.path != "/refresh":
                self.send_error(404)
                return
            try:
                result = do_refresh(slug)
                body = json.dumps(result).encode("utf-8")
                self.send_response(200)
            except ScrapeError as exc:
                body = json.dumps({"status": "error", "error": str(exc)}).encode("utf-8")
                self.send_response(502)
            except Exception as exc:
                logging.exception("refresh failed")
                body = json.dumps({"status": "error", "error": str(exc)}).encode("utf-8")
                self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

    return Handler


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--host", default="127.0.0.1")
    p.add_argument("--port", type=int, default=8765)
    p.add_argument("--slug", default=os.environ.get("PETITION_SLUG", DEFAULT_SLUG))
    args = p.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

    if not DB_PATH.exists():
        raise SystemExit(f"No database found at {DB_PATH}. Run poll.py or backfill.py first.")
    logging.info("Rendering fresh dashboard.html on startup")
    render_html(HTML_PATH, DB_PATH)

    server = ThreadingHTTPServer((args.host, args.port), make_handler(args.slug))
    url = f"http://{args.host}:{args.port}/"
    logging.info("Serving dashboard at %s  (POST /refresh to trigger a fresh scrape)", url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        logging.info("Shutting down")
        server.server_close()


if __name__ == "__main__":
    main()
