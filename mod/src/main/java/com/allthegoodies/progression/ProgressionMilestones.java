package com.allthegoodies.progression;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Marker → PPT mappings: obtaining/mining/entering these latches the player's tier upward.
 *
 * <p><b>Every id here was verified against the installed ATM10 jars (2026-06-26)</b> — notably
 * Mekanism uses {@code ingot_osmium}/{@code ingot_steel} (not {@code *_ingot}), and the ATM Star
 * is {@code allthetweaks:atm_star} (not {@code allthemodium}). This table is intended to move to
 * config (SPEC §6); it stays a clean static map for now so it's trivially tunable.</p>
 */
public final class ProgressionMilestones {

    /** Item id → minimum PPT implied by obtaining it (pickup / craft / smelt). */
    public static final Map<ResourceLocation, Integer> ITEM_TIER = new HashMap<>();
    /** Block id → minimum PPT implied by mining it. */
    public static final Map<ResourceLocation, Integer> BLOCK_TIER = new HashMap<>();
    /** Dimension id → minimum PPT implied by entering it. */
    public static final Map<ResourceLocation, Integer> DIMENSION_TIER = new HashMap<>();

    private static void item(String id, int tier) { ITEM_TIER.put(ResourceLocation.parse(id), tier); }
    private static void block(String id, int tier) { BLOCK_TIER.put(ResourceLocation.parse(id), tier); }
    private static void dim(String id, int tier) { DIMENSION_TIER.put(ResourceLocation.parse(id), tier); }

    static {
        // ── PPT 1 — Established (iron) ────────────────────────────────────────────
        item("minecraft:iron_ingot", 1);

        // ── PPT 2 — Industrial (Mekanism ore doubling) ───────────────────────────
        // NOTE: Mekanism's ingot ids are prefix-style: ingot_osmium / ingot_steel.
        item("mekanism:enrichment_chamber", 2);
        item("mekanism:ingot_osmium", 2);
        item("mekanism:ingot_steel", 2);

        // ── PPT 3 — Allthemodium ─────────────────────────────────────────────────
        item("allthemodium:allthemodium_ingot", 3);
        item("allthemodium:raw_allthemodium", 3);
        block("allthemodium:allthemodium_ore", 3);
        block("allthemodium:allthemodium_slate_ore", 3);
        dim("allthemodium:mining", 3);

        // ── PPT 4 — Vibranium ────────────────────────────────────────────────────
        item("allthemodium:vibranium_ingot", 4);
        item("allthemodium:raw_vibranium", 4);
        block("allthemodium:vibranium_ore", 4);
        block("allthemodium:other_vibranium_ore", 4);

        // ── PPT 5 — Unobtainium ──────────────────────────────────────────────────
        item("allthemodium:unobtainium_ingot", 5);
        item("allthemodium:raw_unobtainium", 5);
        block("allthemodium:unobtainium_ore", 5);
        dim("allthemodium:the_other", 5);

        // ── PPT 6 — Star-chase (alloys / piglich heart / the Star) ───────────────
        item("allthemodium:piglich_heart", 6);
        item("allthemodium:vibranium_allthemodium_alloy_ingot", 6);
        item("allthemodium:unobtainium_vibranium_alloy_ingot", 6);
        item("allthemodium:unobtainium_allthemodium_alloy_ingot", 6);
        item("allthetweaks:atm_star", 6);
        dim("allthemodium:the_beyond", 6);
    }

    public static int forItem(ResourceLocation id)      { return id == null ? 0 : ITEM_TIER.getOrDefault(id, 0); }
    public static int forBlock(ResourceLocation id)     { return id == null ? 0 : BLOCK_TIER.getOrDefault(id, 0); }
    public static int forDimension(ResourceLocation id) { return id == null ? 0 : DIMENSION_TIER.getOrDefault(id, 0); }

    private ProgressionMilestones() {}
}
