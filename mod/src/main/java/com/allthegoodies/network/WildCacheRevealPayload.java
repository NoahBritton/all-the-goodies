package com.allthegoodies.network;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WildCacheRevealPayload(BlockPos pos, int rarityOrdinal) implements CustomPacketPayload {

    /** Ticks the rising-spiral animation climbs before it bursts. Server drops the cache(s) on this tick. */
    public static final int REVEAL_TICKS = 40;
    /** How far (blocks) the reveal broadcasts and how far a player must be to receive a drop. */
    public static final double REVEAL_RADIUS = 24.0;
    /** Height (blocks above the broken block) the spiral peaks at and the caches drop from. */
    public static final double APEX_HEIGHT = 3.0;

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
