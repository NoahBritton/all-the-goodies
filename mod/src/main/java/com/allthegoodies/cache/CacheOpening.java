package com.allthegoodies.cache;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.network.CacheOpenToastPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

/**
 * Shared "open one Cache" logic: resolve the data-driven {@code caches/ppt<N>/<rarity>} loot
 * table, grant its rolls to the player, and send a quick result toast. Used by both
 * {@link ATMCacheItem} (normal right-click) and the debug command (forced opens).
 *
 * <p>A missing pool resolves to the empty loot table (no items), so the 7×6 matrix can be
 * filled in incrementally without errors.</p>
 */
public final class CacheOpening {

    /** Open one Cache at {@code ppt}×{@code rarity} for the player; returns items granted. */
    public static int open(ServerPlayer player, int ppt, RarityTier rarity) {
        ServerLevel level = player.serverLevel();
        ResourceLocation tableId = ResourceLocation.fromNamespaceAndPath(
                AllTheGoodies.MODID, String.format(Locale.ROOT, "caches/ppt%d/%s", ppt, rarity.id()));
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, tableId);
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);

        List<ItemStack> rolled = table.getRandomItems(params);

        // Snapshot what was won (capped) for the toast before inserting consumes/mutates the stacks.
        List<ItemStack> forToast = rolled.stream()
                .limit(CacheOpenToastPayload.MAX_ITEMS)
                .map(ItemStack::copy)
                .toList();

        for (ItemStack reward : rolled) {
            player.getInventory().placeItemBackInInventory(reward);
        }
        PacketDistributor.sendToPlayer(player, new CacheOpenToastPayload(rarity.ordinal(), forToast));
        return rolled.size();
    }

    private CacheOpening() {}
}
