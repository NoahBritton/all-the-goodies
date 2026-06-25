# 11 · Helper-Mod Build Opportunities ("Goodie Bags")

**Pack:** ATM10 · MC 1.21.1 · NeoForge.

This is the **design payload**: concrete features for an ATM10 helper add-on that makes
progression friendlier for players who don't know the pack and aren't strong at Minecraft.
Each idea maps to friction points (Fxx) from [10 · Newcomer Friction](10-newcomer-friction.md).

> **Design stance.** *Ease the curve, don't flatten it.* The pack's identity is the long road
> to the ATM Star. Good goodies remove **confusion, tedium, and unfair deaths** — not the
> progression itself. Prefer **information, safety nets, and head-starts** over raw power.
> Everything should be **toggleable** (config / datapack) so server owners can dial it.

## Build approach (technical)

- **Loader/MC:** NeoForge, MC 1.21.1 (match ATM10). The add-on most naturally **depends on
  JEI, JADE, FTB Quests** and soft-depends on the mods it references.
- **Recipes as data:** ship recipes/quest tweaks as **KubeJS scripts or datapacks** (the way
  ATM itself does it — see [09](09-atm-star.md)), so they're easy to retune per pack version.
  This sidesteps the [accuracy caveat](09-atm-star.md): if a referenced recipe changes, you
  edit data, not Java.
- **Config-gate every goodie.** Newbie-friendly defaults, but a "purist" preset that disables
  most of it.

## Tier 1 — Orientation & information (highest leverage, lowest balance risk)

| Goodie | What it does | Solves |
|---|---|---|
| **"What Now?" Guidebook** (Patchouli-style) | An in-world book auto-given on first join that explains the first 10 steps, points at the Quest Book, and links the [progression ladder](02-progression-ladder.md) | F1, F2 |
| **Source annotations / JADE tooltips** | Tooltip on gating items and ores stating *where they come from* and *what pick mines them* (e.g. "Vibranium — Nether ceiling; needs an Allthemodium pick") | F3, F9, F10, F20 |
| **Boss-prep checklist item** | A consumable that shows a per-boss readiness checklist (gear tier, fire resist, melee vs ranged warning incl. the Cataclysm regen bug) | F4, F23, F24 |
| **Dimensional Atlas** | A book/item documenting each dimension's quirky entry method + prerequisites (Twilight diamond-pool, Bumblezone pearl, Orb of Prophecy, Lich-before-dark-forest) | F21, F22 |
| **Myth-corrector pages** | JEI info pages on the ATM Star debunking Soul-Lava/Pladium/Ars-altar myths and showing the real MI altar | F27 |

## Tier 2 — Starter goodie bags (head-starts, given via early quests)

| Goodie | What it does | Solves |
|---|---|---|
| **Day-One Starter Kit** | Quest reward: a pre-made Silent Gear pick (blueprint already resolved), an Ore Hammer, a stack of cooked food, and a Wooden Jetpack | F5, F7, F8 |
| **Pocket Recipe Cards** | One-use items that, when used, drop the exact ingredients for a tricky early machine (so the player sees what to gather) | F3 |
| **Waystone Starter** | A free activated Waystone at spawn so fast-travel works immediately | (QoL) |
| **"Piglich Lure"** | A craftable that increases Piglich spawn/heart drops in a small area, easing the shared alloy bottleneck — capped/configurable | F11 |

## Tier 3 — Safety nets (remove *unfair* deaths & disasters)

| Goodie | What it does | Solves |
|---|---|---|
| **Training Armor set** | Mid-tier armor with solid defensive Apotheosis affixes, given before the first dimension push, to survive the Warden/early bosses | F4, F23 |
| **Safe "Training Reactor"** | A single-block FE generator with a generous ceiling and **no meltdown**, as a bridge before Mekanism Fission | F14 |
| **Meltdown Insurance / Warning Totem** | An item that warns (or one-time prevents) a Fission meltdown wiping the base | F14 |
| **Grave/back-up goodies** | Ensure a death-grave mod is present and tooltip it; auto-backup Hostile Networks Data Models to counter the reset bug | F25 |

## Tier 4 — Tedium & tech-wall bridges (ease the grind, keep the goal)

| Goodie | What it does | Solves |
|---|---|---|
| **Pre-plumbed 5× Ore Kit** | A compact, mostly-assembled Mekanism 5× setup (or a single "ore quintupler" bridge block) so the acid/gas plumbing isn't a hard wall | F15 |
| **Channel-Free Storage Core** | A starter digital-storage block that behaves like Refined Storage (no channels) and **upgrades into AE2**, so people learn storage before channels | F18 |
| **Pre-configured Factory blocks** | Mekanism factory variants that ship with correct side-config presets | F16 |
| **Auto-Anchor Miner upgrade** | A Digital Miner variant with the Anchor built-in (or a tooltip nudge) | F17 |
| **Ethylene Starter loop** | A small pre-built Bio-Fuel→Ethylene gas-gen kit, with a tooltip explaining why Hydrogen alone is net-zero | F12, F13 |

## Tier 5 — Endgame signposting (the ATM Star itself)

| Goodie | What it does | Solves |
|---|---|---|
| **ATM Star Tracker** | An item/HUD that checklists all 8 components + the alloy/bulk mats, with live "have/need" counts pulled from your storage | F26 |
| **"Start These Early" prompt** | A quest/tooltip flagging the long grinds (28× Unobtainium-Allthemodium, 15× Nether Star blocks) the moment you reach mid-game | F28 |
| **Altar Build Helper** | A hologram/blueprint item (or Building Gadgets template) for the 15×8×15 Runic Star Altar, with a hatch-placement guide | F29 |
| **Component sub-guides** | Per-component pages (mirroring [09](09-atm-star.md)) showing the full ingredient tree and which mod each part comes from | F26 |

## Suggested MVP (first release)

If building incrementally, ship the **highest-leverage, lowest-risk** slice first:

1. **"What Now?" Guidebook** + **JADE source/pick tooltips** (F1, F2, F3, F9, F10).
2. **Day-One Starter Kit** quest reward (F5, F7, F8).
3. **Dimensional Atlas** + **Boss-prep checklist** (F21–F24).
4. **ATM Star Tracker** (F26).

These are almost entirely **information + small head-starts** — they make the pack legible to
a newcomer without touching the core progression, which is the safest possible first step and
the most useful for the stated audience.

## Cross-reference

Every Fxx above is defined in [10 · Newcomer Friction Points](10-newcomer-friction.md). The
systems each goodie touches are detailed in pages [01](01-getting-started.md)–[09](09-atm-star.md).

## Sources

Design synthesis from pages [00](00-overview.md)–[10](10-newcomer-friction.md). No external
sources beyond those cited on the referenced pages.
