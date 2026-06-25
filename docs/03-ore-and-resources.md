# 03 · Ore & Resource Processing

**Pack:** ATM10 · MC 1.21.1 · NeoForge. Recipe facts marked **✅ JEI**.

How you turn scarce ore into bulk ingots, and how you replace mining entirely with farms.

## Mekanism ore processing (the multiplier grind)

**Mekanism** multiplies metal yield by chaining machines, each tier adding machines + a
chemical, ending in a smelt. **Factory** versions (Basic/Advanced/Elite/Ultimate) process
many slots in parallel.

| Tier | Yield | Machine(s) added | Chemical/fluid | Chain (raw ore → ingot) | Traps |
|---|---|---|---|---|---|
| **1×** | 1× | Furnace / **Energized Smelter** | — | Ore → Ingot | Baseline |
| **2×** | 2× | **Enrichment Chamber** | — | Ore → 2 Dust → smelt | Simplest. Non-Mek 2×: **Hammer**, **Crushing Wheels (Create)** |
| **3×** | 3× | **Purification Chamber** + **Crusher** + Enrichment | **Oxygen** | Ore+O₂ → Clump → Crusher → Dirty Dust → Enrich → Dust → smelt | Need an **Electrolytic Separator** for Oxygen; gas piping |
| **4×** | 4× | **Chemical Injection Chamber** (+ above) | **Hydrogen Chloride** | Ore+HCl → Shard → Purify → Clump → … → smelt | HCl supply chain (Chlorine + Hydrogen) is the first real "plumbing wall" |
| **5×** | 5× | **Dissolution Chamber** + **Chemical Washer** + **Chemical Crystallizer** (+ above) | **Sulfuric Acid** | Ore+H₂SO₄ → Dirty Slurry → Wash → Clean Slurry → Crystallize → Crystal → feed 4× chain → smelt | **Biggest trap:** the 3 slurry machines have **no Factory versions**, slurry doesn't split evenly in pipes, sulfuric-acid plumbing is fiddly |

> **Practical advice:** most players run **3× or 4×** as the daily driver and only build **5×**
> for high-value metals — 5× infrastructure is large and doesn't parallelize well (you need
> ~5 Crystallizers per Dissolution Chamber).

## Digital Miner (Mekanism) — automated mining

- The **Digital Miner** scans a configurable **radius / Y range** and **teleports** matching
  blocks to its inventory — no tunnels.
- Configure with **filters** (item/tag/material). **Silk Touch upgrade** pulls *ores* (needed
  to feed the 5× slurry chain).
- Upgrades: **Speed, Energy, Range**, and critically the **Anchor Upgrade** = keeps its chunk
  loaded (only **one per machine**). Without it, mining stops when you walk away ("why did it
  stop?").
- ⚠️ It **cannot** mine the player-only ATM ores (Allthemodium/Vibranium/Unobtainium).

## Mystical Agriculture — grow your resources

Grow **resource crops** that drop **Essence**, which crafts into ingots/gems/mob drops —
replacing mining for most materials.

- **Essence tiers (low→high):** **Inferium → Prudentium → Tertium → Imperium → Supremium →
  Insanium** (Insanium is the ATM-tier top essence). Higher tiers = higher seeds, faster yield.
- **Seeds** are crafted at an **Infusion Altar + Infusion Pedestals** using matching-tier
  Essence + the target resource (mob seeds use **Soul Jars** filled via a **Soulium Dagger**).
- **Farmland matters:** plant on **Essence Farmland** (+~10% extra-seed chance) and match the
  farmland tier to the seed (+another ~10%). Planting on plain dirt loses these bonuses — a
  common newbie mistake.
- **Endgame:** Insanium seeds give the fastest growth/biggest yields for mass production. The
  ATM Star path also wants **Mystical Agradditions** items (**Nether Star Crux**,
  **Insanium Coal Block**, **Creative Essence**, **Witherproof Bricks**).

## Productive Bees (Productive Bees) — passive resources

Breed bees that produce **combs** → centrifuge into resources (metals, gems, even some
endgame mats). Niche vs Mystical Agriculture/Mekanism, but **fully passive** and the **only**
source of several ATM Star ingredients (**Soul Lava Bee**, **Withered Comb**, **Starry Bees →
ATM Star Shards**). See [09 · ATM Star](09-atm-star.md).

> 🪝 **Build hooks.** The 4×→5× jump is where most players stall (gas/acid plumbing). A helper
> mod could ship a compact "pre-plumbed" 5× kit or a single-block ore-quintupler "goodie" as a
> bridge, plus better tooltips explaining Oxygen/HCl/H₂SO₄ supply. The Essence Farmland bonus
> is invisible to newbies — a tooltip hint would help. See [11](11-mod-build-opportunities.md).

## Sources

- [Mekanism Ore Processing](https://wiki.aidancbrady.com/wiki/Ore_Processing) · [FTB Wiki](https://ftb.fandom.com/wiki/Ore_processing_(Mekanism))
- [Mastering Mekanism Ore Processing 2x–5x — Jangro](https://jangro.com/2024/12/22/mastering-mekanism-ore-processing-from-2x-3x-4x-to-5x)
- [Chemical Dissolution Chamber](https://wiki.aidancbrady.com/wiki/Chemical_Dissolution_Chamber) · [Digital Miner](https://wiki.aidancbrady.com/wiki/Digital_Miner) · [Anchor Upgrade](https://wiki.aidancbrady.com/wiki/Anchor_Upgrade)
- [Mystical Agriculture Getting Started — Blake's Mods](https://blakesmods.com/wiki/mysticalagriculture/guides/getting-started)
- [5x has no factory versions — Mekanism issue #339](https://github.com/mekanism/Mekanism-Feature-Requests/issues/339)
