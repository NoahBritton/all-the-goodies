package com.allthegoodies.block;

import com.allthegoodies.cache.CacheRoller;
import com.allthegoodies.cache.RarityTier;
import com.allthegoodies.registry.ATGItems;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class WildCacheBlock extends Block {

    public WildCacheBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity be, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (!level.isClientSide) {
            RarityTier rarity = CacheRoller.rollRarity(level.getRandom());
            ItemStack drop = ATGItems.coloredCacheFor(rarity).get().getDefaultInstance();
            Block.popResource(level, pos, drop);
        }
    }
}
