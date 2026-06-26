# ATM10 — Road to the ATM Star (Builder's Wiki)

A comprehensive, reference-grade guide to the Minecraft modpack **All The Mods 10
(ATM10)**, centered on the full progression "road to the ATM Star."

> **What this is for.** This wiki is written primarily as a **builder's knowledge base** —
> reference material for designing an ATM10 helper add-on (the "goodie bags" project): a mod
> that adds quality-of-life items and tweaks to make progression friendlier for players who
> **don't know the pack and aren't strong at Minecraft**. It doubles as a readable human
> guide. Pages [10](docs/10-newcomer-friction.md) and [11](docs/11-mod-build-opportunities.md)
> are the bridge from "guide" to "buildable mod design."

## Pack target & accuracy

| | |
|---|---|
| Pack | All The Mods 10 (ATM10) |
| Minecraft | 1.21.1 |
| Loader | NeoForge |
| Mod count | ~500 |
| Current release (June 2026) | ATM10 v7.0 (released May 2026) |
| Compiled | 2026-06-25, from web research + the pack's own KubeJS recipe scripts |

> ⚠️ **Accuracy caveat — read this.** ATM10 retunes recipes, quantities, and even which
> mods ship with nearly every release. Recipe-level claims here are marked **✅ JEI** where
> you should confirm against your installed version in-game (press **R** on an item for its
> recipe, **U** for uses). The ATM Star recipe in particular was extracted from the pack's
> KubeJS source and is accurate to that snapshot, but **treat exact counts as
> verify-before-you-hardcode** for any mod you build on top of it.

## The mod project

The helper add-on this wiki feeds into is specified in **[SPEC.md](SPEC.md)** — the controlling
design doc (headline feature: a CS:GO-crate-style, progression-aware loot system; plus QoL
helpers). That's the north-star document for the build; this `docs/` wiki is its reference base.

Picking the project up to develop locally? Start with **[HANDOFF.md](HANDOFF.md)** — current
state, build steps, what's done vs. next, and the version pins to verify.

## How to navigate

Start at **[docs/index.md](docs/index.md)** for the full table of contents, or jump in:

1. [Overview](docs/00-overview.md) — what ATM10 is, JEI, FTB Quests, how progression is gated
2. [Getting Started](docs/01-getting-started.md) — the first few hours
3. [The Progression Ladder](docs/02-progression-ladder.md) — the metal/alloy spine of the pack
4. [Ore & Resource Processing](docs/03-ore-and-resources.md) — Mekanism 2x→5x, Mystical Agriculture
5. [Power](docs/04-power.md) — generation, storage, transfer by stage
6. [Storage & Automation](docs/05-storage-automation.md) — AE2 vs Refined Storage, Mekanism, Create
7. [Dimensions](docs/06-dimensions.md) — where to go and what each gates
8. [Combat, Bosses & Magic](docs/07-combat-bosses-magic.md) — Cataclysm, Apotheosis, mob farms
9. [Key Mods Reference](docs/08-key-mods.md) — the progression-relevant mods, by role
10. [The ATM Star](docs/09-atm-star.md) — the full endgame recipe tree
11. [Newcomer Friction Points](docs/10-newcomer-friction.md) — where new/low-skill players get stuck
12. [Helper-Mod Build Opportunities](docs/11-mod-build-opportunities.md) — concrete "goodie bag" designs
