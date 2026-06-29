package com.allthegoodies.network;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Sent to the opener so the client can toast the actual loot they received. */
public record CacheOpenToastPayload(int rarityOrdinal, List<ItemStack> items) implements CustomPacketPayload {

    /** Cap on how many item rows we ship/show so the toast and packet stay small. */
    public static final int MAX_ITEMS = 8;

    public static final Type<CacheOpenToastPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "cache_open_toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CacheOpenToastPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, CacheOpenToastPayload::rarityOrdinal,
                    ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), CacheOpenToastPayload::items,
                    CacheOpenToastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
