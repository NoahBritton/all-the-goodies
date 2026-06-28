package com.allthegoodies.cache;

import com.allthegoodies.progression.ProgressionTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A colored ATM Cache with a fixed rarity (SPEC.md §3.4). Right-clicking opens it using the
 * player's current PPT and this item's rarity — no second roll needed.
 */
public final class ColoredCacheItem extends Item {

    private final RarityTier rarity;

    public ColoredCacheItem(RarityTier rarity, Properties properties) {
        super(properties);
        this.rarity = rarity;
    }

    public RarityTier getRarity() {
        return rarity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal(rarity.description).withStyle(rarity.color));
        lines.add(Component.literal("Rewards scale with your Progression Tier (PPT)")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            int ppt = ProgressionTier.get(player);
            CacheOpening.open(serverPlayer, ppt, rarity);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
