# 09 · The ATM Star — Complete Recipe Tree

**Pack:** ATM10 · MC 1.21.1 · NeoForge. **Source of truth:** the pack's own KubeJS recipe
scripts in the `AllTheMods/ATM-10` repo (extracted via GitHub code search). Item IDs are
reproduced verbatim. The custom Star items live in **ATM Additions / All The Tweaks**
(namespace `allthetweaks`).

> ⚠️ **Read before you build on this.** This is accurate to the KubeJS snapshot researched on
> 2026-06-25, but ATM10 retunes the Star recipe across releases. **Confirm every quantity in
> JEI** against your install before hard-coding anything. Items not fully resolvable from
> source are flagged **✅ JEI**.

## Myth-busting (common wrong assumptions)

- **The Runic Star Altar is a Modern Industrialization electric multiblock**
  (`modern_industrialization:star_altar`), **not** an Ars Nouveau / Runic Matter altar. It is
  **powered by FE/EU**, not Soul Lava.
- **"~100 mb Soul Lava in the Star recipe" is a red herring.** Soul Lava appears only inside
  the **Dragon Soul** sub-craft (the **Soul Lava Bee**) and in unrelated Runic multiblocks —
  not the Star altar recipe.
- **There is no separate "Pladium" item.** The "Awakened Unobtainium-Vibranium Alloy" is the
  normal alloy block with **Unbreaking I + a custom name** applied at the altar.
- **AllTheCatalystium is NOT in the current Star recipe** — it exists as an item but has no
  recipe in the pack scripts and is not consumed by the Star (likely legacy). **✅ JEI.**

## 1. The Runic Star Altar

- **Block/multiblock:** `modern_industrialization:star_altar` — a `simpleElectricCraftingMultiBlock`
  named **"Runic Star Altar"**, controller **Runic Star Altar Block** (`allthemods:star_altar`).
- **Structure:** **15×8×15**, built mostly from **Polished Darkstone**, **Polished Darkstone
  Stairs**, **Arcane Polished Darkstone**, **Darkstone Pedestals**, and **Arcane Polished
  Darkstone Pillars**, with a **Magnetized Darkstone Pedestal** in the center. Place the
  controller and hit it with a **Modern Industrialization Wrench** to get the build hologram.
- **Hatches (MI):** add **Item Input/Output Hatches**, **Energy Input Hatches**, and an
  **Ars Nouveau Source Input**. Recipe is fed through input hatches; the Star exits the output.
- **Power:** EU/FE, recipe `star_altar(2048, 200)` → **2048 EU/t over 200 ticks**; stack
  **MI Upgrades** in the Altar Block to hit the FE/t. The quest cites needing a large FE/t
  boost. **✅ JEI** the exact buffer.

## 2. The ATM Star — direct altar recipe

From `kubejs/server_scripts/modpack/runic_multis/recipes/star_altar.js`, outputting
`allthetweaks:atm_star`. Note this is **larger than "8 components"** — it folds in the alloy
blocks and bulk mats directly:

| # | Count | Item ID | Mod |
|---|---|---|---|
| 1 | 28× | `allthemodium:unobtainium_allthemodium_alloy_block` | Allthemodium |
| 2 | 15× | `allthecompressed:nether_star_block_3x` | AllTheCompressed |
| 3 | 2× | `allthemodium:unobtainium_vibranium_alloy_block` + `{unbreaking:1, custom_name:"Awakened Unobtainium-Vibranium Alloy Block"}` | Allthemodium (the "Awakened" alloy) |
| 4 | 1× | `allthetweaks:oblivion_shard` | ATM Additions |
| 5 | 1× | `mysticalagradditions:creative_essence` | Mystical Agradditions |
| 6 | 1× | `allthetweaks:nexium_emitter` | ATM Additions |
| 7 | 1× | `allthetweaks:withers_compass` | ATM Additions |
| 8 | 1× | `allthetweaks:improbable_probability_device` | ATM Additions |
| 9 | 1× | `allthetweaks:dragon_soul` | ATM Additions |
| 10 | 1× | `allthetweaks:philosophers_fuel` | ATM Additions |
| 11 | 1× | `allthetweaks:pulsating_black_hole` | ATM Additions |
| 12 | 1× | `allthetweaks:dimensional_seed` | ATM Additions |
| 13 | 1× | `allthetweaks:patrick_star` (the **Infused Patrick Star** variant) | ATM Additions |
| → | out | `allthetweaks:atm_star` | ATM Additions |

So the **8 headline custom components** are: **Dragon Soul, Improbable Probability Device,
Dimensional Seed, Pulsating Black Hole, Nexium Emitter, Wither's Compass, Philosopher's Fuel,
and the (Infused) Patrick Star** — plus **Oblivion Shard**, **Creative Essence**, and the
**alloy/Nether-Star bulk**.

## 3. Each component (shaped crafting, mostly `att_items.js`)

### 3.1 Dragon Soul — `allthetweaks:dragon_soul` — pattern `CDA / SNI / BGE`
| Key | Item | Mod |
|---|---|---|
| C | `apothic_enchanting:infused_breath` | Apothic Enchanting |
| D | `occultism:soul_gem` | Occultism |
| A | Creature Catcher w/ `occultism:dragon_familiar` **or** `occultism:familiar_dragon` spawn egg | Just Dire Things / Occultism |
| S | `productivetrees:socotra_dragon_sapling` | Productive Trees |
| N | maxed `hostilenetworks:data_model` (Ender Dragon) | Hostile Networks |
| I | `allthemodium:piglich_heart_block` | Allthemodium |
| B | `productivebees:spawn_egg_configurable_bee` (**Soul Lava** type) | Productive Bees |
| G | `cataclysm:abyssal_sacrifice` | L'Ender's Cataclysm |
| E | `eternal_starlight:chain_of_souls` | Eternal Starlight |

### 3.2 Improbable Probability Device — `allthetweaks:improbable_probability_device` — `ABA / CGC / FDF`
| Key | Item | Mod |
|---|---|---|
| A | `mekanism:pellet_antimatter` | Mekanism |
| B | `ae2:singularity` | Applied Energistics 2 |
| C | `megacells:portable_item_cell_256m` **or** `modern_industrialization:blastproof_casing` | Mega Cells / MI |
| G | `irons_spellbooks:lightning_upgrade_orb` | Iron's Spells |
| D | `pneumaticcraft:aerial_interface` | PneumaticCraft |
| F | `ironfurnaces:million_furnace` | Iron Furnaces |

### 3.3 Dimensional Seed — `allthetweaks:dimensional_seed` — `ABC / DEF / GHI`
| Key | Item | Mod |
|---|---|---|
| A | `allthecompressed:netherrack_6x` | AllTheCompressed |
| B | `allthecompressed:dirt_6x` | AllTheCompressed |
| C | `allthecompressed:obsidian_5x` | AllTheCompressed |
| D | `allthetweaks:mini_exit` (Mini End Portal) | ATM Additions |
| E | `allthetweaks:mini_nether` | ATM Additions |
| F | `allthetweaks:mini_end` | ATM Additions |
| G | `allthecompressed:end_stone_5x` | AllTheCompressed |
| H | `allthecompressed:emerald_block_4x` | AllTheCompressed |
| I | `allthecompressed:diamond_block_4x` | AllTheCompressed |

### 3.4 Pulsating Black Hole — `allthetweaks:pulsating_black_hole` — `ABC / DEF / GHI`
| Key | Item | Mod |
|---|---|---|
| A | `oritech:nuke` | Oritech |
| B | `ae2:quantum_ring` | AE2 |
| C | `pneumaticcraft:micromissiles` | PneumaticCraft |
| D | `justdirethings:paradoxmachine` | Just Dire Things |
| E | `pocketstorage:psu_4` | Pocket Storage |
| F | `occultism:stable_wormhole` | Occultism |
| G | `rootsclassic:crystal_staff` | Roots Classic |
| H | `industrialforegoing:mycelial_explosive` | Industrial Foregoing |
| I | `evilcraft:lightning_bomb` | EvilCraft |

### 3.5 Nexium Emitter — `allthetweaks:nexium_emitter` — `A_B / _CF / GED`
| Key | Item | Mod |
|---|---|---|
| A | `powah:player_transmitter_nitro` | Powah |
| B | `ae2wtlib:wireless_universal_terminal` (charged) | AE2WTLib |
| C | `advanced_ae:quantum_multi_threader` | Advanced AE |
| F | `mekanism:module_gravitational_modulating_unit` | Mekanism |
| G | `aeinfinitybooster:infinity_card` | AE2 Infinity Booster |
| E | `immersiveengineering:tesla_coil` | Immersive Engineering |
| D | `modern_industrialization:large_advanced_motor` **or** `create:mechanical_arm` | MI / Create |

### 3.6 Wither's Compass — `allthetweaks:withers_compass` — `ABC / DEF / GHI`
| Key | Item | Mod |
|---|---|---|
| A | `productivebees:configurable_comb` (**withered**) | Productive Bees |
| B | `industrialforegoing:wither_builder` | Industrial Foregoing |
| C | `deeperdarker:heart_of_the_deep` | Deeper and Darker |
| D | `generatorgalore:netherstar_generator_64x` | Generator Galore |
| E | `irons_spellbooks:scroll` (wither skull, lvl 10) | Iron's Spells |
| F | `mysticalagriculture:witherproof_bricks` | Mystical Agriculture |
| G | `minecraft:tipped_arrow` (long wither) | Apothic Attributes |
| H | `ars_nouveau:glyph_wither` | Ars Nouveau |
| I | `mysticalagradditions:nether_star_crux` | Mystical Agradditions |

### 3.7 Philosopher's Fuel — `allthetweaks:philosophers_fuel` — `ABC / DEF / GHI`
| Key | Item | Mod |
|---|---|---|
| A | `generatorgalore:ender_generator` | Generator Galore |
| B | `ironfurnaces:rainbow_coal` | Iron Furnaces |
| C | `bigreactors:insanite_block` | Extreme Reactors |
| D | `modern_industrialization:uranium_fuel_rod_quad` **or** `create:blaze_burner` | MI / Create |
| E | `mysticalagradditions:insanium_coal_block` | Mystical Agradditions |
| F | `forbidden_arcanus:smelter_prism` | Forbidden & Arcanus |
| G | `mysticalagriculture:awakened_supremium_ingot_block` | Mystical Agriculture |
| H | `generatorgalore:magmatic_generator_64x` | Generator Galore |
| I | `evilcraft:dark_tank` (16k mb refined T4 fluid) | EvilCraft + Just Dire Things |

### 3.8 Oblivion Shard — `allthetweaks:oblivion_shard` — `DAB / ECF / BGD`
| Key | Item | Mod |
|---|---|---|
| A | `forbidden_arcanus:eternal_stella` | Forbidden & Arcanus |
| B | `evilcraft:piercing_vengeance_focus` | EvilCraft |
| C | `evilcraft:mace_of_destruction` (4k mb blood) | EvilCraft |
| D | `stevescarts:module_galgadorian_drill` | Steve's Carts |
| E | `cataclysm:meat_shredder` | L'Ender's Cataclysm |
| F | `cataclysm:void_forge` | L'Ender's Cataclysm |
| G | `twilightforest:snow_queen_trophy` | Twilight Forest |

### 3.9 (Infused) Patrick Star — `allthetweaks:patrick_star`
Two stages:
- **Base Patrick Star** — raw JSON in `kubejs/server_scripts/modpack/atm_star.js`. **Exact
  grid not extractable from source fragments. → ✅ JEI.**
- **Infused Patrick Star** (altar) — inputs include `4× enchanted_book{mending:1}`,
  `allthemodium:vibranium_allthemodium_alloy_ingot`,
  `allthemodium:unobtainium_allthemodium_alloy_ingot`, plus (per quest) **Infused Dragon's
  Breath**, **ATM Star Shards** (`allthetweaks:atm_star_shard`, from **Starry Bees**), the
  **Unobtainium-Vibranium alloy ingot**, and a base Patrick Star → renamed "Infused Patrick
  Star." Exact counts / Infused Dragon's Breath ID **→ ✅ JEI.**

> **ATM Star Shard** (`allthetweaks:atm_star_shard`) is **not crafted** — get it from
> **Starry Bees** (right-click a Patrick Bee with an ATM Star Block → Starry Bee → Starry Comb
> → Centrifuge → rare shards). Separate from the four `kubejs:atm_star_shard_1..4` fragments
> also used by the altar.

## 4. The alloy chain (`atm_alloys.js` + the altar)

| Alloy (output block) | Machine (mod) | Key inputs **✅ JEI** |
|---|---|---|
| **Vibranium-Allthemodium** | **Energizing Orb (Powah)** (~9 GFE) | Allthemodium block + Vibranium block + 2× `piglich_heart_block` + `nitro_crystal_block_2x` |
| **Unobtainium-Allthemodium** | **Enchanting Apparatus (Ars Nouveau)** | Unobtainium block (reagent); pedestals = 2× Piglich Heart + Air/Earth/Fire/Water essence blocks + Allthemodium block |
| **Vibranium-Unobtainium** | **Dissolution Chamber (Industrial Foregoing)** | ~4× `pink_slime_block` + Vibranium block + Unobtainium block + 2× Piglich Heart |
| **Awakened Unobtainium-Vibranium** (the apex) | **Runic Star Altar** `star_altar(2048,200)` | Unobtainium-Vibranium block + 4× awakened supremium essence + 4× `enchanted_book{unbreaking:1}` + 4× awakened supremium gemstone → enchanted/renamed block |

Order: **Allthemodium+Vibranium → Vib-Allthemodium**, **Allthemodium+Unobtainium →
Unob-Allthemodium**, **Vibranium+Unobtainium → Unob-Vibranium**, then **Awaken** the
Unob-Vibranium block at the altar. **Piglich Heart appears in all three base alloys** — the
single biggest material bottleneck.

## 5. Recommended acquisition order

1. **Mining/alloy backbone first.** Get Allthemodium→Vibranium→Unobtainium flowing; automate
   **Piglich Hearts, Nitro Crystals, Pink Slime, elemental essences**. You need **28×
   Unobtainium-Allthemodium blocks** and **15× Nether Star (3×) blocks** — start these early;
   they're the longest grind.
2. **Build the Runic Star Altar** (15×8×15) with MI hatches + energy + source. This also
   unlocks the **Awakened alloy** and **Infused Patrick Star** sub-recipes.
3. **Awaken** 2× Unobtainium-Vibranium blocks.
4. **Set up Starry Bees** (ATM Star Shards) + **Runic Enchanting** (Infused Dragon's Breath) →
   build the **Infused Patrick Star**.
5. **Knock out the components, easiest → hardest:**
   - **Dimensional Seed** (compressed blocks + mini-portals) — easiest.
   - **Philosopher's Fuel**, **Oblivion Shard**, **Pulsating Black Hole**.
   - **Improbable Probability Device** (antimatter, AE2 singularity, million furnace).
   - **Nexium Emitter** (top-tier AE2/Powah/Mekanism).
   - **Wither's Compass** (withered comb, wither builder, spell scroll).
   - **Dragon Soul** — usually last; pulls from the most mods.
6. **Oblivion Shard + Creative Essence**, then feed everything into the altar.

## Uncertain / verify in-game (✅ JEI)

1. **Base Patrick Star** grid (JSON not extractable from source fragments).
2. **Infused Patrick Star** full input counts + the **Infused Dragon's Breath** ID.
3. **AllTheCatalystium** — appears unused/legacy; confirm it's not in your install's recipe.
4. **ATM Star Shard** bee chain (comb/centrifuge/drop rate).
5. **Unobtainium-Allthemodium** Enchanting Apparatus full 8-pedestal set + Source cost.
6. Exact **block counts** inside the Powah/IF alloy recipes (some array tails inferred).

## Sources

- DeepWiki — [ATM Star & Custom Items](https://deepwiki.com/AllTheMods/ATM-10/3.1-atm-star-and-custom-items)
- `AllTheMods/ATM-10` KubeJS (via code search): `att_items.js`, `runic_multis/recipes/star_altar.js`, `atm_alloys.js`, `atm_star.js`, `startup_scripts/Modern-Industrialization/multiblocks/runic_star_altar.js`, `config/ftbquests/quests/chapters/chapter_2_the_star.snbt`
- [ATM Additions — CurseForge](https://www.curseforge.com/minecraft/mc-mods/atm-additions)
- Corroboration (search snippets): [SiriusMC ATM Star Guide](https://wiki.siriusmc.net/books/modpack-guides-and-tutorials/page/siriusmcs-atm-star-guide-atm10); YouTube "ATM10 EP62 Runic Star Altar"
