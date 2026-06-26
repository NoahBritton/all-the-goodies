package com.allthegoodies.cache;

import net.minecraft.util.RandomSource;

/**
 * The first of the two rolls (SPEC.md §3.2): pick a rarity by weight. The second roll — picking
 * a concrete reward from the {@code {rarity × PPT}} pool — is a data-driven loot table resolved
 * in {@link ATMCacheItem}.
 */
public final class CacheRoller {

    public static RarityTier rollRarity(RandomSource random) {
        int roll = random.nextInt(RarityTier.TOTAL_WEIGHT);
        int cumulative = 0;
        for (RarityTier tier : RarityTier.values()) {
            cumulative += tier.weight;
            if (roll < cumulative) {
                return tier;
            }
        }
        return RarityTier.COMMON; // unreachable while weights sum to TOTAL_WEIGHT
    }

    private CacheRoller() {}
}
