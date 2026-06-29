package com.allthegoodies.network;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.client.CacheOpenToast;
import com.allthegoodies.client.WildCacheRoulette;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AllTheGoodies.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ATGNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1");

        reg.playToClient(
                WildCacheRevealPayload.TYPE,
                WildCacheRevealPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        WildCacheRoulette.start(payload.pos(), payload.rarityOrdinal())));

        reg.playToClient(
                CacheOpenToastPayload.TYPE,
                CacheOpenToastPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        CacheOpenToast.show(payload.rarityOrdinal(), payload.itemCount())));
    }

    private ATGNetwork() {}
}
