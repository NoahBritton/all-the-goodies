package com.allthegoodies.cache;

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

    private static final int MAX_DROPS = 5;
    private static final long WINDOW_MS = 5 * 60 * 1000L; // 5 minutes

    private static final Map<UUID, Deque<Long>> timestamps = new HashMap<>();

    /** Returns true if the player is allowed a drop right now, and records it. */
    public static boolean tryGrant(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Deque<Long> window = timestamps.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());

        // evict timestamps outside the rolling window
        while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
            window.pollFirst();
        }

        if (window.size() >= MAX_DROPS) return false;

        window.addLast(now);
        return true;
    }

    private AntiFarmLimiter() {}
}
