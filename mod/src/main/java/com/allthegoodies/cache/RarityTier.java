package com.allthegoodies.cache;

import net.minecraft.ChatFormatting;

/**
 * CS:GO-style rarity tiers for an {@code ATM Cache} opening. Weights are out of 10000 and sum
 * to exactly 10000. Mirrors SPEC.md §3.2 (final values are intended to move to config).
 */
public enum RarityTier {
    COMMON("Worn", 7900, ChatFormatting.GRAY),
    UNCOMMON("Industrial", 1500, ChatFormatting.AQUA),
    RARE("Mil-Spec", 470, ChatFormatting.BLUE),
    EPIC("Restricted", 90, ChatFormatting.DARK_PURPLE),
    LEGENDARY("Classified", 30, ChatFormatting.GOLD),
    MYTHIC("Covert", 10, ChatFormatting.RED); // "the Knife" — ~0.1%

    public static final int TOTAL_WEIGHT = 10000;

    public final String flavor;
    public final int weight;
    public final ChatFormatting color;

    RarityTier(String flavor, int weight, ChatFormatting color) {
        this.flavor = flavor;
        this.weight = weight;
        this.color = color;
    }

    /** Lower-case id used to build the loot-table path (e.g. {@code caches/ppt3/mythic}). */
    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
