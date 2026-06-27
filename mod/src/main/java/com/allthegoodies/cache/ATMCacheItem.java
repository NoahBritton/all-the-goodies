package com.allthegoodies.cache;

import com.allthegoodies.progression.ProgressionTier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The ATM Cache. Right-click to open: read the player's progression tier, roll a rarity, then
 * draw the reward from the data-driven loot table {@code allthegoodies:caches/ppt<N>/<rarity>}
 * via {@link CacheOpening} (shared with the debug command).
 */
public class ATMCacheItem extends Item {

    public ATMCacheItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            int ppt = ProgressionTier.get(player);
            RarityTier rarity = CacheRoller.rollRarity(serverLevel.getRandom());
            CacheOpening.open(serverPlayer, ppt, rarity);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
