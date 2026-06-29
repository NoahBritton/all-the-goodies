package com.allthegoodies.network;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CacheOpenToastPayload(int rarityOrdinal, int itemCount) implements CustomPacketPayload {

    public static final Type<CacheOpenToastPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "cache_open_toast"));

    public static final StreamCodec<FriendlyByteBuf, CacheOpenToastPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, CacheOpenToastPayload::rarityOrdinal,
                    ByteBufCodecs.INT, CacheOpenToastPayload::itemCount,
                    CacheOpenToastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
