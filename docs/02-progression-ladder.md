# 02 · The Progression Ladder (the spine of the pack)

**Pack:** ATM10 · MC 1.21.1 · NeoForge. Recipe facts marked **✅ JEI**.

Everything in ATM10 hangs off one progression: the **Allthemodium → Vibranium →
Unobtainium** metal ladder (the **Allthemodium** mod), then their **alloys**, then the
**ATM Star**. Each tier's ore is **gated by the previous tier's pickaxe**, and all three are
**player-mine-only**.

## The metal ladder

```
Netherite pick ──mines──▶ Allthemodium ──make Allthemodium pick──▶
Vibranium ──make Vibranium pick──▶ Unobtainium
        │                  │                    │
        └──────── feed alloys + ATM Star ───────┘
```

| Metal (`allthemodium:…`) | Where found | Pick tier needed | Unlocks |
|---|---|---|---|
| **Allthemodium** | Overworld **Deep Dark** (below ~Y -40; glows in the dark), **Ancient Cities**, and the **Mining Dimension** (~Y 65–129) | **Netherite** pick (or Steam Drill) | Allthemodium tools/armor; **Allthemodium pick** → required for Vibranium |
| **Vibranium** | **Nether** near the ceiling (~Y 64–127); also **The Other** (~Y 0–40) | **Allthemodium** pick | Better tools; **Vibranium pick** → required for Unobtainium |
| **Unobtainium** | **The End** — End Highlands / outer islands (cross the gateway). Much rarer. | **Vibranium** pick | Top base metal; highest alloys + ATM Star components |

Key facts:

- All three picks have **mining speed ~10** (faster than Netherite) and each raises the
  **harvest level** that gates the next ore.
- **These ores are player-mine-only.** Quarries, the Digital Miner, fake players, and most
  automation **cannot** mine the raw ore blocks. (Huge source of "why won't my miner work?")
- **Smithing templates:** Allthemodium via brushing **Suspicious Clay** in Ancient Cities;
  Vibranium via **Suspicious Soul Sand** in Bastions; **Unobtainium templates come only from
  dungeon chests in The Other** — so visiting [The Other](06-dimensions.md) is mandatory for
  top-tier gear.

### Predictable stuck points

- "I found Vibranium/Unobtainium but can't mine it" → you don't have the correct-tier pick.
  The bar is **Allthemodium pick for Vibranium**, **Vibranium pick for Unobtainium**.
- "My quarry/Digital Miner gets no Allthemodium" → it's **player-mine-only**.
- Some high-tier tools (e.g. Mystical Agriculture Supremium) historically still couldn't mine
  these ores. **✅** in v7.0.

## The alloys (the rung above base metals)

The three base metals combine **pairwise**, each in a **different mod's machine** — the pack
deliberately forces cross-mod engagement. (Full recipes in [09 · ATM Star](09-atm-star.md).)

| Alloy | Machine (mod) | Headline ingredients **✅ JEI** |
|---|---|---|
| **Vibranium-Allthemodium** | **Energizing Orb (Powah)** | Allthemodium + Vibranium + 2× **Piglich Heart** + Compressed Nitro Crystal + huge FE |
| **Unobtainium-Allthemodium** | **Enchanting Apparatus (Ars Nouveau)** | Allthemodium + Unobtainium + 2× Piglich Heart + elemental essences + ~10k Source |
| **Vibranium-Unobtainium** | **Dissolution Chamber (Industrial Foregoing)** | Vibranium + Unobtainium + ~4× **Pink Slime** + 2× Piglich Heart |

- **Piglich Heart** (dropped by the **Piglich** mob in the Nether) is a **shared bottleneck**
  across all three alloys — a classic stall point. Set up a Piglich farm / Hostile Networks
  model early.
- The apex material is the **Awakened Unobtainium-Vibranium Alloy Block** — which is simply
  the Unobtainium-Vibranium alloy block **enchanted (Unbreaking I) + renamed at the Runic
  Star Altar**. There is **no separate "Pladium" item** (common myth).
- Alloys become the **best tools** (e.g. **Allthemodium Alloy Paxel/Blade**) — indestructible,
  highest harvest level.

> **Tip:** run raw ores through [Mekanism processing](03-ore-and-resources.md) before
> alloying — the ores are scarce (player-mine-only), so maximizing ingots per ore block
> matters.

## The capstone

Base metals → 3 alloys → **Awakened alloy** → fed (with 8 custom components) into the
**Runic Star Altar** to craft the **ATM Star**. The altar is a **Modern Industrialization
electric multiblock**, not a magic altar. Full tree: [09 · The ATM Star](09-atm-star.md).

> 🪝 **Build hooks.** The ladder's friction is (1) not knowing the pick-gating rule, (2) not
> knowing the ores are player-mine-only, and (3) the Piglich Heart grind. A helper mod could
> add a tooltip on each ore stating the required pick, and an early "Piglich lure" goodie to
> ease the shared bottleneck. See [11](11-mod-build-opportunities.md).

## Sources

- [ATM10 Mining and Ores Guide — all-themods.com](https://all-themods.com/mining-guide/)
- [Allthemodium Mod Wiki — minecraft-guides.com](https://www.minecraft-guides.com/mod/allthemodium/)
- [AllTheModium Ores — ModdedMC Wiki](https://moddedmc.net/wiki/allthemodium)
- [AllTheModium Ores Guide | ATM10 — SiriusMC Wiki](https://wiki.siriusmc.net/books/modpack-guides-and-tutorials/page/allthemodium-ores-guide-atm10)
- ATM10 KubeJS: `kubejs/server_scripts/modpack/atm_alloys.js`, `runic_multis/recipes/star_altar.js`
