package com.allthegoodies.jade;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.cache.RarityTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class WildCacheTooltipProvider implements IBlockComponentProvider {

    public static final WildCacheTooltipProvider INSTANCE = new WildCacheTooltipProvider();
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "wild_cache_tooltip");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.add(Component.literal("Break to receive a random cache").withStyle(ChatFormatting.GRAY));
        for (RarityTier tier : RarityTier.values()) {
            tooltip.add(Component.literal("  " + tier.flavor).withStyle(tier.color));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
