# All The Goodies — Build Plan & Sprint Board

> Living document. Source of truth for "what's next." Edit as we go.
> Controlling design doc is [SPEC.md](SPEC.md); this file sequences the work.

## Where we are (status snapshot)

**Shipped to `main`:**
- PPT engine (0–6), data-attachment, latches upward — `ProgressionTier`
- Milestone detectors: 5 event signals (pickup/craft/smelt/ore-mine/dimension), verified ATM10 marker IDs — `ProgressionEvents`, `ProgressionMilestones`
- Debug commands: `/atg ppt [set]`, `/atg cache`, `/atg open` — `ATGCommands`
- ATM Cache item — right-click opens, rolls rarity, pulls loot table — `ATMCacheItem` + `CacheOpening` + `CacheRoller`
- `wild_cache` block — breakable, rolls rarity on break, drops matching colored cache — `WildCacheBlock`
- 6 colored caches (consumer→covert), two-layer roll wired — `ColoredCacheItem`
- NeoForge pinned to 21.1.234 (matches ATM10 live)
- Dev workflow: symlinked JAR into the ATM10 instance; `/reload` for loot, F3+T for textures

**Not done:** 40/42 loot pools empty · no activity drops · no anti-farm · no config · no transparency tooltip · no QoL helpers.

---

## Roadmap — 3 milestones to v0.1

### M1 — "The Loop Is Real" (v0.1-alpha)  ← current focus
*DoD: play ATM10 normally for ~20 min and you organically receive caches; every rarity gives a tier-appropriate reward; an AFK mob farm can't mint caches.*
- All 42 loot pools filled (small is fine)
- Activity drops (mining ore / mob kills / natural chests), PPT-weighted, low %
- Milestone drops verified (tier-up → one cache)
- Anti-farm limiter (rolling per-window cap / diminishing returns)

### M2 — "Honest & Tunable" (v0.1-beta)
*DoD: Noah (or a server) can fully control the experience without touching code.*
- Transparency: cache tooltip shows rarity odds + current PPT (SPEC §3.6 — "honest gambling" pillar)
- Config: master toggle, drop rates, rarity weights, anti-farm caps, "purist" preset
- Playtest-tuning pass (drop feel, Caches/hour)

### M3 — "Orientation" (v0.1 release)
*DoD: SPEC §5.1 MVP list satisfied. Tag v0.1.*
- ATM Star Tracker (live have/need checklist)
- "What Now?" guidebook
- JADE source/pickaxe tooltips

---

## Current Sprint — "Fill the loot + wire the drops" (M1)

| # | Ticket | State | Notes |
|---|--------|-------|-------|
| 1 | All 42 loot pools filled | 🔄 in progress | ppt0 common+mythic done; uncommon/rare/epic started. Tier 3+ modded IDs flagged for JEI verify. |
| 2 | Activity drops | ⬜ todo | `LivingDeathEvent` + `BlockEvent.BreakEvent` → low-% colored cache, PPT-weighted color |
| 3 | Anti-farm limiter | ⬜ todo | rolling window cap on cache grants; depends on #2 |
| 4 | Verify milestone drops | ⬜ todo | confirm tier-up still grants a cache after refactors |

**Sprint DoD:** force-open every rarity → real loot; play 20 min → ≥1 organic cache; AFK farm → no caches.

---

## Icebox — the scope fence 🧊

These are **explicitly not** in v0.1. When a shiny idea appears mid-sprint, it lands here with a note and we do **not** touch it until the current milestone's DoD is met.

- **Particle roulette reveal** — note block trills rolling through rarities, colored dust, poof + item plop on landing (Noah's vision, 2026-06-27). *The reward for finishing M1–M3.*
- `wild_cache` worldgen (half-buried gravestone + terrain rubble + mob guard)
- Crate-shaped block model + crash-landed tilt (block entity renderer)
- Roulette GUI (CS:GO scroll animation)
- Prime Cache (guaranteed-minimum-color find)
- FTB Quests PPT signals
- Other QoL: tech-wall bridge kits, safety-net items, Dimensional Atlas, boss-prep checklist

---

## Working agreement (how we run it)

1. **One branch per ticket** → build → test in the real ATM10 pack → squash-merge → delete branch.
2. **Stick to the script.** New ideas go to the Icebox, not the current branch. Claude holds Noah accountable.
3. **Model tiers:** planning/architecture/stuck-bugs → Opus. Executing well-defined tickets (loot JSON, event boilerplate) → Sonnet. Don't burn Opus on grunt work.
4. **Fast loop = `runClient`** (no pack, ~15s) for compile/registration checks; **real loop = ATM10 instance** for pack-dependent behavior. `/reload` for loot, F3+T for textures — avoid full restarts.
5. **Identity:** this is a Noah repo — `NoahBritton` git identity + `gh auth switch` verified before any push/merge. Never mix Candy.
