# All The Goodies — Build Plan & Sprint Board

> Living document. Source of truth for "what's next." Edit as we go.
> Controlling design doc is [SPEC.md](SPEC.md); this file sequences the work.

## Where we are (status snapshot)

**v0.1 shipped & tagged.** All six MVP items from SPEC §5.1 are done, plus the wild-cache
reveal animation (which the spec had filed under post-MVP polish — we're ahead).

**Shipped to `main`:**
- PPT engine (0–6), data-attachment, latches upward — `ProgressionTier`
- Milestone detectors: pickup/craft/smelt/ore-mine/dimension, verified marker IDs — `ProgressionEvents`, `ProgressionMilestones`
- ATM Star Tracker — counts stars to 25, `/atg stars`
- 42 loot pools (ppt0–ppt6 × 6 rarities), all `minecraft:chest` type
- ATM Cache + 6 colored caches, two-layer roll wired — `ATMCacheItem`, `ColoredCacheItem`, `CacheOpening`, `CacheRoller`
- Activity drops (mob kill / ore break), PPT-weighted, anti-farm rolling window — `ActivityDropEvents`, `AntiFarmLimiter`
- Server config: master toggle, drop rates, anti-farm caps, rarity weights — `ATGConfig`
- JEI + JADE integration; Patchouli "What Now?" guidebook
- Creative tab, honest-odds tooltip on the ATM Cache
- **Wild-cache reveal:** rising color-rolling spiral → apex burst → float → homing fly-to-player, one per nearby player, fully server-synced — `WildCacheRoulette`, `CacheHomingManager`, `ATGScheduler`, network package
- Loot-open toast lists actual items won

**Debug:** `/atg ppt [set]`, `/atg cache [n]`, `/atg open <rarity> [ppt]`, `/atg stars`, `/atg zombie`

---

## Roadmap

### ✅ M1 — "The Loop Is Real" (v0.1-alpha) — DONE
### ✅ M2 — "Honest & Tunable" (v0.1-beta) — DONE
### ✅ M3 — "Orientation" (v0.1) — DONE · **tagged v0.1**
### ✅ Bonus — Wild-cache reveal animation (the earned reward) — DONE

---

## 🎯 Proposed next sprint — pick a direction

We finished the *loop*. Three coherent directions for v0.2; my recommendation first.
**This is Noah's call** — the rest of this section is a proposal, not a commitment.

### ⭐ Recommended: v0.2 "Worth Opening" — make the loot actually exciting

*Why:* the loot system is the headline feature (SPEC §3), but M1's bar was "pools start
small." The loop works; the rewards are still placeholder-grade. Reward quality + the gamble
feeling real is the biggest unrealized lever for fun — and it's mostly solo-able, data-driven
work (aligns with SPEC pillar 3). No worldgen, no scope risk.

| # | Ticket | Notes |
|---|--------|-------|
| 1 | **Loot ID audit & fix** | Verify every modded ID in the real ATM10 JEI. High-risk: `allthemodium:allthemodium_pickaxe` / `vibranium_pickaxe` / `unobtainium_pickaxe` (ATM tool tiers are often Silent Gear / Tinkers, not plain picks). Likely-fine: the ingots, `piglich_heart`, `mekanism:ingot_osmium`. **Needs Noah to boot ATM10** for JEI lookups. |
| 2 | **Real jackpots per tier** | Author the §3.2 "Knife" Mythic + Legendary rewards so every tier has a genuine standout. Use Silent Gear / pre-enchanted gear with good pre-rolled traits (Noah's "classified armor → silent gear that rolls fun stuff, pre-weighted toward decent stats" idea). |
| 3 | **Colored-cache odds tooltip** | The transparency we deferred: a colored cache shows its reward odds for *that color × current PPT*. Honest-gambling pillar (§3.6). |
| 4 | **Prime Cache** | A rare cache guaranteeing a minimum color — the occasional big find (SPEC §3.4 "Later"). |
| 5 | *(stretch)* **Ore-value scaling** | Diamond / ancient-debris / modded rare ores bias toward a better cache + higher drop %; coal stays at the floor. |

**DoD:** every tier has a jackpot that makes you go "!"; all modded IDs resolve in ATM10;
opening a colored cache shows honest odds; Prime Caches exist as a rare thrill.

### Alt A — v0.2 "Found in the World" (worldgen + presentation)
Make wild caches a real exploration object: half-buried "gravestone" crate worldgen (see the
Wild Cache concept), crate-shaped block model + crash-landed tilt, mob guard. **Caveat:**
SPEC §4 flags worldgen as a core non-goal (keeps existing worlds compatible) — additive
new-chunk-only gen may be OK but it's a design decision. Also Noah wants to teach build design
next session, so the cache *builds* are better done collaboratively than solo.

### Alt B — v0.2 "The Opening Roulette" (the CS:GO scroll GUI)
The horizontal item-ticker that scrolls, decelerates, and lands on the reward when you open a
colored cache (SPEC §3.5 "Later"). One big, juicy feature — but we just shipped a reveal
animation, and the open-toast you approved already covers the basics. Higher polish, narrower
value than "Worth Opening."

---

## Icebox — the scope fence 🧊

Explicitly **not** in the next sprint. Shiny mid-sprint ideas land here; we don't touch them
until the current DoD is met.

- ✅ ~~Particle roulette reveal~~ — **shipped** (the wild-cache reveal)
- `wild_cache` worldgen (half-buried gravestone + terrain rubble + mob guard) — *see Alt A*
- Crate-shaped block model + crash-landed tilt (block entity renderer)
- Roulette GUI (CS:GO scroll animation) — *see Alt B*
- Prime Cache — *promoted into the recommended sprint*
- Ore-value scaling — *stretch in the recommended sprint*
- **Cache Buster** — tool that instantly breaks wild_cache blocks, maybe AOE
- **Cache Mass-Opener** — right-click a stack to open all at once with a summary readout
- **Fusion Block** — combine N lower-tier caches into 1 higher-tier
- **Cache Merchant Villager** — special trades: caches for rare items
- **Music HUD mod** — separate mod, show currently-playing ATM track name
- FTB Quests PPT signals + reward delivery
- Other QoL (SPEC §4): tech-wall bridge kits, safety-net items, Dimensional Atlas, boss-prep checklist, myth pages

---

## Working agreement (how we run it)

1. **One branch per ticket** → build → test in the real ATM10 pack → squash-merge → delete branch.
2. **Stick to the script.** New ideas go to the Icebox, not the current branch. Claude holds Noah accountable.
3. **Model tiers:** planning/architecture/stuck-bugs → Opus. Executing well-defined tickets (loot JSON, event boilerplate) → Sonnet. Don't burn Opus on grunt work.
4. **Fast loop = `runClient`** (no pack, ~15s) for compile/registration checks; **real loop = ATM10 instance** for pack-dependent behavior (modded item IDs, JEI). `/reload` for loot, F3+T for textures.
5. **Identity:** this is a Noah repo — `NoahBritton` git identity + `gh auth switch` verified before any push/merge. Never mix Candy.

> **Note on testing limits:** `runClient` has no ATM10 mods, so modded item IDs can't be
> verified there (they log as "unknown registry key" — harmless in dev, but it means ticket #1
> of "Worth Opening" needs Noah to boot the real pack).
