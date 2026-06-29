package com.allthegoodies.client;

import com.allthegoodies.cache.RarityTier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/** Quick corner notification listing the actual loot won when a cache is opened. */
@OnlyIn(Dist.CLIENT)
public final class CacheOpenToast implements Toast {

    private static final long DISPLAY_MS = 4000L;
    private static final int MAX_ROWS = 6;
    private static final int ROW_H = 18;
    private static final int WIDTH = 200;

    private final RarityTier rarity;
    private final List<ItemStack> items;
    private long firstRenderTime = -1L;

    private CacheOpenToast(RarityTier rarity, List<ItemStack> items) {
        this.rarity = rarity;
        this.items = items;
    }

    /** Called from the packet handler on the main client thread. */
    public static void show(int rarityOrdinal, List<ItemStack> items) {
        RarityTier[] tiers = RarityTier.values();
        if (rarityOrdinal < 0 || rarityOrdinal >= tiers.length) return;
        Minecraft.getInstance().getToasts().addToast(new CacheOpenToast(tiers[rarityOrdinal], items));
    }

    @Override
    public Visibility render(GuiGraphics gfx, ToastComponent component, long timeSinceLastVisible) {
        if (firstRenderTime < 0) firstRenderTime = timeSinceLastVisible;
        Font font = component.getMinecraft().font;

        int w = width(), h = height();
        gfx.fill(0, 0, w, h, 0xCC1A1A1A);
        gfx.fill(0, 0, 3, h, 0xFF000000 | rarityColor());  // rarity accent bar

        // Header: "You got" tinted by the cache rarity.
        gfx.drawString(font, Component.literal("You got").withStyle(rarity.color), 9, 7, 0xFFFFFFFF, false);

        int rows = Math.min(items.size(), MAX_ROWS);
        int y = 20;
        for (int i = 0; i < rows; i++) {
            ItemStack stack = items.get(i);
            gfx.renderFakeItem(stack, 9, y - 4);
            String label = stack.getCount() + "× " + stack.getHoverName().getString();
            gfx.drawString(font, label, 30, y, 0xFFE0E0E0, false);
            y += ROW_H;
        }

        if (items.isEmpty()) {
            gfx.drawString(font, Component.literal("(nothing this time)").withStyle(ChatFormatting.DARK_GRAY),
                    9, y, 0xFF777777, false);
        } else if (items.size() > MAX_ROWS) {
            gfx.drawString(font, Component.literal("+" + (items.size() - MAX_ROWS) + " more")
                    .withStyle(ChatFormatting.GRAY), 9, y, 0xFFAAAAAA, false);
        }

        return (timeSinceLastVisible - firstRenderTime) < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        int rows = Math.max(1, Math.min(items.size(), MAX_ROWS));
        int extra = (items.isEmpty() || items.size() > MAX_ROWS) ? 12 : 0;
        return 18 + rows * ROW_H + extra;
    }

    /** ARGB from the rarity's ChatFormatting color, or white. */
    private int rarityColor() {
        Integer color = rarity.color.getColor();
        return color != null ? color : 0xFFFFFF;
    }
}
