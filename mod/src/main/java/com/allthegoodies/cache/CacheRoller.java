package com.allthegoodies.cache;

import com.allthegoodies.config.ATGConfig;
import net.minecraft.util.RandomSource;

/**
 * The first of the two rolls (SPEC.md §3.2): pick a rarity by weight.
 * Weights are read from ATGConfig so server admins can tune odds without a rebuild.
 */
public final class CacheRoller {

    public static RarityTier rollRarity(RandomSource random) {
        int[] weights = {
            ATGConfig.WEIGHT_COMMON.get(),
            ATGConfig.WEIGHT_UNCOMMON.get(),
            ATGConfig.WEIGHT_RARE.get(),
            ATGConfig.WEIGHT_EPIC.get(),
            ATGConfig.WEIGHT_LEGENDARY.get(),
            ATGConfig.WEIGHT_MYTHIC.get(),
        };
        int total = 0;
        for (int w : weights) total += w;
        if (total <= 0) return RarityTier.COMMON;

        int roll = random.nextInt(total);
        int cumulative = 0;
        RarityTier[] tiers = RarityTier.values();
        for (int i = 0; i < tiers.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return tiers[i];
        }
        return RarityTier.COMMON;
    }

    private CacheRoller() {}
}
