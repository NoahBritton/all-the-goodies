package com.allthegoodies.progression;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.registry.ATGItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Milestone detectors: latch a player's PPT upward the first time they hit a tier marker
 * (verified ids in {@link ProgressionMilestones}). Markers are caught across multiple signals
 * — item pickup, craft, smelt, ore break, and dimension entry — so a tier latches no matter how
 * the player gets there. On a real tier-up, we announce it and grant one guaranteed "level-up"
 * Cache (SPEC §3.3 milestone drop).
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID)
public final class ProgressionEvents {

    private static final ResourceLocation ATM_STAR_ID = ResourceLocation.parse("allthetweaks:atm_star");
    public static final int STARS_NEEDED = 25;

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        ItemStack stack = event.getOriginalStack();
        if (stack == null || stack.isEmpty()) return;
        ResourceLocation id = itemId(stack);
        tryRaise(event.getPlayer(), ProgressionMilestones.forItem(id));
        if (ATM_STAR_ID.equals(id)) trackStar(event.getPlayer(), stack.getCount());
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ResourceLocation id = itemId(event.getCrafting());
        tryRaise(event.getEntity(), ProgressionMilestones.forItem(id));
        if (ATM_STAR_ID.equals(id)) trackStar(event.getEntity(), event.getCrafting().getCount());
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        tryRaise(event.getEntity(), ProgressionMilestones.forItem(itemId(event.getSmelting())));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        tryRaise(player, ProgressionMilestones.forBlock(blockId));
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        tryRaise(event.getEntity(), ProgressionMilestones.forDimension(event.getTo().location()));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void trackStar(Player player, int count) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        int prev  = player.getData(ATGAttachments.ATM_STAR_COUNT.get());
        int total = Math.min(prev + count, STARS_NEEDED);
        if (total <= prev) return;
        player.setData(ATGAttachments.ATM_STAR_COUNT.get(), total);
        String suffix = total >= STARS_NEEDED ? " — Pack complete!" : "";
        serverPlayer.sendSystemMessage(Component.literal(
                "★ ATM Star " + total + "/" + STARS_NEEDED + suffix)
                .withStyle(total >= STARS_NEEDED ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static void tryRaise(Player player, int tier) {
        if (tier <= 0) return;
        if (ProgressionTier.raiseTo(player, tier) && player instanceof ServerPlayer serverPlayer) {
            onTierUp(serverPlayer, tier);
        }
    }

    /** Tier-up moment: gold announcement + one guaranteed milestone Cache. */
    private static void onTierUp(ServerPlayer player, int tier) {
        player.sendSystemMessage(Component.literal(
                        "★ Progression Tier " + tier + " — " + ProgressionTier.name(tier) + "!  (a Cache for the climb)")
                .withStyle(ChatFormatting.GOLD));
        player.getInventory().placeItemBackInInventory(new ItemStack(ATGItems.ATM_CACHE.get()));
    }

    private ProgressionEvents() {}
}
