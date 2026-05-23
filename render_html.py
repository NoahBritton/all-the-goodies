"""Render a styled, self-contained HTML dashboard from signatures.db."""
from __future__ import annotations

import json
import sqlite3
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import List, Tuple
from zoneinfo import ZoneInfo

ET = ZoneInfo("America/New_York")


def _to_et(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(ET)


def _fmt_et(dt: datetime, with_date: bool = True, with_tz: bool = True) -> str:
    """Format a datetime as Eastern 12-hour time, e.g. 'May 23, 1:21am EDT'."""
    t = _to_et(dt)
    hour = t.hour % 12 or 12
    ampm = "am" if t.hour < 12 else "pm"
    body = f"{hour}:{t.minute:02d}{ampm}"
    if with_date:
        body = f"{t.strftime('%b %d')}, {body}"
    if with_tz:
        body = f"{body} {t.strftime('%Z')}"
    return body


def _load(conn: sqlite3.Connection) -> List[Tuple[datetime, int, int]]:
    """Load readings, dropping any leading zero-totals (visualization noise:
    a single '0 at petition launch' reading drags the line across hours of
    blank time before real polling started)."""
    rows = conn.execute(
        "SELECT ts, total, goal FROM readings ORDER BY ts ASC"
    ).fetchall()
    out: List[Tuple[datetime, int, int]] = []
    seen_nonzero = False
    for ts, total, goal in rows:
        try:
            t = datetime.fromisoformat(ts)
        except ValueError:
            continue
        if t.tzinfo is None:
            t = t.replace(tzinfo=timezone.utc)
        if not seen_nonzero and total <= 0:
            continue
        seen_nonzero = True
        out.append((t, total, goal))
    return out


def _hourly_rate(readings: List[Tuple[datetime, int, int]]):
    """Bucket readings by hour (Eastern) and compute sigs gained per bucket."""
    if len(readings) < 2:
        return [], []
    by_hour: dict[datetime, Tuple[int, int]] = {}
    for t, total, _ in readings:
        t_et = _to_et(t).replace(tzinfo=None)
        hour = t_et.replace(minute=0, second=0, microsecond=0)
        first, last = by_hour.get(hour, (total, total))
        by_hour[hour] = (min(first, total), max(last, total))

    hours = sorted(by_hour.keys())
    if len(hours) < 2:
        return [], []

    # delta within each bucket vs the previous bucket's last
    out_times, out_rates = [], []
    prev_last = by_hour[hours[0]][0]
    for h in hours:
        _first, last = by_hour[h]
        out_times.append(h)
        out_rates.append(max(0, last - prev_last))
        prev_last = last
    # drop the first one (no prior bucket to compare against)
    return out_times[1:], out_rates[1:]


def _stats(readings: List[Tuple[datetime, int, int]]) -> dict:
    latest_t, latest_total, goal = readings[-1]
    first_t, first_total, _ = readings[0]
    elapsed_h = max((latest_t - first_t).total_seconds() / 3600.0, 0.0001)
    avg_rate = (latest_total - first_total) / elapsed_h

    last_hour_cutoff = latest_t - timedelta(hours=1)
    in_window = [r for r in readings if r[0] >= last_hour_cutoff]
    if len(in_window) >= 2:
        win_h = (in_window[-1][0] - in_window[0][0]).total_seconds() / 3600.0
        recent_rate = (in_window[-1][1] - in_window[0][1]) / win_h if win_h > 0 else 0
    else:
        recent_rate = avg_rate

    progress = (latest_total / goal * 100) if goal else 0
    eta_hours = (goal - latest_total) / recent_rate if recent_rate > 0 and goal else None

    return {
        "latest_total": latest_total,
        "goal": goal,
        "progress_pct": progress,
        "avg_rate": avg_rate,
        "recent_rate": recent_rate,
        "eta_hours": eta_hours,
        "latest_ts": latest_t.isoformat(),
        "n_readings": len(readings),
    }


def _recent_deltas(readings, n: int = 12):
    """Last N (ts, total, delta) pairs, newest first."""
    pairs = []
    for (t0, n0, _), (t1, n1, _) in zip(readings, readings[1:]):
        pairs.append((t1, n1, n1 - n0))
    return list(reversed(pairs[-n:]))


def _time_ago(dt: datetime) -> str:
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    delta = datetime.now(timezone.utc) - dt
    s = int(delta.total_seconds())
    if s < 60:
        return f"{s}s ago"
    if s < 3600:
        return f"{s // 60}m ago"
    if s < 86400:
        return f"{s // 3600}h ago"
    return f"{s // 86400}d ago"


def _escape(s: str) -> str:
    return (
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace('"', "&quot;").replace("'", "&#39;")
    )


def _load_news(conn: sqlite3.Connection, limit: int = 25) -> list[dict]:
    try:
        rows = conn.execute(
            "SELECT source, title, url, published FROM news_items "
            "ORDER BY published DESC LIMIT ?",
            (limit,),
        ).fetchall()
    except sqlite3.OperationalError:
        return []
    return [
        {"source": s, "title": t, "url": u, "published": p}
        for (s, t, u, p) in rows
    ]


def _split_news(items: list[dict]) -> tuple[list[dict], list[dict]]:
    articles = [it for it in items if not it["source"].startswith("r/")]
    reddit = [it for it in items if it["source"].startswith("r/")]
    return articles, reddit


SOURCE_DOMAINS = {
    "IGN": "ign.com",
    "Kotaku": "kotaku.com",
    "Polygon": "polygon.com",
    "GameRant": "gamerant.com",
    "PC Gamer": "pcgamer.com",
    "Eurogamer": "eurogamer.net",
    "Push Square": "pushsquare.com",
    "Dexerto": "dexerto.com",
    "VGC": "videogameschronicle.com",
    "The Verge": "theverge.com",
}


def _favicon(source: str) -> str:
    if source.startswith("r/"):
        return "https://www.redditstatic.com/desktop2x/img/favicon/favicon-32x32.png"
    domain = SOURCE_DOMAINS.get(source)
    if not domain:
        return ""
    return f"https://www.google.com/s2/favicons?domain={domain}&sz=32"


def _render_news_list(items: list[dict]) -> str:
    if not items:
        return (
            '<div class="news-empty">'
            '<span class="news-empty-icon">✦</span>'
            'Nothing matching in this category yet.'
            '</div>'
        )
    rows = []
    for it in items:
        icon = _favicon(it["source"])
        icon_html = (
            f'<img class="news-favicon" src="{icon}" alt="" loading="lazy" '
            f'onerror="this.style.display=\'none\'">' if icon else ''
        )
        rows.append(
            f'<li class="news-item">'
            f'<a class="news-link" href="{_escape(it["url"])}" target="_blank" rel="noopener">'
            f'{icon_html}'
            f'<div class="news-body">'
            f'<div class="news-title">{_escape(it["title"])}</div>'
            f'<div class="news-meta">'
            f'<span class="news-source">{_escape(it["source"])}</span>'
            f'<span class="news-dot">·</span>'
            f'<span class="news-time">{_time_ago(datetime.fromisoformat(it["published"]))}</span>'
            f'</div>'
            f'</div>'
            f'</a>'
            f'</li>'
        )
    return f'<ul class="news-list">{"".join(rows)}</ul>'


def render(out_path: Path, db_path: Path, static_mode: bool = False) -> None:
    """Render the dashboard. When static_mode=True, produce a GitHub-Pages-safe
    page that auto-refreshes via meta-refresh and omits the Check Now button
    (no /refresh endpoint to hit)."""
    conn = sqlite3.connect(db_path)
    readings = _load(conn)
    news_items = _load_news(conn)
    conn.close()
    if not readings:
        raise SystemExit("No readings in database yet.")

    stats = _stats(readings)
    # naive Eastern timestamps so Plotly's axis & hover render as ET wall-clock
    times = [_to_et(t).replace(tzinfo=None).isoformat() for t, _, _ in readings]
    totals = [n for _, n, _ in readings]
    rate_times_dt, rates = _hourly_rate(readings)
    rate_times = [t.isoformat() for t in rate_times_dt]
    deltas = _recent_deltas(readings)

    payload = {
        "times": times,
        "totals": totals,
        "rate_times": rate_times,
        "rates": rates,
        "goal": stats["goal"],
    }

    eta_str = (
        f"{stats['eta_hours']:.1f} h"
        if stats["eta_hours"] is not None and stats["eta_hours"] < 720
        else ("—" if stats["eta_hours"] is None else f"{stats['eta_hours']/24:.1f} d")
    )

    rows_html = "".join(
        f"<tr><td>{_fmt_et(t)}</td>"
        f"<td class='num'>{n:,}</td>"
        f"<td class='num delta'>{'+' if d >= 0 else ''}{d:,}</td></tr>"
        for t, n, d in deltas
    )

    articles, reddit_posts = _split_news(news_items)
    if not news_items:
        news_html = (
            '<p style="color:var(--muted);font-size:13px;margin:0">'
            'No matching items yet — run <code>python news.py</code> or hit '
            '<b>Check now</b> to populate.'
            '</p>'
        )
    else:
        news_html = (
            '<div class="tabs">'
            f'<button class="tab active" data-tab="articles" type="button">Articles <span class="tab-count">{len(articles)}</span></button>'
            f'<button class="tab" data-tab="reddit" type="button">Reddit <span class="tab-count">{len(reddit_posts)}</span></button>'
            '</div>'
            f'<div class="tab-panel active" data-panel="articles">{_render_news_list(articles)}</div>'
            f'<div class="tab-panel" data-panel="reddit">{_render_news_list(reddit_posts)}</div>'
        )

    generated_iso = datetime.now(timezone.utc).isoformat()
    if static_mode:
        meta_refresh = '<meta http-equiv="refresh" content="300">'
        button_html = ''
        auto_interval = 300  # GH Actions cron cadence
    else:
        meta_refresh = ''
        button_html = '<button class="refresh" id="refresh-btn" type="button">Check now</button>'
        auto_interval = 60

    html = HTML_TEMPLATE.format(
        meta_refresh=meta_refresh,
        button_html=button_html,
        auto_interval=auto_interval,
        static_mode_js="true" if static_mode else "false",
        generated_iso=generated_iso,
        latest_total=f"{stats['latest_total']:,}",
        goal=f"{stats['goal']:,}",
        progress_pct=f"{stats['progress_pct']:.1f}",
        progress_width=f"{min(stats['progress_pct'], 100):.2f}",
        avg_rate=f"{stats['avg_rate']:,.0f}",
        recent_rate=f"{stats['recent_rate']:,.0f}",
        eta=eta_str,
        latest_ts=_fmt_et(datetime.fromisoformat(stats["latest_ts"])),
        n_readings=stats["n_readings"],
        payload_json=json.dumps(payload),
        rows_html=rows_html,
        news_html=news_html,
        generated_at=_fmt_et(datetime.now(timezone.utc)),
    )
    out_path.write_text(html, encoding="utf-8")


HTML_TEMPLATE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
{meta_refresh}
<title>Petition tracker</title>
<script src="https://cdn.plot.ly/plotly-2.35.2.min.js"></script>
<style>
  :root {{
    --bg: #0f1419;
    --panel: #1a2028;
    --panel-2: #232b36;
    --text: #e6edf3;
    --muted: #8b949e;
    --accent: #58a6ff;
    --accent-2: #f0883e;
    --good: #3fb950;
    --line: #30363d;
  }}
  * {{ box-sizing: border-box; }}
  html, body {{
    margin: 0;
    background: var(--bg);
    color: var(--text);
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", system-ui, sans-serif;
    font-size: 15px;
    line-height: 1.5;
  }}
  .wrap {{
    max-width: 1200px;
    margin: 0 auto;
    padding: 32px 24px 64px;
  }}
  header {{
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 8px;
  }}
  header h1 {{
    margin: 0;
    font-size: 22px;
    font-weight: 600;
  }}
  header .meta {{
    color: var(--muted);
    font-size: 13px;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-variant-numeric: tabular-nums;
  }}
  header .meta .dot {{ opacity: 0.5; }}
  .freshness {{
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }}
  .freshness-dot {{
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--good);
    box-shadow: 0 0 6px rgba(63, 185, 80, 0.7);
    animation: freshness-pulse 2s ease-in-out infinite;
  }}
  .freshness-dot.stale {{
    background: var(--accent-2);
    box-shadow: 0 0 6px rgba(240, 136, 62, 0.7);
  }}
  .freshness-dot.dead {{
    background: #f85149;
    box-shadow: 0 0 6px rgba(248, 81, 73, 0.7);
    animation: none;
  }}
  @keyframes freshness-pulse {{
    0%, 100% {{ opacity: 1; }}
    50%      {{ opacity: 0.45; }}
  }}
  header .right {{
    display: flex;
    align-items: center;
    gap: 12px;
  }}
  button.refresh {{
    background: var(--accent);
    color: #0d1117;
    border: 0;
    border-radius: 6px;
    padding: 8px 14px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: filter 0.15s ease, transform 0.05s ease;
  }}
  button.refresh:hover:not(:disabled) {{ filter: brightness(1.1); }}
  button.refresh:active:not(:disabled) {{ transform: translateY(1px); }}
  button.refresh:disabled {{
    background: var(--panel-2);
    color: var(--muted);
    cursor: not-allowed;
  }}
  #refresh-toast {{
    position: fixed;
    bottom: 24px;
    right: 24px;
    background: var(--panel);
    border: 1px solid var(--line);
    color: var(--text);
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 13px;
    opacity: 0;
    transform: translateY(8px);
    transition: opacity 0.2s ease, transform 0.2s ease;
    pointer-events: none;
    max-width: 340px;
  }}
  #refresh-toast.show {{ opacity: 1; transform: translateY(0); }}
  #refresh-toast.ok {{ border-color: var(--good); }}
  #refresh-toast.err {{ border-color: #f85149; }}
  .cards {{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
  }}
  .card {{
    background: var(--panel);
    border: 1px solid var(--line);
    border-radius: 10px;
    padding: 18px 20px;
  }}
  .card .label {{
    color: var(--muted);
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 6px;
  }}
  .card .value {{
    font-size: 26px;
    font-weight: 600;
    font-variant-numeric: tabular-nums;
  }}
  .card .sub {{
    color: var(--muted);
    font-size: 12px;
    margin-top: 4px;
  }}
  .progress {{
    background: var(--panel);
    border: 1px solid var(--line);
    border-radius: 10px;
    padding: 18px 20px;
    margin-bottom: 24px;
  }}
  .progress-bar {{
    height: 10px;
    background: var(--panel-2);
    border-radius: 5px;
    overflow: hidden;
    margin-top: 10px;
  }}
  .progress-fill {{
    height: 100%;
    background: linear-gradient(90deg, var(--accent), var(--good));
    transition: width 0.4s ease;
  }}
  .progress-row {{
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: var(--muted);
  }}
  .progress-row strong {{ color: var(--text); }}
  .chart-card {{
    background: var(--panel);
    border: 1px solid var(--line);
    border-radius: 10px;
    padding: 16px;
    margin-bottom: 24px;
  }}
  #chart {{ width: 100%; height: 460px; }}
  .grid-2 {{
    display: grid;
    grid-template-columns: 1fr;
    gap: 24px;
  }}
  @media (min-width: 800px) {{ .grid-2 {{ grid-template-columns: 1fr 1fr; }} }}
  .panel {{
    background: var(--panel);
    border: 1px solid var(--line);
    border-radius: 10px;
    padding: 18px 20px;
  }}
  .panel h2 {{
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--muted);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }}
  table {{
    width: 100%;
    border-collapse: collapse;
    font-variant-numeric: tabular-nums;
  }}
  th, td {{
    text-align: left;
    padding: 8px 6px;
    border-bottom: 1px solid var(--line);
    font-size: 13px;
  }}
  th {{
    color: var(--muted);
    font-weight: 500;
  }}
  td.num {{ text-align: right; }}
  td.delta {{ color: var(--good); }}
  .tabs {{
    display: flex;
    gap: 4px;
    border-bottom: 1px solid var(--line);
    margin-bottom: 8px;
  }}
  .tab {{
    background: transparent;
    border: 0;
    color: var(--muted);
    font-size: 13px;
    font-weight: 500;
    padding: 8px 14px;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    margin-bottom: -1px;
    font-family: inherit;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }}
  .tab:hover {{ color: var(--text); }}
  .tab.active {{ color: var(--accent); border-bottom-color: var(--accent); }}
  .tab-count {{
    background: var(--panel-2);
    color: var(--muted);
    border-radius: 10px;
    padding: 1px 7px;
    font-size: 11px;
    font-variant-numeric: tabular-nums;
  }}
  .tab.active .tab-count {{ background: rgba(88, 166, 255, 0.18); color: var(--accent); }}
  .tab-panel {{ display: none; }}
  .tab-panel.active {{ display: block; }}
  ul.news-list {{
    list-style: none;
    margin: 0;
    padding: 0;
    max-height: 480px;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: var(--panel-2) transparent;
  }}
  ul.news-list::-webkit-scrollbar {{ width: 8px; }}
  ul.news-list::-webkit-scrollbar-thumb {{
    background: var(--panel-2);
    border-radius: 4px;
  }}
  .news-item {{ margin: 0; }}
  .news-link {{
    display: flex;
    gap: 12px;
    padding: 12px 10px;
    border-radius: 6px;
    text-decoration: none;
    color: inherit;
    transition: background 0.12s ease, transform 0.05s ease;
  }}
  .news-link:hover {{ background: var(--panel-2); text-decoration: none; }}
  .news-link:active {{ transform: scale(0.997); }}
  .news-favicon {{
    width: 18px;
    height: 18px;
    margin-top: 2px;
    flex-shrink: 0;
    border-radius: 3px;
    background: var(--panel-2);
  }}
  .news-body {{ flex: 1; min-width: 0; }}
  .news-title {{
    font-size: 13.5px;
    line-height: 1.4;
    color: var(--text);
    font-weight: 500;
    margin-bottom: 4px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }}
  .news-link:hover .news-title {{ color: var(--accent); }}
  .news-meta {{
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11.5px;
    color: var(--muted);
  }}
  .news-source {{
    color: var(--text);
    font-weight: 500;
  }}
  .news-dot {{ opacity: 0.5; }}
  .news-time {{ font-variant-numeric: tabular-nums; }}
  .news-empty {{
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: var(--muted);
    font-size: 13px;
    padding: 32px 12px;
    text-align: center;
  }}
  .news-empty-icon {{
    color: var(--accent);
    font-size: 16px;
    opacity: 0.6;
  }}
  .footer-meta {{
    display: flex;
    justify-content: space-between;
    color: var(--muted);
    font-size: 12px;
    margin-top: 24px;
    flex-wrap: wrap;
    gap: 8px;
  }}
  footer {{
    text-align: center;
    color: var(--muted);
    font-size: 12px;
    margin-top: 32px;
  }}
  /* ---- Petition banner ---- */
  #petition-banner {{
    position: relative;
    background: linear-gradient(135deg, #ff6b35 0%, #f7931e 35%, #58a6ff 100%);
    background-size: 200% 200%;
    animation: banner-shift 12s ease infinite;
    color: #fff;
    padding: 14px 56px 14px 24px;
    text-align: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(0);
    transition: transform 0.35s ease, opacity 0.35s ease;
  }}
  #petition-banner.hidden {{
    transform: translateY(-100%);
    opacity: 0;
    pointer-events: none;
  }}
  @keyframes banner-shift {{
    0%   {{ background-position: 0% 50%; }}
    50%  {{ background-position: 100% 50%; }}
    100% {{ background-position: 0% 50%; }}
  }}
  .banner-inner {{
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 18px;
    flex-wrap: wrap;
  }}
  .banner-text {{
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 0.2px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25);
  }}
  .banner-text strong {{
    font-weight: 700;
    font-size: 15px;
  }}
  .banner-cta {{
    background: rgba(255, 255, 255, 0.95);
    color: #1a1a2e;
    padding: 8px 18px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 700;
    text-decoration: none;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    transition: transform 0.12s ease, box-shadow 0.12s ease, background 0.12s ease;
    white-space: nowrap;
  }}
  .banner-cta:hover {{
    background: #fff;
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    text-decoration: none;
  }}
  .banner-cta:active {{ transform: translateY(0); }}
  .banner-close {{
    position: absolute;
    top: 50%;
    right: 14px;
    transform: translateY(-50%);
    background: transparent;
    border: 0;
    color: rgba(255, 255, 255, 0.85);
    font-size: 22px;
    line-height: 1;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background 0.12s ease, color 0.12s ease;
  }}
  .banner-close:hover {{
    background: rgba(255, 255, 255, 0.18);
    color: #fff;
  }}
  a {{ color: var(--accent); text-decoration: none; }}
  a:hover {{ text-decoration: underline; }}
</style>
</head>
<body>
<div id="petition-banner" role="region" aria-label="Sign the petition">
  <div class="banner-inner">
    <span class="banner-text">
      <strong>Want Destiny 3?</strong> Add your name to the petition asking Sony to greenlight it.
    </span>
    <a class="banner-cta" href="https://www.change.org/p/petition-sony-to-develop-destiny-3"
       target="_blank" rel="noopener">Sign the petition →</a>
  </div>
  <button class="banner-close" id="banner-close" type="button" aria-label="Dismiss banner">×</button>
</div>
<div class="wrap">
  <header>
    <h1>Petition signature tracker</h1>
    <div class="right">
      <div class="meta">
        <span>{n_readings} readings</span>
        <span class="dot">·</span>
        <span class="freshness">
          <span class="freshness-dot" id="freshness-dot"></span>
          <span id="last-update">just now</span>
        </span>
        <span class="dot">·</span>
        <span id="countdown">next in {auto_interval}s</span>
      </div>
      {button_html}
    </div>
  </header>
  <div id="refresh-toast"></div>

  <div class="cards">
    <div class="card">
      <div class="label">Current signatures</div>
      <div class="value">{latest_total}</div>
      <div class="sub">of {goal} goal</div>
    </div>
    <div class="card">
      <div class="label">Recent rate (last hour)</div>
      <div class="value">{recent_rate}<span style="font-size:14px;color:var(--muted)"> /hr</span></div>
      <div class="sub">Avg over data: {avg_rate} /hr</div>
    </div>
    <div class="card">
      <div class="label">ETA to goal</div>
      <div class="value">{eta}</div>
      <div class="sub">at current pace</div>
    </div>
    <div class="card">
      <div class="label">Progress</div>
      <div class="value">{progress_pct}%</div>
      <div class="sub">to goal</div>
    </div>
  </div>

  <div class="progress">
    <div class="progress-row">
      <span><strong>{latest_total}</strong> signatures</span>
      <span>goal <strong>{goal}</strong></span>
    </div>
    <div class="progress-bar"><div class="progress-fill" style="width:{progress_width}%"></div></div>
  </div>

  <div class="chart-card">
    <div id="chart"></div>
  </div>

  <div class="grid-2">
    <div class="panel">
      <h2>Recent readings</h2>
      <table>
        <thead><tr><th>Time</th><th class="num">Total</th><th class="num">Δ</th></tr></thead>
        <tbody>{rows_html}</tbody>
      </table>
    </div>
    <div class="panel">
      <h2>What's happening</h2>
      {news_html}
    </div>
  </div>

  <div class="footer-meta">
    <span>Last reading: {latest_ts}</span>
    <span>Page generated: {generated_at}</span>
  </div>
</div>

<script>
const data = {payload_json};

const totalTrace = {{
  x: data.times,
  y: data.totals,
  type: 'scatter',
  mode: 'lines+markers',
  name: 'Total signatures',
  line: {{ color: '#58a6ff', width: 2.5, shape: 'spline', smoothing: 0.3 }},
  marker: {{ size: 5, color: '#58a6ff' }},
  hovertemplate: '<b>%{{y:,}}</b> sigs<br>%{{x|%b %-d, %-I:%M %p}} ET<extra></extra>',
}};

const rateTrace = {{
  x: data.rate_times,
  y: data.rates,
  type: 'bar',
  name: 'Sigs / hour (bucketed)',
  marker: {{ color: '#f0883e', opacity: 0.55 }},
  yaxis: 'y2',
  hovertemplate: '<b>%{{y:,}}</b> sigs in hour<br>%{{x|%b %-d, %-I %p}} ET<extra></extra>',
}};

const layout = {{
  paper_bgcolor: '#1a2028',
  plot_bgcolor: '#1a2028',
  font: {{ color: '#e6edf3', family: 'system-ui, sans-serif' }},
  margin: {{ l: 60, r: 60, t: 20, b: 50 }},
  xaxis: {{ gridcolor: '#30363d', zerolinecolor: '#30363d' }},
  yaxis: {{
    title: 'Total signatures',
    gridcolor: '#30363d',
    zerolinecolor: '#30363d',
    tickformat: ',',
  }},
  yaxis2: {{
    title: 'Sigs / hour',
    overlaying: 'y',
    side: 'right',
    showgrid: false,
    tickformat: ',',
  }},
  shapes: [{{
    type: 'line',
    xref: 'paper', x0: 0, x1: 1,
    yref: 'y', y0: data.goal, y1: data.goal,
    line: {{ color: '#8b949e', dash: 'dash', width: 1.5 }},
  }}],
  annotations: [{{
    xref: 'paper', x: 1, xanchor: 'right',
    yref: 'y', y: data.goal, yanchor: 'bottom',
    text: 'goal ' + data.goal.toLocaleString(),
    showarrow: false,
    font: {{ color: '#8b949e', size: 11 }},
  }}],
  legend: {{ orientation: 'h', y: -0.15 }},
  hovermode: 'x unified',
}};

Plotly.newPlot('chart', [totalTrace, rateTrace], layout, {{ displayModeBar: false, responsive: true }});

const btn = document.getElementById('refresh-btn');
const toast = document.getElementById('refresh-toast');
const countdownEl = document.getElementById('countdown');
const lastUpdateEl = document.getElementById('last-update');
const freshnessDot = document.getElementById('freshness-dot');
const AUTO_INTERVAL_S = {auto_interval};
const STATIC_MODE = {static_mode_js};
const GENERATED_AT = new Date('{generated_iso}');
let secondsLeft = AUTO_INTERVAL_S;
let toastTimer = null;
let busy = false;

function formatAgo(seconds) {{
  seconds = Math.max(0, Math.floor(seconds));
  if (seconds < 5) return 'just now';
  if (seconds < 60) return seconds + 's ago';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m < 60) return s ? `${{m}}m ${{s}}s ago` : `${{m}}m ago`;
  const h = Math.floor(m / 60);
  return `${{h}}h ${{m % 60}}m ago`;
}}

function formatRemaining(seconds) {{
  seconds = Math.max(0, Math.floor(seconds));
  if (seconds < 60) return `next in ${{seconds}}s`;
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s ? `next in ${{m}}m ${{s}}s` : `next in ${{m}}m`;
}}

function updateFreshness() {{
  const ageS = (Date.now() - GENERATED_AT.getTime()) / 1000;
  lastUpdateEl.textContent = formatAgo(ageS);
  // Dot color: green if within one interval, amber if within 2x, red if older
  freshnessDot.classList.remove('stale', 'dead');
  if (ageS > AUTO_INTERVAL_S * 2.5) freshnessDot.classList.add('dead');
  else if (ageS > AUTO_INTERVAL_S * 1.2) freshnessDot.classList.add('stale');
}}

function showToast(msg, kind) {{
  toast.textContent = msg;
  toast.className = 'show ' + (kind || '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {{ toast.className = ''; }}, 3500);
}}

const servedOverHttp = location.protocol === 'http:' || location.protocol === 'https:';
const hasButton = btn !== null;
if (hasButton && !servedOverHttp) {{
  btn.disabled = true;
  btn.title = 'Open via serve.py to enable auto/manual refresh';
}}

// Initial paint + 1-second ticker that updates both freshness and countdown
updateFreshness();
function tickFreshness() {{
  updateFreshness();
  if (STATIC_MODE) {{
    // Static mode: countdown is the time until the next 5-min cron tick,
    // estimated from the generation timestamp + the cron interval.
    const ageS = (Date.now() - GENERATED_AT.getTime()) / 1000;
    const remaining = AUTO_INTERVAL_S - (ageS % AUTO_INTERVAL_S);
    countdownEl.textContent = formatRemaining(remaining);
  }}
}}
setInterval(tickFreshness, 1000);

async function triggerRefresh(manual) {{
  if (busy || !servedOverHttp || !hasButton) return;
  busy = true;
  const originalLabel = btn.textContent;
  btn.disabled = true;
  btn.textContent = manual ? 'Checking…' : 'Auto-checking…';
  try {{
    const r = await fetch('/refresh', {{ method: 'POST' }});
    const data = await r.json();
    if (!r.ok || data.status === 'error') {{
      showToast('Refresh failed: ' + (data.error || r.status), 'err');
      busy = false;
      btn.disabled = false;
      btn.textContent = originalLabel;
      secondsLeft = AUTO_INTERVAL_S;
      return;
    }}
    if (data.status === 'unchanged') {{
      if (manual) showToast('No change — total still ' + data.total.toLocaleString(), 'ok');
      busy = false;
      btn.disabled = false;
      btn.textContent = originalLabel;
      secondsLeft = AUTO_INTERVAL_S;
      return;
    }}
    const delta = data.delta != null ? (data.delta >= 0 ? '+' : '') + data.delta.toLocaleString() : '';
    showToast('Updated: ' + data.total.toLocaleString() + ' ' + delta, 'ok');
    setTimeout(() => location.reload(), 600);
  }} catch (err) {{
    showToast('Refresh failed: ' + err.message, 'err');
    busy = false;
    btn.disabled = false;
    btn.textContent = originalLabel;
    secondsLeft = AUTO_INTERVAL_S;
  }}
}}

if (hasButton) {{
  btn.addEventListener('click', () => triggerRefresh(true));
}}

const banner = document.getElementById('petition-banner');
const bannerClose = document.getElementById('banner-close');
if (banner && localStorage.getItem('petitionBannerDismissed') === '1') {{
  banner.style.display = 'none';
}}
if (bannerClose) {{
  bannerClose.addEventListener('click', () => {{
    banner.classList.add('hidden');
    setTimeout(() => {{ banner.style.display = 'none'; }}, 400);
    localStorage.setItem('petitionBannerDismissed', '1');
  }});
}}

document.querySelectorAll('.tab').forEach(tab => {{
  tab.addEventListener('click', () => {{
    const name = tab.dataset.tab;
    document.querySelectorAll('.tab').forEach(t => t.classList.toggle('active', t === tab));
    document.querySelectorAll('.tab-panel').forEach(p => {{
      p.classList.toggle('active', p.dataset.panel === name);
    }});
  }});
}});

if (servedOverHttp && !STATIC_MODE && hasButton) {{
  setInterval(() => {{
    if (busy) return;
    secondsLeft -= 1;
    if (secondsLeft <= 0) {{
      countdownEl.textContent = 'refreshing…';
      triggerRefresh(false);
    }} else {{
      countdownEl.textContent = formatRemaining(secondsLeft);
    }}
  }}, 1000);
}}
</script>
</body>
</html>
"""
