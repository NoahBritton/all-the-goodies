package com.allthegoodies.cache;

import com.allthegoodies.config.ATGConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rolling-window cap on activity drops: a player can receive at most
 * MAX_DROPS cache drops in any WINDOW_MS millisecond period. Drops that
 * would exceed the cap are silently suppressed.
 *
 * Milestone drops (tier-up) are NOT subject to this limit — only
 * organic activity drops go through here.
 */
public final class AntiFarmLimiter {

    // Values read from config at call time so live changes take effect.

    private static final Map<UUID, Deque<Long>> timestamps = new HashMap<>();

    /** Returns true if the player is allowed a drop right now, and records it. */
    public static boolean tryGrant(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Deque<Long> window = timestamps.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());

        long windowMs = ATGConfig.ANTI_FARM_WINDOW_SECONDS.get() * 1000L;
        int maxDrops  = ATGConfig.ANTI_FARM_MAX_DROPS.get();

        while (!window.isEmpty() && now - window.peekFirst() > windowMs) {
            window.pollFirst();
        }

        if (window.size() >= maxDrops) return false;

        window.addLast(now);
        return true;
    }

    private AntiFarmLimiter() {}
}
