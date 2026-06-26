# All The Goodies (mod)

A NeoForge add-on for **All The Mods 10** (Minecraft 1.21.1). Progression-aware loot crates
(the **ATM Cache**) plus quality-of-life goodies. Design: see [`../SPEC.md`](../SPEC.md).

## Status

**v0.1 scaffold** — compiles and registers the ATM Cache item + the progression-tier engine.
Opening a Cache rolls a rarity and draws from `caches/ppt<N>/<rarity>` loot tables (only the
`ppt0` examples exist so far). Drop sources, milestone detection, JADE tooltips, the guidebook
and the ATM Star Tracker are the next MVP steps.

## Build

Requires **JDK 21** and network access to `maven.neoforged.net` + `services.gradle.org`.

```bash
cd mod
gradle wrapper           # one-time: generates ./gradlew + the wrapper jar
./gradlew build          # builds the mod jar into build/libs
./gradlew runClient      # launches a dev client with the mod loaded
```

(If you have Gradle 8.x installed you can also just run `gradle build` directly.)

> ⚠️ This scaffold was authored in a sandbox **without** access to the NeoForge/Gradle
> artifact hosts, so it has **not yet been compiled**. JSON/TOML are validated and the code
> targets the NeoForge 1.21.1 API, but the first real `gradle build` is the verification step —
> run it in an environment that can reach those hosts.

## Layout

```
src/main/java/com/allthegoodies/
  AllTheGoodies.java                 # @Mod entry point
  registry/ATGItems.java             # item registrations (ATM Cache)
  progression/ATGAttachments.java    # PPT data attachment
  progression/ProgressionTier.java   # tier get/raise (latches upward)
  cache/RarityTier.java              # CS:GO-style rarities + weights
  cache/CacheRoller.java             # weighted rarity roll
  cache/ATMCacheItem.java            # open logic → loot table by {ppt × rarity}
src/main/resources/
  META-INF/neoforge.mods.toml
  assets/allthegoodies/...           # lang, item model (placeholder texture)
  data/allthegoodies/loot_table/caches/ppt<N>/<rarity>.json
```

## Next steps (from SPEC.md §5.1)

- Milestone event handlers that call `ProgressionTier.raiseTo(...)`.
- Cache drop hooks (mob kills / mining / chests / quests) + anti-farm limiter.
- Config (rates, weights, toggles).
- "What Now?" guidebook, JADE source/pickaxe tooltips, ATM Star Tracker.
- Fill the 7×6 loot-table matrix; add a real Cache texture.
