package com.allthegoodies.registry;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ATGCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AllTheGoodies.MODID);

    public static final Supplier<CreativeModeTab> ATG_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("All The Goodies"))
                    .icon(() -> new ItemStack(ATGItems.ATM_CACHE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ATGItems.ATM_CACHE.get());
                        output.accept(ATGItems.CACHE_CONSUMER.get());
                        output.accept(ATGItems.CACHE_INDUSTRIAL.get());
                        output.accept(ATGItems.CACHE_MILSPEC.get());
                        output.accept(ATGItems.CACHE_RESTRICTED.get());
                        output.accept(ATGItems.CACHE_CLASSIFIED.get());
                        output.accept(ATGItems.CACHE_COVERT.get());
                        output.accept(ATGBlocks.WILD_CACHE.asItem());
                        // Patchouli guide book (only if Patchouli is loaded)
                        if (ModList.get().isLoaded("patchouli")) {
                            ItemStack book = vazkii.patchouli.api.PatchouliAPI.get()
                                    .getBookStack(ResourceLocation.fromNamespaceAndPath(AllTheGoodies.MODID, "what_now"));
                            if (!book.isEmpty()) output.accept(book);
                        }
                    })
                    .build());

    private ATGCreativeTab() {}
}
