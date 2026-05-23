"""SQLite helpers shared across poll/graph/backfill."""
from __future__ import annotations

import sqlite3
from pathlib import Path
from typing import Iterable, Optional

DB_PATH = Path(__file__).parent / "signatures.db"

SCHEMA = """
CREATE TABLE IF NOT EXISTS readings (
    ts        TEXT PRIMARY KEY,
    displayed INTEGER,
    total     INTEGER NOT NULL,
    goal      INTEGER,
    weekly    INTEGER
);
CREATE INDEX IF NOT EXISTS idx_readings_total ON readings(total);

CREATE TABLE IF NOT EXISTS news_items (
    id          TEXT PRIMARY KEY,
    source      TEXT NOT NULL,
    title       TEXT NOT NULL,
    url         TEXT NOT NULL,
    published   TEXT NOT NULL,
    fetched_at  TEXT NOT NULL,
    snippet     TEXT
);
CREATE INDEX IF NOT EXISTS idx_news_published ON news_items(published);

CREATE TABLE IF NOT EXISTS meta (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
"""


def connect(path: Path = DB_PATH) -> sqlite3.Connection:
    conn = sqlite3.connect(path)
    conn.executescript(SCHEMA)
    return conn


def latest_total(conn: sqlite3.Connection) -> Optional[int]:
    row = conn.execute(
        "SELECT total FROM readings ORDER BY ts DESC LIMIT 1"
    ).fetchone()
    return row[0] if row else None


def insert_reading(conn: sqlite3.Connection, reading: dict) -> bool:
    """Insert a reading row. Returns True if inserted, False if duplicate ts."""
    try:
        conn.execute(
            "INSERT INTO readings (ts, displayed, total, goal, weekly) "
            "VALUES (?, ?, ?, ?, ?)",
            (
                reading["timestamp"] if "timestamp" in reading else reading["ts"],
                reading.get("displayed"),
                reading["total"],
                reading.get("goal"),
                reading.get("weekly"),
            ),
        )
        conn.commit()
        return True
    except sqlite3.IntegrityError:
        return False


def insert_many(conn: sqlite3.Connection, rows: Iterable[dict]) -> int:
    n = 0
    for r in rows:
        if insert_reading(conn, r):
            n += 1
    return n
