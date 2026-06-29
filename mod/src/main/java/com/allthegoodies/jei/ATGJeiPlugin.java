package com.allthegoodies.jei;

import com.allthegoodies.AllTheGoodies;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;

/**
 * Registers All The Goodies with JEI. Cache items appear automatically via DeferredRegister —
 * this plugin hook exists so we can add recipe categories or info pages later.
 */
@JeiPlugin
public final class ATGJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "jei_plugin");
    }
}
