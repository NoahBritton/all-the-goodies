# ATM Cache loot pools

Each opening resolves to one loot table at path:

```
caches/ppt<N>/<rarity>      →  data/allthegoodies/loot_table/caches/ppt<N>/<rarity>.json
```

- `<N>` = the opener's Player Progression Tier, **0–6** (see SPEC.md §3.1).
- `<rarity>` = `common | uncommon | rare | epic | legendary | mythic` (SPEC.md §3.2).

That's a **7 × 6 = 42** table matrix. Missing tables resolve to "no items" (safe), so fill
them in incrementally. Only `ppt0/common` and `ppt0/mythic` exist so far as examples.

Guidelines:
- Use `"type": "minecraft:advancement_reward"` (matches the param set the Cache rolls with).
- Magnitude should follow SPEC.md §3.2: common/uncommon = small, rare/epic = medium,
  legendary = large, **mythic = the "Knife"** (one standout item perfectly timed to that tier).
- Reference ATM item ids from `docs/09-atm-star.md` / the pack's JEI when authoring higher
  tiers (e.g. `allthemodium:*`, `mekanism:*`). Verify ids in-game before relying on them.
