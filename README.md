# petition-tracker

Polls a Change.org petition page for its signature count, stores readings in SQLite, and renders the trend as a static PNG or a live-updating web dashboard.

Default target: [petition-sony-to-develop-destiny-3](https://www.change.org/p/petition-sony-to-develop-destiny-3) (ID `491299022`).

## How it works

Change.org embeds petition state in an inline `<script>` block in the HTML. The scraper fetches the page with a realistic desktop `User-Agent` and extracts:

```
"signatureCount":{"displayed":N,"total":N,"goal":N}
"weeklySignatureCount":N
```

No headless browser needed. If Cloudflare ever serves a JS challenge or 403s the request, the scraper will retry with exponential backoff and then raise a clear error — at that point a real browser or residential proxy would be needed, but that's out of scope here.

## Setup

```powershell
cd petition-tracker
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Run the poller

```powershell
python poll.py                    # default: 5 min interval, default petition
python poll.py --interval 600     # 10 min
python poll.py --slug some-other-petition-slug
```

Each successful poll appends a row to `signatures.db`. Rows with an unchanged `total` are skipped to keep the DB lean. Logs stream to stdout and `poll.log`. Ctrl-C stops cleanly.

Env vars `POLL_INTERVAL` and `PETITION_SLUG` also work.

## Render the graph

```powershell
python graph.py --save trend.png         # static PNG (matplotlib)
python graph.py --html dashboard.html    # styled standalone HTML dashboard
python graph.py --export readings.csv    # dump raw readings to CSV
```

The HTML dashboard is self-contained (Plotly.js from CDN) — just open the file in any browser. It shows:

- **Stat cards** — current count, recent rate (last hour), ETA to goal, % progress.
- **Progress bar** — total vs goal.
- **Chart** — total signatures (blue line) + hourly-bucketed signing rate (orange bars) + goal line. Bucketing per hour keeps the rate readable; raw between-poll deltas are too jittery to be useful.
- **Recent readings table** — last 12 polls with delta column.

The page has a `<meta refresh>` set to 60s, so just leave the tab open. To keep the file fresh, run the poller with `--html`:

```powershell
python poll.py --interval 300 --html dashboard.html
```

Each successful poll re-renders `dashboard.html`, and the open browser tab picks it up on the next auto-refresh.

### Manual "Check now" button

The dashboard has a **Check now** button that triggers a fresh scrape on demand. It only works when the page is served over HTTP (it needs a backend endpoint to talk to). Run:

```powershell
python serve.py --port 8765
```

Then open <http://127.0.0.1:8765/>. Clicking **Check now** POSTs to `/refresh`, which fetches the petition page, inserts the new reading if `total` changed, re-renders the HTML, and reloads the tab.

You can run `serve.py` and `poll.py --html dashboard.html` side by side — the poller handles the 5-minute cadence; the button is for when you want to force an update right now.

## Backfill historic data

Provide a CSV with at least `ts,total` columns (optional: `displayed,goal,weekly`):

```csv
ts,total
2026-04-01T12:00:00Z,11200
2026-04-15T12:00:00Z,11540
```

Then:

```powershell
python backfill.py path\to\historic.csv
```

Duplicate timestamps are skipped, so re-running is safe.

## Tests

```powershell
pytest
```

## Notes / caveats

- Change.org sits behind Cloudflare. A bare Python `User-Agent` is reliably 403'd; the spoofed Chrome UA in `scraper.py` currently works. If that stops working, the next steps would be a real-browser-based scraper (Playwright with `stealth`) or a residential proxy.
- All timestamps are stored as ISO 8601 in UTC.
- The signing-rate trace can dip negative if the page's `total` ever goes down (Change.org occasionally removes signatures); the poller logs a warning when this happens.
