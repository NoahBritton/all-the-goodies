package com.allthegoodies.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side config for All The Goodies (written to world/serverconfig/).
 * All drop-rate and balance values live here so a server admin can tune
 * the experience without touching code or rebuilding the mod.
 */
public final class ATGConfig {

    public static final ModConfigSpec SPEC;

    // ── master toggle ─────────────────────────────────────────────────────────
    public static final ModConfigSpec.BooleanValue ENABLED;

    // ── activity drop chances ─────────────────────────────────────────────────
    /** 1-in-N chance to drop a cache on hostile mob kill. */
    public static final ModConfigSpec.IntValue MOB_KILL_CHANCE;
    /** 1-in-N chance to drop a cache on ore block break. */
    public static final ModConfigSpec.IntValue ORE_BREAK_CHANCE;

    // ── anti-farm ─────────────────────────────────────────────────────────────
    /** Rolling window size in seconds. */
    public static final ModConfigSpec.IntValue ANTI_FARM_WINDOW_SECONDS;
    /** Max activity drops allowed in the window. */
    public static final ModConfigSpec.IntValue ANTI_FARM_MAX_DROPS;

    // ── rarity weights (sum should equal 10000 for clean % display) ───────────
    public static final ModConfigSpec.IntValue WEIGHT_COMMON;
    public static final ModConfigSpec.IntValue WEIGHT_UNCOMMON;
    public static final ModConfigSpec.IntValue WEIGHT_RARE;
    public static final ModConfigSpec.IntValue WEIGHT_EPIC;
    public static final ModConfigSpec.IntValue WEIGHT_LEGENDARY;
    public static final ModConfigSpec.IntValue WEIGHT_MYTHIC;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("All The Goodies — server config").push("general");
        ENABLED = b.comment("Master toggle. Set false to disable all cache drops and openings.")
                .define("enabled", true);
        b.pop();

        b.push("activity_drops");
        MOB_KILL_CHANCE = b.comment("1-in-N chance to drop a cache when killing a hostile mob. Higher = rarer.")
                .defineInRange("mob_kill_chance", 20, 1, 10000);
        ORE_BREAK_CHANCE = b.comment("1-in-N chance to drop a cache when breaking an ore block. Higher = rarer.")
                .defineInRange("ore_break_chance", 50, 1, 10000);
        b.pop();

        b.push("anti_farm");
        ANTI_FARM_WINDOW_SECONDS = b.comment("Rolling window size in seconds for the drop cap.")
                .defineInRange("window_seconds", 300, 10, 3600);
        ANTI_FARM_MAX_DROPS = b.comment("Max activity drops a player can receive within the window.")
                .defineInRange("max_drops", 5, 1, 100);
        b.pop();

        b.push("rarity_weights");
        b.comment("Weights for the rarity roll. Higher weight = more likely.",
                  "Default values sum to 10000 for clean percentage display in tooltips.",
                  "You can use any positive values — they don't need to sum to 10000.");
        WEIGHT_COMMON    = b.defineInRange("common",    7900, 0, 100000);
        WEIGHT_UNCOMMON  = b.defineInRange("uncommon",  1500, 0, 100000);
        WEIGHT_RARE      = b.defineInRange("rare",       470, 0, 100000);
        WEIGHT_EPIC      = b.defineInRange("epic",        90, 0, 100000);
        WEIGHT_LEGENDARY = b.defineInRange("legendary",   30, 0, 100000);
        WEIGHT_MYTHIC    = b.defineInRange("mythic",      10, 0, 100000);
        b.pop();

        SPEC = b.build();
    }

    private ATGConfig() {}
}
