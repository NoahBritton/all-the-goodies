package com.allthegoodies.client;

import com.allthegoodies.cache.RarityTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Quick corner notification shown when a colored cache is opened. */
@OnlyIn(Dist.CLIENT)
public final class CacheOpenToast implements Toast {

    private static final long DISPLAY_MS = 2500L;

    private final RarityTier rarity;
    private final int itemCount;
    private long firstRenderTime = -1L;

    private CacheOpenToast(RarityTier rarity, int itemCount) {
        this.rarity = rarity;
        this.itemCount = itemCount;
    }

    /** Called from the packet handler on the main client thread. */
    public static void show(int rarityOrdinal, int itemCount) {
        RarityTier[] tiers = RarityTier.values();
        if (rarityOrdinal < 0 || rarityOrdinal >= tiers.length) return;
        Minecraft.getInstance().getToasts().addToast(new CacheOpenToast(tiers[rarityOrdinal], itemCount));
    }

    @Override
    public Visibility render(GuiGraphics gfx, ToastComponent component, long timeSinceLastVisible) {
        if (firstRenderTime < 0) firstRenderTime = timeSinceLastVisible;

        // Background (dark)
        gfx.fill(0, 0, this.width(), this.height(), 0xCC1A1A1A);
        gfx.fill(0, 0, 3, this.height(), 0xFF000000 | rarityColor());

        // "Cache Opened" label
        gfx.drawString(component.getMinecraft().font,
                Component.literal("Cache Opened").withStyle(net.minecraft.ChatFormatting.WHITE),
                8, 7, 0xFFFFFF, false);

        // Rarity line
        String label = rarity.flavor + (itemCount > 0 ? "  ×" + itemCount : "");
        gfx.drawString(component.getMinecraft().font,
                Component.literal(label).withStyle(rarity.color),
                8, 18, 0xFFFFFF, false);

        return (timeSinceLastVisible - firstRenderTime) < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public int width() { return 160; }

    @Override
    public int height() { return 32; }

    /** Returns an ARGB int from the rarity's ChatFormatting color, or white. */
    private int rarityColor() {
        Integer color = rarity.color.getColor();
        return color != null ? color : 0xFFFFFF;
    }
}
