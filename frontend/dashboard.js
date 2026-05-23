/* Petition tracker frontend. Hosted at axogames.dev/d3-petition-tracker/.
   Fetches state from the VPS API, renders the dashboard, ticks every second. */

// Override via ?api=https://other-host/ for local dev
const API_BASE = new URLSearchParams(location.search).get("api")
  || "https://petition-api.axogames.dev";
const POLL_INTERVAL_S = 60;

const ET_TZ = "America/New_York";
const SOURCE_DOMAINS = {
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
};

// ----- DOM cache -----
const el = (id) => document.getElementById(id);
const readingsCountEl = el("readings-count");
const lastUpdateEl = el("last-update");
const countdownEl = el("countdown");
const freshnessDot = el("freshness-dot");
const errorBanner = el("error-banner");
const apiHostEl = el("api-host");
const banner = el("petition-banner");

// ----- State -----
let state = null;            // last successful payload
let generatedAt = null;      // Date object, when state was generated server-side
let secondsLeft = POLL_INTERVAL_S;
let busy = false;

// ----- Formatting helpers -----
function fmtAgo(seconds) {
  seconds = Math.max(0, Math.floor(seconds));
  if (seconds < 5) return "just now";
  if (seconds < 60) return seconds + "s ago";
  const m = Math.floor(seconds / 60), s = seconds % 60;
  if (m < 60) return s ? `${m}m ${s}s ago` : `${m}m ago`;
  const h = Math.floor(m / 60);
  return `${h}h ${m % 60}m ago`;
}

function fmtRemaining(seconds) {
  seconds = Math.max(0, Math.floor(seconds));
  if (seconds < 60) return `next in ${seconds}s`;
  const m = Math.floor(seconds / 60), s = seconds % 60;
  return s ? `next in ${m}m ${s}s` : `next in ${m}m`;
}

function fmtET(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleString("en-US", {
    timeZone: ET_TZ,
    month: "short", day: "numeric",
    hour: "numeric", minute: "2-digit",
    hour12: true, timeZoneName: "short",
  });
}

function fmtETShort(iso) {
  const d = new Date(iso);
  return d.toLocaleString("en-US", {
    timeZone: ET_TZ,
    month: "short", day: "numeric",
    hour: "numeric", minute: "2-digit", hour12: true,
  });
}

function fmtNum(n) {
  if (n == null) return "—";
  return n.toLocaleString();
}

function escapeHtml(s) {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
          .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function faviconUrl(source) {
  if (source.startsWith("r/")) {
    return "https://www.redditstatic.com/desktop2x/img/favicon/favicon-32x32.png";
  }
  const domain = SOURCE_DOMAINS[source];
  return domain ? `https://www.google.com/s2/favicons?domain=${domain}&sz=32` : "";
}

// ----- Fetch & render -----
async function fetchState() {
  busy = true;
  try {
    const r = await fetch(`${API_BASE}/api/state.json`, { cache: "no-store" });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    const data = await r.json();
    state = data;
    generatedAt = new Date(data.generated_at);
    secondsLeft = POLL_INTERVAL_S;
    errorBanner.classList.add("hidden");
    render(data);
  } catch (err) {
    showError(`Couldn't reach the tracker API (${err.message}). Retrying…`);
    secondsLeft = POLL_INTERVAL_S;  // retry next tick
  } finally {
    busy = false;
  }
}

function showError(msg) {
  errorBanner.textContent = msg;
  errorBanner.classList.remove("hidden");
  freshnessDot.classList.remove("fresh", "stale");
  freshnessDot.classList.add("dead");
}

function render(data) {
  const readings = data.readings || [];
  if (!readings.length) {
    showError("No readings yet — the poller hasn't captured any data.");
    return;
  }
  const latest = readings[readings.length - 1];
  const first = readings[0];

  // ---- Cards
  el("card-total").textContent = fmtNum(latest.total);
  el("card-goal").textContent = `of ${fmtNum(latest.goal)} goal`;

  const elapsedH = Math.max((new Date(latest.ts) - new Date(first.ts)) / 3600000, 0.0001);
  const avgRate = (latest.total - first.total) / elapsedH;
  el("card-avg-rate").textContent = `Avg over data: ${fmtNum(Math.round(avgRate))} /hr`;

  // last-hour rate
  const cutoff = new Date(latest.ts).getTime() - 3600_000;
  const recentReadings = readings.filter(r => new Date(r.ts).getTime() >= cutoff);
  let recentRate = avgRate;
  if (recentReadings.length >= 2) {
    const winH = (new Date(recentReadings[recentReadings.length - 1].ts) - new Date(recentReadings[0].ts)) / 3600000;
    if (winH > 0) {
      recentRate = (recentReadings[recentReadings.length - 1].total - recentReadings[0].total) / winH;
    }
  }
  el("card-recent-rate").innerHTML = `${fmtNum(Math.round(recentRate))}<span class="unit"> /hr</span>`;

  const remaining = (latest.goal || 0) - latest.total;
  const etaHours = recentRate > 0 ? remaining / recentRate : null;
  let etaStr = "—";
  if (etaHours !== null && etaHours >= 0) {
    etaStr = etaHours < 24 ? `${etaHours.toFixed(1)} h` : `${(etaHours / 24).toFixed(1)} d`;
  }
  el("card-eta").textContent = etaStr;

  const progressPct = latest.goal ? (latest.total / latest.goal) * 100 : 0;
  el("card-progress").textContent = `${progressPct.toFixed(1)}%`;
  el("prog-current").textContent = fmtNum(latest.total);
  el("prog-goal").textContent = fmtNum(latest.goal);
  el("progress-fill").style.width = `${Math.min(progressPct, 100)}%`;

  // ---- Chart
  renderChart(readings);

  // ---- Recent readings table
  const tbody = el("readings-body");
  tbody.innerHTML = "";
  const deltas = [];
  for (let i = 1; i < readings.length; i++) {
    deltas.push({
      ts: readings[i].ts,
      total: readings[i].total,
      delta: readings[i].total - readings[i - 1].total,
    });
  }
  deltas.slice(-12).reverse().forEach(row => {
    const tr = document.createElement("tr");
    const deltaClass = row.delta < 0 ? "num delta neg" : "num delta";
    const sign = row.delta >= 0 ? "+" : "";
    tr.innerHTML = `
      <td>${escapeHtml(fmtETShort(row.ts))}</td>
      <td class="num">${fmtNum(row.total)}</td>
      <td class="${deltaClass}">${sign}${fmtNum(row.delta)}</td>
    `;
    tbody.appendChild(tr);
  });

  // ---- News
  renderNews(data.news || []);

  // ---- Header meta
  readingsCountEl.textContent = `${readings.length} readings`;
  el("latest-ts").textContent = fmtET(latest.ts);
  apiHostEl.textContent = new URL(API_BASE).host;

  updateFreshness();
}

function renderChart(readings) {
  // Build hourly buckets in ET for the rate trace
  const byHour = new Map();
  readings.forEach(r => {
    const d = new Date(r.ts);
    // ET hour key: use Intl to bucket properly
    const key = d.toLocaleString("en-CA", {
      timeZone: ET_TZ,
      year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", hour12: false,
    });
    const cur = byHour.get(key) || { first: r.total, last: r.total };
    cur.first = Math.min(cur.first, r.total);
    cur.last  = Math.max(cur.last,  r.total);
    byHour.set(key, cur);
  });
  const hours = Array.from(byHour.keys()).sort();
  const rateTimes = [];
  const rateValues = [];
  let prev = null;
  for (const h of hours) {
    const bucket = byHour.get(h);
    if (prev !== null) {
      // h is "YYYY-MM-DD, HH" — convert back to a Date for plot
      const [date, hh] = h.split(", ");
      const dt = new Date(`${date}T${hh}:00:00-04:00`);  // EDT offset; close enough
      rateTimes.push(dt);
      rateValues.push(Math.max(0, bucket.last - prev.last));
    }
    prev = bucket;
  }

  const totalTrace = {
    x: readings.map(r => new Date(r.ts)),
    y: readings.map(r => r.total),
    type: "scatter",
    mode: "lines+markers",
    name: "Total signatures",
    line: { color: "#58a6ff", width: 2.5, shape: "spline", smoothing: 0.3 },
    marker: { size: 5, color: "#58a6ff" },
    hovertemplate: "<b>%{y:,}</b> sigs<br>%{x|%b %d, %I:%M %p}<extra></extra>",
  };

  const rateTrace = {
    x: rateTimes,
    y: rateValues,
    type: "bar",
    name: "Sigs / hour (bucketed)",
    marker: { color: "#f0883e", opacity: 0.55 },
    yaxis: "y2",
    hovertemplate: "<b>%{y:,}</b> sigs/hour<br>%{x|%b %d, %I %p}<extra></extra>",
  };

  const goal = readings[readings.length - 1].goal;
  const layout = {
    paper_bgcolor: "#1a2028",
    plot_bgcolor: "#1a2028",
    font: { color: "#e6edf3", family: "system-ui, sans-serif" },
    margin: { l: 60, r: 60, t: 20, b: 50 },
    xaxis: { gridcolor: "#30363d", zerolinecolor: "#30363d" },
    yaxis: {
      title: "Total signatures",
      gridcolor: "#30363d", zerolinecolor: "#30363d", tickformat: ",",
    },
    yaxis2: {
      title: "Sigs / hour",
      overlaying: "y", side: "right", showgrid: false, tickformat: ",",
    },
    shapes: goal ? [{
      type: "line", xref: "paper", x0: 0, x1: 1,
      yref: "y", y0: goal, y1: goal,
      line: { color: "#8b949e", dash: "dash", width: 1.5 },
    }] : [],
    annotations: goal ? [{
      xref: "paper", x: 1, xanchor: "right",
      yref: "y", y: goal, yanchor: "bottom",
      text: "goal " + goal.toLocaleString(),
      showarrow: false, font: { color: "#8b949e", size: 11 },
    }] : [],
    legend: { orientation: "h", y: -0.15 },
    hovermode: "x unified",
  };

  Plotly.react("chart", [totalTrace, rateTrace], layout, { displayModeBar: false, responsive: true });
}

function renderNews(items) {
  const articles = items.filter(it => !it.source.startsWith("r/"));
  const reddit = items.filter(it => it.source.startsWith("r/"));
  el("count-articles").textContent = articles.length;
  el("count-reddit").textContent = reddit.length;
  el("panel-articles").innerHTML = renderNewsList(articles);
  el("panel-reddit").innerHTML = renderNewsList(reddit);
}

function renderNewsList(items) {
  if (!items.length) {
    return `<div class="news-empty"><span>✦</span> Nothing matching in this category yet.</div>`;
  }
  const rows = items.map(it => {
    const icon = faviconUrl(it.source);
    const iconHtml = icon
      ? `<img class="news-favicon" src="${escapeHtml(icon)}" alt="" loading="lazy" onerror="this.style.display='none'">`
      : "";
    const ageS = (Date.now() - new Date(it.published).getTime()) / 1000;
    return `
      <li>
        <a class="news-link" href="${escapeHtml(it.url)}" target="_blank" rel="noopener">
          ${iconHtml}
          <div class="news-body">
            <div class="news-title">${escapeHtml(it.title)}</div>
            <div class="news-meta">
              <span class="news-source">${escapeHtml(it.source)}</span>
              <span class="news-dot">·</span>
              <span class="news-time">${fmtAgo(ageS)}</span>
            </div>
          </div>
        </a>
      </li>`;
  }).join("");
  return `<ul class="news-list">${rows}</ul>`;
}

function updateFreshness() {
  if (!generatedAt) {
    freshnessDot.classList.remove("fresh", "stale");
    freshnessDot.classList.add("dead");
    lastUpdateEl.textContent = "no data";
    return;
  }
  const ageS = (Date.now() - generatedAt.getTime()) / 1000;
  lastUpdateEl.textContent = fmtAgo(ageS);
  freshnessDot.classList.remove("fresh", "stale", "dead");
  if (ageS > POLL_INTERVAL_S * 2.5) freshnessDot.classList.add("dead");
  else if (ageS > POLL_INTERVAL_S * 1.2) freshnessDot.classList.add("stale");
  else freshnessDot.classList.add("fresh");
}

// ----- Tick loop -----
function tick() {
  if (busy) return;
  secondsLeft -= 1;
  if (secondsLeft <= 0) {
    countdownEl.textContent = "refreshing…";
    fetchState();
    return;
  }
  countdownEl.textContent = fmtRemaining(secondsLeft);
  updateFreshness();
}

// ----- Banner dismiss -----
if (banner && localStorage.getItem("petitionBannerDismissed") === "1") {
  banner.style.display = "none";
}
const bannerClose = el("banner-close");
if (bannerClose) {
  bannerClose.addEventListener("click", () => {
    banner.classList.add("dismissing");
    setTimeout(() => { banner.style.display = "none"; }, 400);
    localStorage.setItem("petitionBannerDismissed", "1");
  });
}

// ----- Tabs -----
document.querySelectorAll(".tab").forEach(tab => {
  tab.addEventListener("click", () => {
    const name = tab.dataset.tab;
    document.querySelectorAll(".tab").forEach(t => t.classList.toggle("active", t === tab));
    document.querySelectorAll(".tab-panel").forEach(p => {
      p.classList.toggle("active", p.dataset.panel === name);
    });
  });
});

// ----- Boot -----
apiHostEl.textContent = new URL(API_BASE).host;
fetchState();
setInterval(tick, 1000);
