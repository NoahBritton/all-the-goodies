# 04 · Power

**Pack:** ATM10 · MC 1.21.1 · NeoForge. Numbers are approximate / version-sensitive — **✅ JEI**.

## Energy basics

- ATM10's universal energy is **FE (Forge Energy) / RF (Redstone Flux)** — 1:1 interchangeable;
  almost every machine accepts FE/RF.
- **Mekanism** internally uses **Joules (J)** but auto-converts (≈ **1 FE ≈ 2.5 J**). Same
  power, different unit — the "kJ" tooltips confuse newcomers.
- **Create** uses **Stress Units (SU)**, which is **mechanical power, NOT FE**. A water wheel
  cannot power a Mekanism machine without a bridge addon. **This trips up nearly everyone.**
- Big numbers use SI prefixes: k / M / G. "200 MRF/t" = 200,000,000 RF per tick.

## Power generation by stage

### Early game
- **Heat Generator (Mekanism)** — usually your first generator (needs **no steel**). Burns
  coal/charcoal/lava; tiny output, boosted next to lava.
- **Bio-Generator (Mekanism)** — burns **Bio Fuel** (~5.6 kJ/item); pair with a crusher.
- **Thermal dynamos (Thermal)** — **Stirling / Magmatic / Compression Dynamo**; cheap,
  modular, augmentable, beginner-friendly FE.
- **Powah starter tier** (Powah buffed early gens, so starting here feels good):
  **Thermoelectric Generator** (hot block + cold block; **Soul Lava** = 9× RF/mb),
  **Magmator** (burns lava), **Furnator** (solid fuel). **Solar Panel (Powah)** exists but is
  deliberately expensive — a poor early choice.
- **Create** gives cheap *mechanical* automation early (water wheels/windmills → presses,
  mixers, crafters) — great for processing, just remember SU ≠ FE.

### Mid game
- **Powah Reactors (Powah)** — the workhorse. Tiers **Starter → Basic → Hardened → Blazing →
  Niotic → Spirited → Nitro**. Fuel is **Uraninite** (Uranium through the **Energizing Orb**).
  Place 36 same-tier blocks to auto-form the multiblock. "Set and forget," clean, scales
  massively (Nitro ≈ 770M FE/fuel at low burn). **✅ JEI** numbers.
- **Gas-Burning Generator (Mekanism)** — burn **Ethylene/Ethene** (Bio Fuel + Hydrogen via
  Pressurized Reaction Chamber), up to ~72 kFE/t. ⚠️ **Burning plain Hydrogen is net-zero** —
  the Electrolytic Separator costs as much as the burn yields. Use **Ethylene**.

### Late game (mega power)
- **Mekanism Fission Reactor** — multiblock burning **Fissile Fuel**; heats coolant (water→steam,
  or **Liquid Sodium** for far higher throughput); pipe steam to an **Industrial Turbine** for
  FE. Produces **Nuclear Waste** → Polonium/Plutonium (feeds Fusion). ⚠️ **Can melt down and
  irradiate your base** if overheated / starved of coolant — the pack's biggest "you will die"
  trap.
- **Mekanism Fusion Reactor** — endgame, **safe** (no meltdown). Consumes **Deuterium +
  Tritium** (or **D-T Fuel**). ATM10's quest target is **~200 MRF/t** with direct D-T
  injection (issue #794). Needs **Polonium** (from fission waste) and **Laser Amplifiers** to
  ignite — expensive.
- **Extreme Reactors (Extreme Reactors)** — the **Bigger Reactors** lineage. **Passive-cooled**
  → FE directly (simpler); **active-cooled** → Steam → **Reactor Turbine** for huge FE. Pro
  tip: cool with **Liquid Sodium** → Heat Exchanger → more steam → more turbines. The
  **Insanite Block** here is an ATM Star ingredient.

## Storage & transfer

**Storage**
- **Energy Cells (Powah)** — tiered FE batteries; ubiquitous in ATM10.
- **Induction Matrix (Mekanism)** — scalable multiblock battery; the go-to for fusion/fission
  scale. Capacity & I/O set by the **Cells/Providers** installed.

**Transfer**
- **Energy Cables (Powah)** / **Universal Cables (Mekanism, Basic→Ultimate)** — tiered FE.
- **Energy ducts (Thermal)** / **Pipez (Pipez)** — simple FE pipes.
- **Flux Networks (Flux Networks)** — **wireless FE** (Flux Plug input, Flux Point output).
  Hugely popular as the late-game answer to avoid running cables; usually how you tie all
  generators + the Induction Matrix together.

## Recommended path

1. **Early:** a Thermal/Powah generator or Heat Generator → enough for first machines.
2. **Mid:** a **Powah Reactor** (Hardened/Blazing) + **Energy Cells** + **Flux Networks** for
   distribution. This comfortably runs an ore-processing base.
3. **Late:** **Mekanism Fission → Turbine** (careful!) or **Extreme Reactors**, stored in an
   **Induction Matrix**, then **Fusion** for the ATM Star's huge FE needs (the
   Vibranium-Allthemodium alloy alone wants ~9 GFE in the Energizing Orb).

> 🪝 **Build hooks.** Three friction points: SU≠FE confusion, the Hydrogen-is-net-zero trap,
> and Fission meltdowns wiping a base. A helper mod could add a cheap, safe "training reactor"
> goodie with a generous FE ceiling and no meltdown, plus tooltips clarifying SU vs FE. See
> [11](11-mod-build-opportunities.md).

## Sources

- [Powah — All The Guides](https://allthemods.github.io/alltheguides/atm9/powah/) · [Powah reactors — Enigmatica wiki](https://wiki.enigmatica.net/enigmatica6/energy-generation/energy-generation/powah)
- [Mekanism Gas-Burning Generator](https://wiki.aidancbrady.com/wiki/Gas-Burning_Generator) · [Heat Generator](https://wiki.aidancbrady.com/wiki/Heat_Generator) · [Fusion Reactor](https://wiki.aidancbrady.com/wiki/Fusion_Reactor)
- [Fusion 200 MRF/t — ATM-10 issue #794](https://github.com/AllTheMods/ATM-10/issues/794)
- [Extreme Reactors — All The Guides](https://allthemods.github.io/alltheguides/atm9/extremereactors/)
