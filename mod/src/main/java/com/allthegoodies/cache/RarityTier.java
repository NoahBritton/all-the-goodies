package com.allthegoodies.cache;

import net.minecraft.ChatFormatting;

/**
 * CS:GO-style rarity tiers for an {@code ATM Cache} opening. Weights are out of 10000 and sum
 * to exactly 10000. Mirrors SPEC.md §3.2 (final values are intended to move to config).
 */
public enum RarityTier {
    COMMON("Common",    7900, ChatFormatting.GRAY,        "Food, torches, basic iron gear"),
    UNCOMMON("Uncommon", 1500, ChatFormatting.AQUA,       "Iron ingots, tools, early materials"),
    RARE("Rare",         470, ChatFormatting.BLUE,        "Enchanted tools, diamonds"),
    EPIC("Epic",          90, ChatFormatting.DARK_PURPLE, "Enchanted armor sets, rare materials"),
    LEGENDARY("Legendary", 30, ChatFormatting.GOLD,       "Top-tier tools for your progress stage"),
    MYTHIC("Mythic",      10, ChatFormatting.RED,         "The jackpot — best gear at your tier");

    public static final int TOTAL_WEIGHT = 10000;

    public final String flavor;
    public final int weight;
    public final ChatFormatting color;
    public final String description;

    RarityTier(String flavor, int weight, ChatFormatting color, String description) {
        this.flavor = flavor;
        this.weight = weight;
        this.color = color;
        this.description = description;
    }

    /** Lower-case id used to build the loot-table path (e.g. {@code caches/ppt3/mythic}). */
    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
