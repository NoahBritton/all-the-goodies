package com.allthegoodies.client;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.cache.RarityTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side particle + sound roulette played when a Wild Cache is broken.
 * Cycles through rarity colors fast, slows to land on the winner.
 *
 * Animation duration: ~1.4 seconds (28 ticks).
 *   ticks 0–11  : fast spin, all 6 rarities twice (1 tick each)
 *   ticks 12–23 : slow spin, all 6 rarities again (2 ticks each)
 *   ticks 24–28 : landing — big burst in the winning rarity color
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID, value = Dist.CLIENT)
public final class WildCacheRoulette {

    // Per-rarity particle colors (RGB 0-1).
    private static final Vector3f[] COLORS = {
            new Vector3f(0.6f, 0.6f, 0.6f),   // COMMON      gray
            new Vector3f(0.0f, 0.8f, 0.8f),   // UNCOMMON    aqua
            new Vector3f(0.2f, 0.3f, 1.0f),   // RARE        blue
            new Vector3f(0.6f, 0.0f, 0.8f),   // EPIC        purple
            new Vector3f(1.0f, 0.75f, 0.0f),  // LEGENDARY   gold
            new Vector3f(1.0f, 0.1f, 0.1f),   // MYTHIC      red
    };

    // Tick offsets for the spin sequence (rarity index cycles with modulo).
    // fast phase: 0-11 (every tick), slow phase: 12-23 (every 2 ticks), land: 24+
    private static final int FAST_END  = 12;
    private static final int SLOW_END  = 24;
    private static final int BURST_TICK = 24;
    private static final int DONE_TICK  = 29;

    private static final List<PendingReveal> PENDING = new ArrayList<>();

    /** Called from the network packet handler on the main client thread. */
    public static void start(BlockPos pos, int rarityOrdinal) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        PENDING.add(new PendingReveal(pos, rarityOrdinal, (int) level.getGameTime()));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.isPaused()) return;

        int now = (int) level.getGameTime();
        Iterator<PendingReveal> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingReveal r = it.next();
            int elapsed = now - r.startTick;

            if (elapsed >= DONE_TICK) {
                it.remove();
                continue;
            }

            int spinIndex = spinIndexAt(elapsed, r.rarityOrdinal);
            if (spinIndex >= 0) {
                spawnSpin(level, r.pos, spinIndex, elapsed);
            }

            if (elapsed == BURST_TICK) {
                spawnLanding(level, r.pos, r.rarityOrdinal);
            }
        }
    }

    /** Returns the rarity index to display at this tick, or -1 if this tick is silent. */
    private static int spinIndexAt(int elapsed, int winner) {
        int n = RarityTier.values().length; // 6
        if (elapsed < FAST_END) {
            // one rarity per tick, cycling from index 0
            return elapsed % n;
        }
        if (elapsed < SLOW_END) {
            // two ticks per rarity
            int slowElapsed = elapsed - FAST_END;
            return slowElapsed % 2 == 0 ? (slowElapsed / 2) % n : -1;
        }
        return -1;
    }

    private static void spawnSpin(Level level, BlockPos pos, int rarityIndex, int elapsed) {
        Vector3f c = COLORS[rarityIndex];
        DustParticleOptions dust = new DustParticleOptions(c, 1.2f);
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;

        // 4 dust particles in a small cloud
        for (int i = 0; i < 4; i++) {
            double ox = (level.getRandom().nextDouble() - 0.5) * 0.6;
            double oy = (level.getRandom().nextDouble() - 0.5) * 0.6;
            double oz = (level.getRandom().nextDouble() - 0.5) * 0.6;
            level.addParticle(dust, cx + ox, cy + oy, cz + oz, 0, 0.05, 0);
        }

        // rising pling pitch across the spin
        float pitch = 0.6f + (elapsed / (float) SLOW_END) * 1.2f;
        level.playLocalSound(cx, cy, cz, SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.BLOCKS, 0.4f, pitch, false);
    }

    private static void spawnLanding(Level level, BlockPos pos, int rarityOrdinal) {
        Vector3f c = COLORS[rarityOrdinal];
        DustParticleOptions dust = new DustParticleOptions(c, 2.0f);
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;

        // large burst
        for (int i = 0; i < 20; i++) {
            double vx = (level.getRandom().nextDouble() - 0.5) * 0.3;
            double vy = level.getRandom().nextDouble() * 0.4;
            double vz = (level.getRandom().nextDouble() - 0.5) * 0.3;
            level.addParticle(dust, cx, cy, cz, vx, vy, vz);
        }

        // poof
        level.addParticle(ParticleTypes.POOF, cx, cy, cz, 0, 0.1, 0);

        // high-pitched triumphant pling
        level.playLocalSound(cx, cy, cz, SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.BLOCKS, 1.0f, 2.0f, false);
    }

    private record PendingReveal(BlockPos pos, int rarityOrdinal, int startTick) {}

    private WildCacheRoulette() {}
}
