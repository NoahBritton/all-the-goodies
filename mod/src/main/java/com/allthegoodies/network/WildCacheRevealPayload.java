package com.allthegoodies.network;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WildCacheRevealPayload(BlockPos pos, int rarityOrdinal) implements CustomPacketPayload {

    public static final Type<WildCacheRevealPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "wild_cache_reveal"));

    public static final StreamCodec<FriendlyByteBuf, WildCacheRevealPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, WildCacheRevealPayload::pos,
                    ByteBufCodecs.INT,     WildCacheRevealPayload::rarityOrdinal,
                    WildCacheRevealPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
