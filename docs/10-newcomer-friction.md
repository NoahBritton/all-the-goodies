# 10 · Newcomer Friction Points

**Pack:** ATM10 · MC 1.21.1 · NeoForge.

This page catalogs **where new and low-skill players predictably get stuck** in ATM10. It is
the input to [11 · Helper-Mod Build Opportunities](11-mod-build-opportunities.md) — every
friction point below (Fxx) has at least one proposed mitigation there.

Each entry: the wall, why it stops people, and a severity for a not-good-at-Minecraft player.

## A. Orientation & "what do I do?"

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F1** | **~500 mods, no obvious start** | Paralysis; the player opens the inventory and has no idea where to begin | 🔴 High |
| **F2** | **Quest Book is easy to miss** | The top-left FTB Quests icon goes unnoticed; it's the actual tutorial | 🔴 High |
| **F3** | **JEI tells you recipes, never *where* to get inputs** | You learn *how* to craft X but not which dimension/mob/biome the raw input lives in | 🔴 High |
| **F4** | **Modded combat & hunger are harsher** | New dimensions and bosses kill underprepared players who came from vanilla | 🟠 Med |

## B. Early game (first hours)

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F5** | **Silent Gear needs a blueprint first** | Parts won't combine without a crafted blueprint; looks broken | 🟠 Med |
| **F6** | **Vein Miner is a hold-key (`` ` ``)** | Players don't realize it's hold-to-use, or rebind/lose the grave key | 🟡 Low |
| **F7** | **Early starvation** | Vanilla wheat is slow; players don't know about Farmer's Delight foraging | 🟠 Med |
| **F8** | **No early ore doubling** | Missing the Ore Hammer wastes scarce early ore | 🟡 Low |

## C. The metal ladder

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F9** | **"I can't mine this ore"** | Pick-gating is invisible: Vibranium needs an Allthemodium pick, Unobtainium needs a Vibranium pick | 🔴 High |
| **F10** | **ATM ores are player-mine-only** | Quarries/Digital Miner get nothing; players waste time automating the impossible | 🔴 High |
| **F11** | **Piglich Heart bottleneck** | All three alloys need Piglich Hearts; hunting a rare Nether mob stalls progress | 🟠 Med |

## D. Power, ore processing, automation

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F12** | **Create SU ≠ FE** | Players try to power a Mekanism machine with a water wheel and nothing happens | 🟠 Med |
| **F13** | **Hydrogen is net-zero fuel** | Burning plain Hydrogen yields no net power; must use Ethylene | 🟡 Low |
| **F14** | **Fission Reactor meltdown** | Overheating/coolant loss irradiates and destroys the base — catastrophic | 🔴 High |
| **F15** | **Mekanism 4×→5× plumbing wall** | Oxygen/HCl/Sulfuric-Acid gas chains + no Factory versions for slurry machines | 🔴 High |
| **F16** | **Mekanism factory side-config / gas routing** | Wrong I/O colors/sides = nothing processes; no feedback why | 🟠 Med |
| **F17** | **Digital Miner Anchor Upgrade** | Without it, mining silently stops when you leave the chunk | 🟡 Low |
| **F18** | **AE2 channels** | Devices silently go offline; needs Controllers/Dense cable/P2P planning | 🔴 High |
| **F19** | **QIO frequency mismatch** | Dashboard shows empty because frequencies don't match | 🟡 Low |
| **F20** | **Mystical Agriculture Essence Farmland** | Planting on plain dirt loses the extra-seed bonus; invisible to newbies | 🟡 Low |

## E. Dimensions & bosses

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F21** | **Un-guessable portal/entry methods** | Twilight (diamond in flower-ringed pool), Bumblezone (pearl into beehive), Eternal Starlight (Orb of Prophecy) | 🟠 Med |
| **F22** | **Twilight Forest boss-order wall** | The "you are not worthy" dark forest needs the Lich (which needs the Naga first) | 🟠 Med |
| **F23** | **The Warden / underprepared deaths** | Deep Dark Warden one-shots early gear; needed for Allthemodium | 🔴 High |
| **F24** | **Cataclysm bosses can be unkillable (bug)** | Cross-mod regen/damage-cap interactions; ranged/DoT may not work (issue #2559) | 🟠 Med |
| **F25** | **Hostile Networks models reset (bug)** | Data Models can lose levels; lost grind (issues #1034/#1395) | 🟡 Low |

## F. The endgame (ATM Star)

| ID | Friction | Why it stops people | Severity |
|---|---|---|---|
| **F26** | **Sheer breadth of the Star recipe** | 8 components pulling from ~30 mods; overwhelming to track | 🔴 High |
| **F27** | **Myths/misinformation** | Old guides claim Soul Lava / Pladium / Ars altar — wrong for current ATM10 (see [09](09-atm-star.md)) | 🟠 Med |
| **F28** | **The longest grinds aren't signposted** | 28× Unobtainium-Allthemodium blocks + 15× Nether Star blocks should be started early but aren't obvious | 🟠 Med |
| **F29** | **Building the MI Runic Star Altar** | 15×8×15 multiblock with MI hatches/energy/source is a big, unfamiliar build | 🟠 Med |

## Priorities for the helper mod (top targets)

The highest-leverage friction for a "make progression easier for someone who doesn't know the
pack" mod, ranked:

1. **F1/F2/F3** — orientation: what to do, the quest book, and *where* inputs come from.
2. **F9/F10** — the pick-gating + player-mine-only rules (pure information gaps).
3. **F23/F14/F24** — the deaths/disasters (Warden, meltdown, unkillable bosses).
4. **F15/F18** — the two big tech walls (5× plumbing, AE2 channels).
5. **F26/F28** — endgame breadth + un-signposted long grinds.

See [11 · Build Opportunities](11-mod-build-opportunities.md) for concrete designs.

## Sources

Synthesized from pages [00](00-overview.md)–[09](09-atm-star.md) and their sources; bug
references: [ATM-10 #2559](https://github.com/AllTheMods/ATM-10/issues/2559),
[#1034](https://github.com/AllTheMods/ATM-10/issues/1034),
[#1395](https://github.com/AllTheMods/ATM-10/issues/1395).
