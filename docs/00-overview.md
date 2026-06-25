# 00 · Overview

**Pack:** ATM10 · MC 1.21.1 · NeoForge · ~500 mods · v7.0 (June 2026). Recipe facts marked **✅ JEI**.

## What ATM10 is

All The Mods 10 is a **"kitchen-sink" modpack** by the AllTheMods team: ~500 mods spanning
tech, magic, exploration, automation, and farming bundled into one world. It is **not
linear** — you pick your path across mod ecosystems — but the pack overlays a single
capstone goal that pulls from nearly every system: crafting the **ATM Star
(`allthetweaks:atm_star`)**. "Build toward the ATM Star" is the de facto win condition, and
the [FTB Quests](#ftb-quests-the-progression-spine) line routes you there.

- **Loader:** NeoForge (ATM10 is fully NeoForge — not a Forge pack).
- **Current release:** v7.0 (May 2026). Early builds were `0.x` (2024); the line progressed
  through `6.x` into `7.0`.
- **Distribution:** CurseForge.

## JEI — your most-used tool

**JEI (Just Enough Items)** is the recipe/usage lookup overlay and the single most important
QoL tool in the pack.

- Hover an item, press **R** (or left-click) → its **recipe**.
- Hover an item, press **U** (or right-click) → its **uses**.
- There has been community discussion about swapping JEI for **EMI** (issue #2456), and some
  point releases removed EMI to ship JEI only. **✅ JEI** — confirm which viewer your install
  ships and its keybinds.

> Practically every "how do I make this?" question in this wiki resolves to: *open JEI, press
> R*. The helper mod should assume JEI is present and integrate with it (recipe categories,
> info tooltips) rather than fight it.

## FTB Quests — the progression spine

**FTB Quests** is the structured questline. The **Quest Book** lives at the **top-left of the
inventory screen** (and has a keybind).

- Quests are grouped into **chapters** with tasks, dependencies, and rewards.
- Chapter 2 is literally **"The Star"** (`config/ftbquests/quests/chapters/chapter_2_the_star.snbt`),
  walking you through the Runic Star Altar build and the ATM Star components.
- Quests **hand out reward items and loot bags**, so completing them is materially worth it —
  not just guidance. This is the closest thing the pack has to a tutorial.

## How progression is gated

Gating is mostly **material/tool-tier based**, not hard locks:

1. **The metal ladder (the dominant gate).** Allthemodium → Vibranium → Unobtainium, where
   **each ore can only be mined by the previous tier's pickaxe**. This single mechanic paces
   the entire mid-to-late game. See [02 · Progression Ladder](02-progression-ladder.md).
2. **The FTB Quest line** creates a soft, recommended order on top of that.
3. **The ATM Star recipe** forces breadth: its components pull from dozens of different mods
   (Mekanism, AE2, Powah, Ars Nouveau, Industrial Foregoing, Apotheosis, L'Ender's
   Cataclysm, Productive Bees, and more), so you cannot ignore whole mod categories.

> 🪝 **Build hooks (for the helper mod).** The two biggest "new player has no idea what to do
> next" gaps are: (a) the quest book is easy to miss / overwhelming, and (b) JEI tells you a
> *recipe* but never *where in the world* to get the inputs. A helper add-on can add an
> in-world "what now?" guide item, signpost the quest book, and annotate gating items with
> where they come from. See [11 · Build Opportunities](11-mod-build-opportunities.md).

## Sources

- [All the Mods 10 — CurseForge](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10) · [v7.0 file](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10/files/8091114)
- [All the Mods 10 — ModpackIndex](https://www.modpackindex.com/modpack/85233/all-the-mods-10-atm10)
- [Quest System — DeepWiki (AllTheMods/ATM-10)](https://deepwiki.com/AllTheMods/ATM-10/2-quest-system)
- [Replace JEI with EMI — ATM-10 issue #2456](https://github.com/AllTheMods/ATM-10/issues/2456)
- [Introduction | ATM10 — SiriusMC Wiki](https://wiki.siriusmc.net/books/modpack-guides-and-tutorials/page/introduction-atm10)
- ATM10 KubeJS: `config/ftbquests/quests/chapters/chapter_2_the_star.snbt` (AllTheMods/ATM-10 repo)
