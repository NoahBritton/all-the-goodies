package com.allthegoodies.registry;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.block.WildCacheBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATGBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AllTheGoodies.MODID);

    public static final DeferredBlock<Block> WILD_CACHE = BLOCKS.register(
            "wild_cache",
            () -> new WildCacheBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    private ATGBlocks() {}
}
