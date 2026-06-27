package com.allthegoodies.registry;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.cache.ATMCacheItem;
import com.allthegoodies.cache.ColoredCacheItem;
import com.allthegoodies.cache.RarityTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item registrations for All The Goodies. */
public final class ATGItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AllTheGoodies.MODID);

    /** The plain ATM Cache (rolls rarity on open — kept for legacy/debug use). */
    public static final DeferredItem<Item> ATM_CACHE = ITEMS.register(
            "atm_cache",
            () -> new ATMCacheItem(new Item.Properties().stacksTo(16)));

    /** Colored caches — fixed rarity, opened right-click (SPEC §3.4). */
    public static final DeferredItem<Item> CACHE_CONSUMER   = ITEMS.register("cache_consumer",   () -> new ColoredCacheItem(RarityTier.COMMON,    new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CACHE_INDUSTRIAL = ITEMS.register("cache_industrial", () -> new ColoredCacheItem(RarityTier.UNCOMMON,  new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CACHE_MILSPEC    = ITEMS.register("cache_milspec",    () -> new ColoredCacheItem(RarityTier.RARE,      new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CACHE_RESTRICTED = ITEMS.register("cache_restricted", () -> new ColoredCacheItem(RarityTier.EPIC,      new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CACHE_CLASSIFIED = ITEMS.register("cache_classified", () -> new ColoredCacheItem(RarityTier.LEGENDARY, new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CACHE_COVERT     = ITEMS.register("cache_covert",     () -> new ColoredCacheItem(RarityTier.MYTHIC,    new Item.Properties().stacksTo(64)));

    /** Returns the colored cache item for a given rarity. */
    public static DeferredItem<Item> coloredCacheFor(RarityTier rarity) {
        return switch (rarity) {
            case COMMON    -> CACHE_CONSUMER;
            case UNCOMMON  -> CACHE_INDUSTRIAL;
            case RARE      -> CACHE_MILSPEC;
            case EPIC      -> CACHE_RESTRICTED;
            case LEGENDARY -> CACHE_CLASSIFIED;
            case MYTHIC    -> CACHE_COVERT;
        };
    }

    /** Block item for the wild_cache world block. */
    public static final DeferredItem<BlockItem> WILD_CACHE = ITEMS.registerSimpleBlockItem(ATGBlocks.WILD_CACHE);

    private ATGItems() {}
}
