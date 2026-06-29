package com.allthegoodies.jade;

import com.allthegoodies.block.WildCacheBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class ATGJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration reg) {}

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerBlockComponent(WildCacheTooltipProvider.INSTANCE, WildCacheBlock.class);
    }
}
