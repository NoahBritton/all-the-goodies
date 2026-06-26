package com.allthegoodies.cache;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.progression.ProgressionTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Locale;

/**
 * The ATM Cache. Right-click to open: read the player's progression tier, roll a rarity, then
 * draw the reward from the data-driven loot table {@code allthegoodies:caches/ppt<N>/<rarity>}.
 *
 * <p>Missing pools resolve to an empty loot table (no items) rather than erroring, so the full
 * 7×6 matrix can be filled in incrementally. See SPEC.md §3.</p>
 */
public class ATMCacheItem extends Item {

    public ATMCacheItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            int ppt = ProgressionTier.get(player);
            RarityTier rarity = CacheRoller.rollRarity(serverLevel.getRandom());

            int granted = grantRewards(serverLevel, serverPlayer, ppt, rarity);
            announce(serverPlayer, ppt, rarity, granted);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Rolls the {ppt × rarity} loot table and places the results in the player's inventory. */
    private int grantRewards(ServerLevel level, ServerPlayer player, int ppt, RarityTier rarity) {
        ResourceLocation tableId = ResourceLocation.fromNamespaceAndPath(
                AllTheGoodies.MODID,
                String.format(Locale.ROOT, "caches/ppt%d/%s", ppt, rarity.id()));
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);

        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableKey);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.ADVANCEMENT_REWARD);

        List<ItemStack> rolled = table.getRandomItems(params);
        for (ItemStack reward : rolled) {
            player.getInventory().placeItemBackInInventory(reward);
        }
        return rolled.size();
    }

    private void announce(ServerPlayer player, int ppt, RarityTier rarity, int granted) {
        Component line = Component.literal("ATM Cache → ")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(rarity.flavor + " (" + rarity.name() + ")").withStyle(rarity.color))
                .append(Component.literal(
                        "  • tier " + ppt + " " + ProgressionTier.name(ppt) + " • " + granted + " item(s)")
                        .withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(line);
    }
}
