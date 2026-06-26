package com.allthegoodies.registry;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.cache.ATMCacheItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item registrations for All The Goodies. */
public final class ATGItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AllTheGoodies.MODID);

    /** The core loot crate. Right-click to open; rewards scale to the opener's progression tier. */
    public static final DeferredItem<Item> ATM_CACHE = ITEMS.register(
            "atm_cache",
            () -> new ATMCacheItem(new Item.Properties().stacksTo(16)));

    private ATGItems() {}
}
