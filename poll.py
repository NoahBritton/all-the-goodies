"""Poll the petition page on an interval and persist readings to SQLite."""
from __future__ import annotations

import argparse
import logging
import os
import signal
import sys
import time
from pathlib import Path

from db import DB_PATH, connect, insert_reading, latest_total
from render_html import render as render_html
from scraper import DEFAULT_SLUG, ScrapeError, fetch_counts

LOG_PATH = Path(__file__).parent / "poll.log"

_stop = False


def _handle_sigint(signum, frame):
    global _stop
    _stop = True
    logging.info("Stop signal received — finishing current loop")


def setup_logging() -> None:
    fmt = "%(asctime)s %(levelname)s %(message)s"
    logging.basicConfig(
        level=logging.INFO,
        format=fmt,
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler(LOG_PATH, encoding="utf-8"),
        ],
    )


def poll_loop(slug: str, interval: int, html_out: Path | None) -> None:
    conn = connect()
    logging.info("Polling %s every %ds (db=signatures.db)", slug, interval)
    if html_out:
        logging.info("Will refresh dashboard at %s after each poll", html_out)

    while not _stop:
        try:
            reading = fetch_counts(slug)
            prev_total = latest_total(conn)
            total = reading["total"]

            if prev_total is not None and total == prev_total:
                logging.info("total=%s unchanged — skip insert", total)
            else:
                inserted = insert_reading(conn, reading)
                if inserted:
                    if prev_total is not None and total < prev_total:
                        logging.warning(
                            "count DECREASED: %s -> %s (%+d)",
                            prev_total, total, total - prev_total,
                        )
                    else:
                        delta = total - prev_total if prev_total is not None else 0
                        logging.info(
                            "inserted total=%s (%+d) displayed=%s weekly=%s",
                            total, delta, reading.get("displayed"), reading.get("weekly"),
                        )
                else:
                    logging.info("duplicate timestamp — not inserted")
            if html_out:
                try:
                    render_html(html_out, DB_PATH)
                except Exception as exc:
                    logging.warning("html render failed: %s", exc)
        except ScrapeError as exc:
            logging.error("scrape error: %s", exc)
        except Exception as exc:
            logging.exception("unexpected error: %s", exc)

        # Sleep in small chunks so Ctrl-C is responsive
        for _ in range(interval):
            if _stop:
                break
            time.sleep(1)

    conn.close()
    logging.info("Poller stopped cleanly")


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument(
        "--interval",
        type=int,
        default=int(os.environ.get("POLL_INTERVAL", "300")),
        help="Seconds between polls (default: 300)",
    )
    p.add_argument(
        "--slug",
        default=os.environ.get("PETITION_SLUG", DEFAULT_SLUG),
        help="Change.org petition slug (after /p/)",
    )
    p.add_argument(
        "--html",
        type=Path,
        default=None,
        help="Path to refresh styled HTML dashboard after each poll (e.g. dashboard.html)",
    )
    args = p.parse_args()

    setup_logging()
    signal.signal(signal.SIGINT, _handle_sigint)
    signal.signal(signal.SIGTERM, _handle_sigint)
    poll_loop(args.slug, args.interval, args.html)


if __name__ == "__main__":
    main()
