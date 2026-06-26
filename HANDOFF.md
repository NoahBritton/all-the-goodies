# Handoff — continue developing locally

This repo was built in a **remote sandbox that cannot reach the NeoForge / Gradle / Minecraft
artifact hosts**, so the mod has been *written* but never *compiled*. This document hands the
project to a **local** environment (your machine, or a Claude Code session running there) where
a real build can happen.

> **Read order for a fresh session:** this file → [`SPEC.md`](SPEC.md) (the design) →
> [`CLAUDE.md`](CLAUDE.md) (operating rules) → [`mod/README.md`](mod/README.md) (build).

---

## 1. What this project is

A NeoForge add-on for **All The Mods 10** (Minecraft **1.21.1**, NeoForge) that makes
progression friendlier for newcomers. Two parts live in this repo:

| Part | Path | Status |
|---|---|---|
| **ATM10 builder wiki** (reference knowledge base) | `docs/` | ✅ done, on `main` |
| **Project spec** (controlling design doc) | `SPEC.md` | ✅ done, on `main` |
| **The mod** ("All The Goodies") | `mod/` | 🔶 v0.1 scaffold, **unbuilt**, in **PR #3** |

The headline feature is the **ATM Cache**: CS:GO-crate-style loot that drops as you play and
pays out **progression-aware** rewards (the jackpot is always useful *right now*). Full design
in [`SPEC.md`](SPEC.md).

## 2. Current state (branches & PRs)

- **`main`** — the wiki + `SPEC.md` + `CLAUDE.md` + `.claude/settings.json`. Clean.
- **PR #1, #2** — merged (repo reset to wiki; spec added).
- **PR #3 — `claude/mod-scaffold`** — the mod scaffold. **OPEN, not merged**, because it could
  not be build-verified in the sandbox. *This is the first thing to handle locally.*

## 3. ⭐ Start here (local session)

```bash
git clone <repo> && cd <repo>
git fetch origin
git checkout claude/mod-scaffold      # the PR #3 branch with mod/

cd mod
# Pin a real ModDevGradle + NeoForge version first — see §6 "Version pins to verify".
gradle wrapper                        # one-time: creates ./gradlew + wrapper jar (needs JDK 21)
./gradlew build                       # THE verification step — downloads NeoForge, compiles
./gradlew runClient                   # launch a dev client; /give yourself allthegoodies:atm_cache and right-click
```

**Expected outcome & what to do:**
- ✅ **Builds clean** → merge PR #3 (squash), then continue with §5 "Next tasks".
- ⚠️ **Compile errors** → almost certainly small NeoForge 1.21.1 API mismatches in the 7 Java
  files (I wrote them without a compiler). Fix in place; see §6 for the likely suspects.

In-game smoke test: give yourself an `ATM Cache`, right-click → you should get a chat line like
`ATM Cache → Worn (COMMON) • tier 0 Stranded • N item(s)` and receive the rolled items. Rare
rolls (mythic ≈ 0.1%) are hard to hit by hand — use a creative test world and open many, or
temporarily bump the `MYTHIC` weight in `RarityTier.java` to test the jackpot path.

## 4. What's implemented vs. stubbed (in `mod/`)

**Implemented (v0.1):**
- `AllTheGoodies.java` — `@Mod` entry point, registers items + attachments.
- `registry/ATGItems.java` — registers the `atm_cache` item.
- `progression/ATGAttachments.java` + `ProgressionTier.java` — the **PPT** (0–6) data
  attachment, latches upward, `get` / `raiseTo` helpers.
- `cache/RarityTier.java` — 6 rarities, weights sum to 10000.
- `cache/CacheRoller.java` — weighted rarity roll.
- `cache/ATMCacheItem.java` — right-click → roll rarity → draw `caches/ppt<N>/<rarity>` loot
  table → grant + announce. Missing tables = no items (safe).
- Resources: `neoforge.mods.toml`, lang, placeholder item model (reuses the vanilla bundle
  texture), and example `ppt0/common` + `ppt0/mythic` loot tables.

**Stubbed / not built yet (the MVP gap):**
- **Nothing makes PPT go up yet** — `raiseTo()` exists but no milestone detectors call it.
- **Nothing drops Caches yet** — they only exist via `/give`.
- No config, no guidebook, no JADE tooltips, no ATM Star Tracker.
- Only 2 of the 42 loot tables exist. No real Cache texture.

## 5. Next tasks (MVP order — from SPEC.md §5.1)

1. **Milestone → PPT detectors.** Event handlers that call `ProgressionTier.raiseTo(player, N)`.
   Reliable signals: item pickup/craft of marker items (`allthemodium:allthemodium_ingot` → 3,
   `..vibranium_ingot` → 4, `..unobtainium_ingot` → 5, etc.), dimension entry, and FTB Quests
   chapter completion if present. Make the marker→tier map data/config-driven.
2. **Cache drop hooks** — low chance on mob kills (`LivingDeathEvent`), ore mining
   (`BlockEvent.BreakEvent`), chest opening, fishing, advancements; plus a **guaranteed** Cache
   on each PPT increase. Add a **soft rate-limiter** so AFK farms can't mint Caches.
3. **Config** (NeoForge `ModConfigSpec`) — master toggle, drop rates, rarity weights, anti-farm
   caps, a "purist" preset.
4. **Fill the loot matrix** — author the remaining `ppt<N>/<rarity>` tables (use `docs/09` +
   in-game JEI for ATM item ids; verify ids before relying on them).
5. **QoL items** — "What Now?" guidebook (Patchouli or custom), JADE source/pickaxe tooltips,
   **ATM Star Tracker** (live have/need checklist).
6. **Polish** — real Cache texture; later, the CS:GO roulette opening GUI + Prime Cache.

A `/give` + a debug command to **set PPT** and **force a rarity** will speed iteration a lot —
add one early.

## 6. Gotchas the sandbox couldn't catch

**Version pins to verify (do this before the first build):**
- `mod/build.gradle` uses `id 'net.neoforged.moddev' version '2.0.+'`. The dynamic `2.0.+`
  **failed to resolve in the sandbox** (likely the blocked network, but pin it anyway). Replace
  with a concrete current version — check the [NeoForge MDK](https://github.com/NeoForgeMDKs)
  or moddev-gradle releases.
- `mod/gradle.properties` pins `neo_version=21.1.193`. **Confirm that version exists** for
  1.21.1 (see <https://projects.neoforged.net/neoforged/neoforge>); bump if needed.

**API/format notes (correct as written, but where to look if the build complains):**
- `ATMCacheItem.use(...)` returns `InteractionResultHolder<ItemStack>` — correct for **1.21.1**.
  (It becomes `InteractionResult` in 1.21.2+, so do **not** "upgrade" it unless you also bump MC.)
- Loot tables live under `data/<ns>/loot_table/...` — **singular** `loot_table` (the 1.21 rename).
  Don't switch back to `loot_tables`.
- `AttachmentType.builder(...).serialize(Codec.INT).copyOnDeath().build()` — NeoForge 1.21.1 API.
- Loot tables declare `"type": "minecraft:advancement_reward"` to match the param set the Cache
  rolls with (`LootContextParamSets.ADVANCEMENT_REWARD`).
- The item model uses `minecraft:item/bundle` as a placeholder texture — replace with a real
  `allthegoodies:item/atm_cache` texture when you have art.

## 7. Operating rules (already in `CLAUDE.md`)

- **Standing authorization:** Claude may open PRs and merge low-risk, *verified*, self-contained
  work on its own judgment; **hold** large/destructive/unverified work for Noah. (PR #3 is held
  precisely because it's unverified — once it builds, that clears.)
- Work on **feature branches**, never commit to `main` directly. Prefer **squash merge**, delete
  the branch after.
- **Recipe/data facts are version-sensitive** — keep "verify in JEI" flags in the wiki, and
  verify ATM item ids in-game before hard-coding them into loot tables.
- Ship reward pools / tweaks as **data (loot tables / datapack / KubeJS)** where possible, not
  hardcoded Java, so they retune per pack version.

## 8. Open decisions for Noah (from SPEC.md §8)

1. **Mod name** — working title "All The Goodies" (`modid: allthegoodies`, item: "ATM Cache").
2. **Jackpot aggressiveness** — default: meaningful boost, never a full progression skip.
3. **One scaling Cache vs. Bronze/Silver/Gold** — currently one Cache + a planned rare "Prime".

Changing `modid` later is a rename across package names + resource folders + `neoforge.mods.toml`
— cheaper to settle the name **before** writing much more.

## 9. Environment note

To build/verify in a **remote** Claude Code session instead of locally, the web environment
would need a network policy that allowlists `maven.neoforged.net` and `services.gradle.org`
(and their CDNs). See <https://code.claude.com/docs/en/claude-code-on-the-web>. Otherwise,
local is the path — everything above assumes that.
