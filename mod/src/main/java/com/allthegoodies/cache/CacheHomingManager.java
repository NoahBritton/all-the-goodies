package com.allthegoodies.cache;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Steers floating Wild Cache items into their target player each server tick, then delivers the
 * cache straight to the player's inventory (with a pickup ding + sparkle). Guarantees delivery —
 * if the item can't reach the player within {@link #MAX_HOMING_TICKS}, it's handed over anyway.
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID)
public final class CacheHomingManager {

    private static final int MAX_HOMING_TICKS = 50;
    private static final double DELIVER_DIST = 1.3;

    private record Homing(ItemEntity entity, ServerPlayer target, int startTick) {}

    private static final List<Homing> ACTIVE = new ArrayList<>();

    /** Begin homing this floating cache toward {@code target} (may be null → it just drops normally). */
    public static void add(ItemEntity entity, ServerPlayer target) {
        if (entity == null || !entity.isAlive()) return;
        if (target == null || !target.isAlive()) {
            // No one to fly to — let it fall and be grabbed the normal way.
            entity.setNoGravity(false);
            entity.setPickUpDelay(0);
            return;
        }
        int now = entity.level().getServer().getTickCount();
        ACTIVE.add(new Homing(entity, target, now));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();
        int now = server.getTickCount();

        Iterator<Homing> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Homing h = it.next();
            ItemEntity e = h.entity();

            if (e == null || !e.isAlive()) { it.remove(); continue; }

            ServerPlayer target = h.target();
            if (target == null || !target.isAlive() || target.isRemoved()) {
                e.setNoGravity(false);
                e.setPickUpDelay(0);
                it.remove();
                continue;
            }

            Vec3 toPlayer = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(e.position());
            double dist = toPlayer.length();

            if (dist <= DELIVER_DIST || now - h.startTick() >= MAX_HOMING_TICKS) {
                deliver(e, target);
                it.remove();
                continue;
            }

            // Accelerate toward the player; cap the speed so close hand-offs aren't jittery.
            double speed = Math.min(0.6, 0.18 + dist * 0.16);
            e.setNoGravity(true);
            e.setDeltaMovement(toPlayer.normalize().scale(speed));
            e.hasImpulse = true;
        }
    }

    private static void deliver(ItemEntity entity, ServerPlayer target) {
        ItemStack stack = entity.getItem().copy();
        entity.discard();

        if (!target.getInventory().add(stack)) {
            target.drop(stack, false);   // inventory full → drop at the player's feet
        }

        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.6f, 1.7f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + 1.0, target.getZ(), 7, 0.3, 0.4, 0.3, 0.0);
        }
    }

    private CacheHomingManager() {}
}
