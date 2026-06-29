package com.allthegoodies.client;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.network.WildCacheRevealPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
 * Client-side "particle roulette" played when a Wild Cache is broken: a column of dust spirals
 * upward, strobing through every rarity color (the roll), decelerates, and locks onto the winning
 * color near the top — then bursts. The server drops the actual cache(s) from the apex on the same
 * tick the burst fires (see {@link WildCacheRevealPayload#REVEAL_TICKS}).
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID, value = Dist.CLIENT)
public final class WildCacheRoulette {

    /** Per-rarity dust colors (RGB 0–1), indexed by RarityTier ordinal. */
    private static final Vector3f[] COLORS = {
            new Vector3f(0.65f, 0.65f, 0.65f), // COMMON     gray
            new Vector3f(0.10f, 0.85f, 0.85f), // UNCOMMON   aqua
            new Vector3f(0.25f, 0.35f, 1.00f), // RARE       blue
            new Vector3f(0.65f, 0.10f, 0.90f), // EPIC       purple
            new Vector3f(1.00f, 0.78f, 0.05f), // LEGENDARY  gold
            new Vector3f(1.00f, 0.12f, 0.12f), // MYTHIC     red
    };

    private static final int RISE_TICKS = WildCacheRevealPayload.REVEAL_TICKS; // climb duration
    private static final int DONE_TICK   = RISE_TICKS + 6;                     // linger after burst
    private static final int STRANDS      = 3;     // intertwined spiral arms
    private static final double TURNS      = 3.0;  // full revolutions over the climb
    private static final double APEX       = WildCacheRevealPayload.APEX_HEIGHT;
    private static final double BASE_Y      = 0.3; // start height above the block

    private static final List<Reveal> ACTIVE = new ArrayList<>();

    /** Called from the network handler on the client thread. */
    public static void start(BlockPos pos, int rarityOrdinal) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        ACTIVE.add(new Reveal(pos, rarityOrdinal, (int) level.getGameTime()));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.isPaused()) return;

        int now = (int) level.getGameTime();
        Iterator<Reveal> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Reveal r = it.next();
            int elapsed = now - r.startTick;

            if (elapsed >= DONE_TICK) { it.remove(); continue; }

            if (elapsed < RISE_TICKS) {
                spawnSpiral(level, r, elapsed);
            } else if (elapsed == RISE_TICKS) {
                spawnBurst(level, r);
            }
        }
    }

    private static void spawnSpiral(Level level, Reveal r, int elapsed) {
        double t = elapsed / (double) RISE_TICKS;          // 0..1 climb progress
        double cx = r.pos.getX() + 0.5, cz = r.pos.getZ() + 0.5;
        double y  = r.pos.getY() + Mth.lerp(t, BASE_Y, APEX);
        double radius = 0.55 * (1.0 - 0.35 * t);           // taper inward as it rises

        Vector3f color = rollColor(elapsed, t, r.rarityOrdinal);
        DustParticleOptions dust = new DustParticleOptions(color, 1.3f);

        for (int s = 0; s < STRANDS; s++) {
            double angle = t * TURNS * Math.PI * 2.0 + (s * Math.PI * 2.0 / STRANDS);
            double px = cx + Math.cos(angle) * radius;
            double pz = cz + Math.sin(angle) * radius;
            level.addParticle(dust, px, y, pz, 0, 0.01, 0);
        }

        // Pling rising in pitch as it climbs; a couple per second.
        if (elapsed % 3 == 0) {
            float pitch = 0.7f + (float) t * 1.1f;
            level.playLocalSound(cx, y, cz, SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.BLOCKS, 0.45f, pitch, false);
        }
    }

    /** Strobe through all rarities fast, slow the strobe, then lock onto the winner near the top. */
    private static Vector3f rollColor(int elapsed, double t, int winner) {
        if (t >= 0.78) return COLORS[winner];               // locked
        int divisor = t < 0.5 ? 1 : 2;                      // decelerate the roll
        return COLORS[(elapsed / divisor) % COLORS.length];
    }

    private static void spawnBurst(Level level, Reveal r) {
        double cx = r.pos.getX() + 0.5, cz = r.pos.getZ() + 0.5;
        double y  = r.pos.getY() + APEX;
        DustParticleOptions dust = new DustParticleOptions(COLORS[r.rarityOrdinal], 2.2f);

        for (int i = 0; i < 28; i++) {
            double vx = (level.getRandom().nextDouble() - 0.5) * 0.35;
            double vy = (level.getRandom().nextDouble() - 0.5) * 0.35;
            double vz = (level.getRandom().nextDouble() - 0.5) * 0.35;
            level.addParticle(dust, cx, y, cz, vx, vy, vz);
        }
        level.addParticle(ParticleTypes.FLASH, cx, y, cz, 0, 0, 0);
        level.addParticle(ParticleTypes.POOF, cx, y, cz, 0, 0.05, 0);

        level.playLocalSound(cx, y, cz, SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.BLOCKS, 1.0f, 2.0f, false);
        level.playLocalSound(cx, y, cz, SoundEvents.NOTE_BLOCK_CHIME.value(),
                SoundSource.BLOCKS, 0.8f, 1.4f, false);
    }

    private record Reveal(BlockPos pos, int rarityOrdinal, int startTick) {}

    private WildCacheRoulette() {}
}
