package com.allthegoodies.progression;

import net.minecraft.world.entity.player.Player;

/**
 * The progression-scaling engine. A player's tier (0..6) decides which reward pool an
 * {@code ATM Cache} draws from, so jackpots are always useful "right now."
 *
 * <p>Tier only ever increases ("latches up"). Milestone detection (first Allthemodium ingot,
 * dimension entry, FTB quest chapters, ...) lives in event handlers to be added next; those
 * handlers call {@link #raiseTo(Player, int)}.</p>
 */
public final class ProgressionTier {
    public static final int MIN = 0;
    public static final int MAX = 6;

    /** Human-readable names, indexed by tier. Mirrors SPEC.md §3.1. */
    public static final String[] NAMES = {
            "Stranded",     // 0
            "Established",  // 1
            "Industrial",   // 2
            "Allthemodium", // 3
            "Vibranium",    // 4
            "Unobtainium",  // 5
            "Star-chase"    // 6
    };

    public static int get(Player player) {
        return clamp(player.getData(ATGAttachments.PROGRESSION_TIER.get()));
    }

    /**
     * Raise the player's tier to at least {@code tier} (latching upward).
     *
     * @return {@code true} if the tier actually increased.
     */
    public static boolean raiseTo(Player player, int tier) {
        int target = clamp(tier);
        if (target > get(player)) {
            player.setData(ATGAttachments.PROGRESSION_TIER.get(), target);
            return true;
        }
        return false;
    }

    public static String name(int tier) {
        return NAMES[clamp(tier)];
    }

    private static int clamp(int tier) {
        return Math.max(MIN, Math.min(MAX, tier));
    }

    private ProgressionTier() {}
}
