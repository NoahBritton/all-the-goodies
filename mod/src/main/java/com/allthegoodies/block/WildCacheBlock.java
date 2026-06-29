package com.allthegoodies.block;

import com.allthegoodies.cache.CacheHomingManager;
import com.allthegoodies.cache.CacheRoller;
import com.allthegoodies.cache.RarityTier;
import com.allthegoodies.network.WildCacheRevealPayload;
import com.allthegoodies.registry.ATGItems;
import com.allthegoodies.util.ATGScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WildCacheBlock extends Block {

    public WildCacheBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Break to reveal a random colored cache")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Everyone nearby gets one — watch the spiral")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity be, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Roll the rarity now (so the animation can show it), but DROP at the end of the animation.
        RarityTier rarity = CacheRoller.rollRarity(serverLevel.getRandom());

        // Broadcast the rising-spiral reveal to every player in range (they all see it).
        PacketDistributor.sendToPlayersNear(
                serverLevel, null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                WildCacheRevealPayload.REVEAL_RADIUS,
                new WildCacheRevealPayload(pos, rarity.ordinal()));

        // When the spiral peaks, spawn the floating cache(s); they hover, then fly to each player.
        // Small lead past REVEAL_TICKS so the cache emerges just AFTER the client-side burst crests
        // (the client animation starts a tick or two late due to packet travel).
        ATGScheduler.schedule(serverLevel.getServer(), WildCacheRevealPayload.REVEAL_TICKS + SPAWN_AFTER_BURST,
                () -> spawnFloating(serverLevel, pos, rarity));
    }

    /** Ticks after the burst before the cache materializes (covers client-start latency). */
    private static final int SPAWN_AFTER_BURST = 4;
    /** How long (ticks) the cache hovers at the apex before it flies to the player. */
    private static final int FLOAT_TICKS = 16;

    private static void spawnFloating(ServerLevel level, BlockPos pos, RarityTier rarity) {
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double apexY = pos.getY() + WildCacheRevealPayload.APEX_HEIGHT;
        double centerY = pos.getY() + 0.5;
        double r = WildCacheRevealPayload.REVEAL_RADIUS;

        // One cache for every player who could see the reveal (min 1 — the breaker).
        List<ServerPlayer> nearby = level.getPlayers(p -> p.distanceToSqr(cx, centerY, cz) <= r * r);
        int count = Math.max(1, nearby.size());
        MinecraftServer server = level.getServer();

        for (int i = 0; i < count; i++) {
            ServerPlayer target = nearby.isEmpty() ? null : nearby.get(i % nearby.size());
            ItemStack stack = ATGItems.coloredCacheFor(rarity).get().getDefaultInstance();

            ItemEntity entity = new ItemEntity(level, cx, apexY, cz, stack);
            // Single cache sits dead-center under the burst; multiples spread into a ring.
            double ring = count > 1 ? 0.45 : 0.0;
            double angle = (Math.PI * 2.0 * i) / count;
            entity.setPos(cx + Math.cos(angle) * ring, apexY, cz + Math.sin(angle) * ring);
            entity.setDeltaMovement(0, 0.02, 0);   // gentle bob
            entity.setNoGravity(true);
            entity.setUnlimitedLifetime();
            entity.setPickUpDelay(32767);          // no early pickup while it floats
            level.addFreshEntity(entity);

            // After the hover, hand it to the homing manager which steers it into the player.
            final ItemEntity fe = entity;
            final ServerPlayer ft = target;
            ATGScheduler.schedule(server, FLOAT_TICKS, () -> CacheHomingManager.add(fe, ft));
        }
    }
}
