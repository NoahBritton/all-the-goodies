package com.allthegoodies.block;

import com.allthegoodies.cache.CacheRoller;
import com.allthegoodies.cache.RarityTier;
import com.allthegoodies.network.WildCacheRevealPayload;
import com.allthegoodies.registry.ATGItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
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
        lines.add(Component.literal("Break to receive a random colored cache")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Found naturally in the world — or spawned for testing")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity be, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            RarityTier rarity = CacheRoller.rollRarity(level.getRandom());
            ItemStack drop = ATGItems.coloredCacheFor(rarity).get().getDefaultInstance();
            Block.popResource(level, pos, drop);
            // Send roulette animation to all players nearby
            PacketDistributor.sendToPlayersNear(
                    serverLevel, null,
                    pos.getX(), pos.getY(), pos.getZ(), 32.0,
                    new WildCacheRevealPayload(pos, rarity.ordinal()));
        }
    }
}
