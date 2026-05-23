"""Backfill historic readings into signatures.db from a CSV file.

Expected CSV columns (header row required):
    ts,total[,displayed,goal,weekly]

`ts` may be any ISO 8601 timestamp (with or without timezone). Rows with
a `ts` already present in the DB are skipped silently.
"""
from __future__ import annotations

import argparse
import csv
from datetime import datetime, timezone
from pathlib import Path

from db import connect, insert_many


def _normalize_ts(raw: str) -> str:
    raw = raw.strip()
    # accept "Z" suffix
    if raw.endswith("Z"):
        raw = raw[:-1] + "+00:00"
    try:
        dt = datetime.fromisoformat(raw)
    except ValueError as exc:
        raise ValueError(f"unparseable timestamp: {raw!r}") from exc
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc).isoformat()


def load_csv(path: Path) -> list[dict]:
    rows: list[dict] = []
    with path.open("r", encoding="utf-8", newline="") as fh:
        reader = csv.DictReader(fh)
        if "ts" not in reader.fieldnames or "total" not in reader.fieldnames:
            raise SystemExit(f"CSV must have at least 'ts' and 'total' columns; got {reader.fieldnames}")
        for i, raw in enumerate(reader, start=2):
            try:
                row = {
                    "ts": _normalize_ts(raw["ts"]),
                    "total": int(raw["total"]),
                    "displayed": int(raw["displayed"]) if raw.get("displayed") else None,
                    "goal": int(raw["goal"]) if raw.get("goal") else None,
                    "weekly": int(raw["weekly"]) if raw.get("weekly") else None,
                }
            except (KeyError, ValueError) as exc:
                raise SystemExit(f"line {i}: {exc}")
            rows.append(row)
    return rows


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("csv_path", type=Path, help="Path to historic CSV")
    args = p.parse_args()

    rows = load_csv(args.csv_path)
    conn = connect()
    inserted = insert_many(conn, rows)
    conn.close()
    print(f"Loaded {len(rows)} rows from {args.csv_path}; inserted {inserted} new (skipped {len(rows) - inserted} duplicates).")


if __name__ == "__main__":
    main()
