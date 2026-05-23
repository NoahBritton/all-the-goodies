"""Render the signature-count time series, statically or as a live dashboard."""
from __future__ import annotations

import argparse
import csv
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import List, Tuple

from db import DB_PATH, connect
from render_html import render as render_html


def load_readings(conn: sqlite3.Connection) -> List[Tuple[datetime, int, int]]:
    rows = conn.execute(
        "SELECT ts, total, goal FROM readings ORDER BY ts ASC"
    ).fetchall()
    out = []
    for ts, total, goal in rows:
        try:
            t = datetime.fromisoformat(ts)
        except ValueError:
            continue
        out.append((t, total, goal))
    return out


def compute_rate_per_hour(readings: List[Tuple[datetime, int, int]]):
    """Implied signing rate (sigs/hour) between consecutive readings."""
    times, rates = [], []
    for (t0, n0, _), (t1, n1, _) in zip(readings, readings[1:]):
        dt_h = (t1 - t0).total_seconds() / 3600.0
        if dt_h <= 0:
            continue
        rates.append((n1 - n0) / dt_h)
        times.append(t1)
    return times, rates


def render_static(out_path: Path) -> None:
    import matplotlib.pyplot as plt

    conn = connect()
    readings = load_readings(conn)
    conn.close()
    if not readings:
        raise SystemExit("No readings in database yet. Run poll.py first.")

    times = [r[0] for r in readings]
    totals = [r[1] for r in readings]
    goal = readings[-1][2]

    rate_times, rates = compute_rate_per_hour(readings)

    fig, ax1 = plt.subplots(figsize=(12, 6))
    ax1.plot(times, totals, color="tab:blue", label="Total signatures", linewidth=2)
    if goal:
        ax1.axhline(goal, linestyle="--", color="gray", alpha=0.7, label=f"Goal ({goal:,})")
    ax1.set_xlabel("Time (UTC)")
    ax1.set_ylabel("Total signatures", color="tab:blue")
    ax1.tick_params(axis="y", labelcolor="tab:blue")
    ax1.grid(True, alpha=0.3)

    if rates:
        ax2 = ax1.twinx()
        ax2.plot(rate_times, rates, color="tab:orange", label="Signing rate (per hour)", alpha=0.7)
        ax2.set_ylabel("Signatures / hour", color="tab:orange")
        ax2.tick_params(axis="y", labelcolor="tab:orange")

    fig.autofmt_xdate()
    fig.suptitle("Change.org petition signature trend")
    fig.tight_layout()
    fig.savefig(out_path, dpi=140)
    print(f"Saved {out_path} ({len(readings)} readings)")


def write_html(out_path: Path, static_mode: bool = False) -> None:
    render_html(out_path, DB_PATH, static_mode=static_mode)
    mode = "static (5-min meta-refresh)" if static_mode else "live (serve.py)"
    print(f"Wrote dashboard to {out_path} — {mode}")


def export_csv(out_path: Path) -> None:
    conn = connect()
    rows = conn.execute(
        "SELECT ts, displayed, total, goal, weekly FROM readings ORDER BY ts ASC"
    ).fetchall()
    conn.close()
    with out_path.open("w", newline="", encoding="utf-8") as fh:
        w = csv.writer(fh)
        w.writerow(["ts", "displayed", "total", "goal", "weekly"])
        w.writerows(rows)
    print(f"Exported {len(rows)} rows to {out_path}")


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    g = p.add_mutually_exclusive_group(required=True)
    g.add_argument("--save", metavar="PATH", help="Write a static PNG to PATH")
    g.add_argument("--html", metavar="PATH", help="Write a styled standalone HTML dashboard")
    g.add_argument("--export", metavar="PATH", help="Export raw readings to CSV at PATH")
    p.add_argument("--static", action="store_true", help="With --html: produce a GH-Pages-safe page (no manual button, meta-refresh)")
    args = p.parse_args()

    if not DB_PATH.exists():
        raise SystemExit(f"No database found at {DB_PATH}. Run poll.py first.")

    if args.save:
        render_static(Path(args.save))
    elif args.html:
        write_html(Path(args.html), static_mode=args.static)
    elif args.export:
        export_csv(Path(args.export))


if __name__ == "__main__":
    main()
