package com.allthegoodies;

import com.allthegoodies.progression.ATGAttachments;
import com.allthegoodies.registry.ATGBlocks;
import com.allthegoodies.registry.ATGCreativeTab;
import com.allthegoodies.registry.ATGItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * All The Goodies — a progression-aware loot-crate ("ATM Cache") and quality-of-life add-on
 * for All The Mods 10. See SPEC.md at the repository root for the design.
 */
@Mod(AllTheGoodies.MODID)
public final class AllTheGoodies {
    public static final String MODID = "allthegoodies";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AllTheGoodies(IEventBus modEventBus) {
        // Registries
        ATGAttachments.ATTACHMENTS.register(modEventBus);
        ATGBlocks.BLOCKS.register(modEventBus);
        ATGItems.ITEMS.register(modEventBus);
        ATGCreativeTab.TABS.register(modEventBus);

        LOGGER.info("[All The Goodies] initialising — ATM Cache + progression engine loaded");
    }
}
