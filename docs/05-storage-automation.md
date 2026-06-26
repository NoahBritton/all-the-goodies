# 05 · Storage & Automation

**Pack:** ATM10 · MC 1.21.1 · NeoForge. Recipe facts marked **✅ JEI**.

Digital storage + autocrafting is what turns the ATM Star from "impossible grind" into "queue
it and wait." ATM10 ships **three** competing systems plus Create.

## AE2 vs Refined Storage — pick one

Both are digital storage + autocrafting networks. Running both is wasteful — **choose a
primary.**

| Aspect | **Applied Energistics 2 (AE2)** | **Refined Storage (RS)** |
|---|---|---|
| Philosophy | Complex, high control, scalable | Simpler, plug-and-play |
| **Channels** | **Yes** — each device uses a **channel**; cables carry **8** (dense **32**). Plan **Controllers / P2P / cable colors** or devices go offline. **#1 AE2 beginner trap.** | **None** — connect anything anywhere. Much friendlier. |
| Storage disk | Many items but **max 63 distinct types/disk** | Per-item count, **no type limit** |
| Autocrafting | **Crafting CPU** + **Molecular Assemblers** + Pattern Providers; very powerful & parallel | **Crafters** (no separate CPU/assembler); simpler |
| Power draw | Lower idle drain | Higher passive drain |
| Best for | Deep control, huge scale, P2P, complex craft trees | Fast, simple storage with minimal planning |

> **Takeaway:** **RS = easier start, AE2 = higher ceiling.** AE2 is what most people use to
> *autocraft the ATM Star itself*. If you choose AE2, learn **channels** first: use a
> **Controller** + **Dense Cable**, or **P2P Tunnels**, to fan channels out.

## Mekanism storage/automation

- **QIO — Quantum Item Orchestrator (Mekanism):** Mekanism's digital storage. **QIO Drive
  Array** (holds **QIO Drives**) + a **QIO Dashboard** to view/search. **Drive Array and
  Dashboard must share a frequency** — mismatch shows an empty terminal (common confusion).
  No channels; great mass storage, less craft-automation depth than AE2/RS. With **Applied
  Mekanistics** you can even view QIO through AE2 terminals.
- **Factory machines (Mekanism):** **Basic (3) → Advanced (5) → Elite (7) → Ultimate (9)**
  parallel versions of Smelter/Crusher/Enrichment/Injection/etc. Essential for scaling ore
  processing. Trap: **side I/O colors + gas routing** per slot row — use the **side-config
  GUI**, **Pressurized Tubes**, and a **Configurator**.

## Create (Create) — mechanical automation

- Kinetic (**SU**, not FE) automation: **Crushing Wheels** (2× ore), **Mechanical Press/Mixer**
  (alloys, compacting), **Mechanical Crafters** (automate shaped recipes), **Contraptions**
  (moving drills/harvesters/deployers).
- **ATM Star relevance:** Create is **required** for some endgame component recipes (the
  Improbable Probability Device, Nexium Emitter, and Philosopher's Fuel each have a Create
  alternative ingredient — **✅ JEI**). Mechanical Crafters mass-produce intermediates cheaply.

## Item / fluid / energy logistics

- **Pipez (Pipez)** — dead-simple tiered pipes for items/fluids/energy/gas with filters &
  round-robin. **Beginner-friendly default.**
- **Laser IO (Laser IO)** — GUI cards moving **items, fluids, AND energy** through one node.
  Steeper config, extremely flexible; an ATM favorite for hooking machines to AE2/RS.
- **Modular Routers (Modular Routers)** — one Router block runs **modules** (pull/send/
  distribute/filter). Great for many-to-one filtered logistics.
- **Mekanism transport** — **Logistical Transporters** (items), **Mechanical Pipes** (fluids),
  **Pressurized Tubes** (gases — needed for the ore chain), **Universal Cables** (FE).

> **Rule of thumb:** **Pipez** for "just move it," **Laser IO** for "everything through one
> node," **Modular Routers** for filtered logistics, **Mekanism tubes** for gases.

## Why this matters for the ATM Star

The Star's components pull from a dozen mods at once. Realistically you need **mature power
(≥1 GFE on hand)**, an **AE2/RS autocraft network**, and **Mekanism/Create processing**
online *before* the Star is achievable — the network is what assembles the final recipe.

> 🪝 **Build hooks.** The two walls are **AE2 channels** (silent offline devices) and
> **Mekanism factory side-config** (nothing processes). A helper mod could ship a "starter
> storage core" that behaves channel-free like RS but upgrades into AE2, or pre-configured
> factory blocks. See [11](11-mod-build-opportunities.md).

## Sources

- [Refined Storage vs AE2 — Refined Mods](https://refinedmods.com/refined-storage/comparing-refined-storage-with-applied-energistics.html)
- [Mekanism QIO](https://wiki.aidancbrady.com/wiki/QIO) · [Mekanism Upgrades](https://wiki.aidancbrady.com/wiki/Upgrades)
- [Getting Started with Modular Routers — Jangro](https://jangro.com/2024/07/31/getting-started-with-modular-routers-minecraft-mod-a-practical-guide)
- [ATM10 Getting Started (automation) — Jangro](https://jangro.com/2024/07/13/all-the-mods-10-getting-started-guide)
