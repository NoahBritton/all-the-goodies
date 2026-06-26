# Project Spec — "All The Goodies" (working title)

> **Status:** v0.1 (living document). This is the controlling spec for the ATM10 helper
> add-on. We iterate by editing this file — features should not drift from here without an
> explicit change recorded in the [Decision log](#decision-log).
>
> **Working name:** *All The Goodies* (ATG) — plays on "All The Mods." Provisional; see
> [Open decisions](#open-decisions). The in-game loot crate item is the **ATM Cache**.

## 1. Vision

A NeoForge add-on for **All The Mods 10** (MC 1.21.1) that makes the long road to the ATM
Star friendlier for players who don't know the pack and aren't strong at Minecraft — **without
trivializing it**.

It does this two ways:

1. **The ATM Cache system (headline feature):** random loot crates that drop as you play
   normally and pay out **progression-aware** rewards in CS:GO-style rarity tiers. The
   jackpot is always something *uber-useful at your current point in the game*.
2. **Quality-of-life helpers (supporting):** orientation, signposting, and safety-net items
   that remove confusion and unfair deaths. Many of these double as Cache rewards.

**Design north star:** *Ease the curve, don't flatten it.* Prefer **information, head-starts,
and safety nets** over raw power. Everything is **config/datapack-toggleable** so a server (or
Noah) can dial the help up or down.

## 2. Design pillars

1. **Progression-aware.** The mod knows roughly where the player is and scales help to it. A
   reward useless or game-breaking at the wrong time is a bug.
2. **Plays into normal play.** Crates come from *doing things* (mining, killing, exploring,
   completing quests), not menus. "Goodie bags as you play."
3. **Data-driven & tunable.** Reward pools, drop rates, and rarity weights live in **loot
   tables / datapacks / KubeJS**, not hardcoded Java — so they retune per pack version and
   Noah can tweak without a rebuild. (Same approach ATM itself uses.)
4. **World-safe & additive.** No required worldgen in the core; works dropped into an existing
   world. Nothing destructive or irreversible to the save.
5. **Honest gambling.** Rarity odds are transparent (viewable), anti-AFK-farm protected, and
   never pay-to-win-style real-money — purely in-game.

## 3. The ATM Cache system (headline)

### 3.1 Player Progression Tier (PPT) — the scaling engine

A per-player value, **0–6**, that only ever increases ("latches up"), stored via a NeoForge
**data attachment** on the player.

| PPT | Name | Rough meaning |
|---|---|---|
| 0 | Stranded | just spawned; stone/wood |
| 1 | Established | iron tools, first base, early food |
| 2 | Industrial | early Mekanism/Create, basic power, ore doubling |
| 3 | Allthemodium | Netherite+, Deep Dark, first ATM metal |
| 4 | Vibranium | Nether ceiling mining, alloys starting |
| 5 | Unobtainium | The End, high-tier alloys, autocraft network |
| 6 | Star-chase | building ATM Star components |

**How PPT is computed (latching milestones):** the player's PPT rises the first time they hit
a tier's marker — e.g. *first Allthemodium ingot obtained → PPT ≥ 3*. Markers are detected via
events (item pickup/craft, advancements, dimension entry) and, where available, **FTB Quests**
chapter progress. Latching upward avoids flapping. Marker→tier mapping lives in config/data so
it's tunable. (See [Open decisions](#open-decisions) for the exact signal set.)

### 3.2 Rarity tiers (CS:GO-style)

Each Cache opening rolls a rarity, then pulls a reward from the **{rarity × PPT}** pool.
Indicative weights (config-tunable):

| Rarity | Flavor | Weight | Reward magnitude |
|---|---|---|---|
| Common | Worn | ~79% | Small — resources, food, XP, low components |
| Uncommon | Industrial | ~15% | Small/Medium — useful stack, a basic upgrade |
| Rare | Mil-Spec | ~4.7% | Medium — a useful machine, meaningful cache |
| Epic | Restricted | ~0.9% | Medium/Large — gear piece, partial skip |
| Legendary | Classified | ~0.3% | Large — big cache, rare ingredient (e.g. a Piglich Heart at PPT 4) |
| **Mythic** | **Covert / "the Knife"** | **~0.1%** | **Jackpot — one standout item perfectly timed to the player's PPT** |

The **two-roll model** (rarity first, then PPT-scaled reward) is what makes a Mythic at PPT 2
feel different from a Mythic at PPT 5 while both feel like jackpots.

**The "Knife" per tier (jackpot examples — illustrative, final pools in data):**

| PPT | Example Mythic reward |
|---|---|
| 0 | Unbreakable starter Silent Gear pick, or a Wooden Jetpack |
| 1 | A full set of training armor + a stack of cooked meals |
| 2 | A pre-plumbed ore-doubling kit or a powered starter generator |
| 3 | A stack of Allthemodium, or an Allthemodium tool part |
| 4 | 2× Piglich Heart (the alloy bottleneck), or a Vibranium alloy ingot |
| 5 | An Unobtainium alloy block, or a big Awakened-essence cache |
| 6 | A free mid-tier ATM Star *sub-component* (skips one grind) |

### 3.3 How Caches drop ("as you play normally")

- **Activity drops (low chance):** mob kills, mining ore, opening naturally-generated chests,
  fishing, completing advancements/quests. Active play yields Caches; menus don't.
- **Milestone drops (guaranteed):** the first time you reach each PPT, you get one guaranteed
  tier-appropriate Cache as a "level-up" moment.
- **Anti-farm:** a soft rate limiter / diminishing returns so AFK mob farms can't mint Caches
  (e.g., a rolling per-window cap; ore drops weighted by *new* blocks). All config-tunable.

### 3.4 Cache items

- **MVP:** a single **ATM Cache** item whose contents scale by the opener's PPT.
- **Later:** a rarer **Prime Cache** that guarantees a higher minimum rarity, as an occasional
  big find.

### 3.5 Opening UX

- **MVP:** right-click the Cache → server rolls → reward(s) go to inventory + a color-coded
  toast/chat line announcing the rarity. Simple, robust.
- **Later (polish):** a CS:GO-style roulette/scroll animation GUI. Explicitly **not MVP**.

### 3.6 Transparency

An in-Cache tooltip or JEI page shows the current rarity odds and (optionally) the player's
current PPT, so the gambling is honest.

## 4. Quality-of-life helpers (supporting)

Carried over from [`docs/11-mod-build-opportunities.md`](docs/11-mod-build-opportunities.md).
Each maps to a newcomer friction point (Fxx) in
[`docs/10-newcomer-friction.md`](docs/10-newcomer-friction.md). Many of these are **also Cache
rewards**, which ties the two pillars together.

- **Orientation/info:** "What Now?" guidebook, JADE source/pickaxe tooltips, boss-prep
  checklist, Dimensional Atlas, ATM Star myth-corrector pages.
- **Starter goodies:** Day-One Kit, Waystone starter, configurable Piglich Lure.
- **Safety nets:** training armor, a *safe* (no-meltdown) training reactor, meltdown warning,
  Hostile Networks data-model backup.
- **Tech-wall bridges:** pre-plumbed 5× ore kit, channel-free storage core that upgrades into
  AE2, pre-configured Mekanism factory blocks.
- **Endgame signposting:** **ATM Star Tracker** (live have/need checklist), "start these early"
  nudge, altar build-helper.

## 5. Scope

### 5.1 MVP (v0.1 — the first playable release)

The full Cache **loop**, plus the highest-leverage info helpers:

1. **ATM Cache** item + PPT engine + two-roll rarity + data-driven reward pools (all 7 PPTs ×
   6 rarities, even if pools start small).
2. **Activity + milestone drops** with the anti-farm limiter.
3. **Simple open UX** (right-click → reward + toast). No animation yet.
4. **"What Now?" guidebook** + **JADE source/pickaxe tooltips**.
5. **ATM Star Tracker**.
6. **Config** for all rates/weights/toggles.

### 5.2 Post-MVP backlog (rough order)

- CS:GO roulette opening animation + Prime Cache.
- Remaining safety-net items (training reactor, armor, model backup).
- Tech-wall bridge kits.
- Dimensional Atlas + boss-prep checklist + myth pages.
- FTB Quests integration for PPT signals and reward delivery.

### 5.3 Non-goals (so we don't drift)

- ❌ No required new worldgen (ores/structures/dimensions) in the core — keeps existing worlds
  compatible.
- ❌ No real-money / external anything. Gambling is purely in-game cosmetic-of-progression.
- ❌ Not a "creative mode" button — rewards lubricate progression, they don't hand you the ATM
  Star.
- ❌ No edits to other mods' files; we only *add* (items, loot tables, recipes, events).

## 6. Technical architecture

- **Platform:** NeoForge, MC **1.21.1** (match ATM10 7.x). Java 21.
- **Soft dependencies:** JEI, JADE, FTB Quests, Allthemodium, Mekanism (for reward/marker
  item references). Soft so the mod still loads if a dep is absent.
- **Java responsibilities:** PPT data attachment + milestone latching; event hooks
  (`LivingDeathEvent`, `BlockEvent.BreakEvent`, advancement/pickup events, dimension change);
  Cache item + right-click open; weighted rarity roll; anti-farm rate limiter; config.
- **Data-driven content (datapack/KubeJS):** every `{PPT × rarity}` reward pool is a **loot
  table**; marker→PPT mapping and rarity weights are data/config. → tune without recompiling.
- **Config:** master on/off; per-feature toggles; drop rates; rarity weights; anti-farm caps;
  "purist" preset that disables most goodies.
- **Repo layout (planned):**
  ```
  docs/            # the ATM10 wiki (reference) — already here
  SPEC.md          # this file
  mod/             # the NeoForge project (to be scaffolded)
    src/main/java/...        # PPT engine, events, Cache item, config
    src/main/resources/
      data/<modid>/loot_tables/caches/ppt<N>/<rarity>.json
      assets/...
  ```

## 7. Compatibility & worlds

- **Existing world:** ✅ supported. Core is items/loot/recipes/events — no chunk regen needed.
  Noah's current world (only walked around, no progression) loses nothing; PPT simply starts
  at 0.
- **Caveat for later:** if any post-MVP feature ever adds worldgen, it only appears in new
  chunks — at that point a fresh world is nicer but never required for the core.
- **Dev/testing:** use a separate creative test world; consider a debug command to set PPT and
  force-roll a rarity for fast iteration.

## 8. Open decisions

1. **Mod name** — "All The Goodies"? "ATM Caches"? "Modium Cache"? (provisional: All The Goodies)
2. **PPT signal set** — which exact markers latch each tier (FTB quest chapters vs item
   milestones vs both). Leaning: item milestones as the reliable base, FTB quests as a bonus
   signal when present.
3. **Reward aggressiveness** — how much should a jackpot accelerate progression? (Default
   stance: meaningful boost, never a full skip except rare high-PPT sub-components.)
4. **Drop feel** — target Caches-per-hour of active play at each tier (needs playtest tuning).
5. **Single Cache that scales vs. distinct Bronze/Silver/Gold caches.** (Leaning single +
   later Prime.)

## 9. Decision log

- **2026-06-26** — Project reframed: the "goodie bags" are a CS:GO-crate-style
  **progression-aware loot system** (now the headline feature); QoL helpers become supporting
  and double as rewards. MVP = full Cache loop + guidebook + JADE tooltips + ATM Star Tracker.
  Core stays worldgen-free to keep existing worlds compatible (Noah will continue his current
  world).
