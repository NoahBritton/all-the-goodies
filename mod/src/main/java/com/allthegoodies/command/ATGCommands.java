package com.allthegoodies.command;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.cache.CacheOpening;
import com.allthegoodies.cache.RarityTier;
import com.allthegoodies.progression.ATGAttachments;
import com.allthegoodies.progression.ProgressionTier;
import com.allthegoodies.registry.ATGItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Debug/admin command {@code /atg} (op level 2) for fast iteration on the Cache + PPT systems:
 * <ul>
 *   <li>{@code /atg ppt} — show your current PPT</li>
 *   <li>{@code /atg ppt set <0-6>} — set PPT directly (bypasses the latch, for testing low tiers)</li>
 *   <li>{@code /atg cache [count]} — give yourself ATM Cache(s)</li>
 *   <li>{@code /atg open <rarity> [ppt]} — force-open a specific rarity (test the jackpot pools without RNG)</li>
 * </ul>
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID)
public final class ATGCommands {

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        // /atg open <rarity> [ppt] — one literal per rarity
        LiteralArgumentBuilder<CommandSourceStack> open = Commands.literal("open");
        for (RarityTier r : RarityTier.values()) {
            final RarityTier rarity = r;
            open.then(Commands.literal(rarity.id())
                    .executes(ctx -> forceOpen(ctx.getSource(), rarity, -1))
                    .then(Commands.argument("ppt", IntegerArgumentType.integer(ProgressionTier.MIN, ProgressionTier.MAX))
                            .executes(ctx -> forceOpen(ctx.getSource(), rarity, IntegerArgumentType.getInteger(ctx, "ppt")))));
        }

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("atg")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("ppt")
                        .executes(ctx -> showPpt(ctx.getSource()))
                        .then(Commands.literal("set")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(ProgressionTier.MIN, ProgressionTier.MAX))
                                        .executes(ctx -> setPpt(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "tier"))))))
                .then(Commands.literal("cache")
                        .executes(ctx -> giveCaches(ctx.getSource(), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> giveCaches(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))))
                .then(open);

        event.getDispatcher().register(root);
    }

    private static int showPpt(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        int ppt = ProgressionTier.get(p);
        src.sendSuccess(() -> Component.literal("PPT " + ppt + " — " + ProgressionTier.name(ppt))
                .withStyle(ChatFormatting.AQUA), false);
        return ppt;
    }

    private static int setPpt(CommandSourceStack src, int tier) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        p.setData(ATGAttachments.PROGRESSION_TIER.get(), tier);   // direct set (debug) — bypasses latch
        src.sendSuccess(() -> Component.literal("Set PPT = " + tier + " (" + ProgressionTier.name(tier) + ")")
                .withStyle(ChatFormatting.GREEN), true);
        return tier;
    }

    private static int giveCaches(CommandSourceStack src, int count) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        p.getInventory().placeItemBackInInventory(new ItemStack(ATGItems.ATM_CACHE.get(), count));
        src.sendSuccess(() -> Component.literal("Gave " + count + " ATM Cache(s)")
                .withStyle(ChatFormatting.GREEN), false);
        return count;
    }

    private static int forceOpen(CommandSourceStack src, RarityTier rarity, int pptOverride) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        int ppt = pptOverride >= 0 ? pptOverride : ProgressionTier.get(p);
        int granted = CacheOpening.open(p, ppt, rarity);
        src.sendSuccess(() -> Component.literal(
                        "Force-opened " + rarity.name() + " @ ppt " + ppt + " → " + granted + " item(s)")
                .withStyle(ChatFormatting.GRAY), false);
        return granted;
    }

    private ATGCommands() {}
}
