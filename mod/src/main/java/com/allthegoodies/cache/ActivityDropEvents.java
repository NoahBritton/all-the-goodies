package com.allthegoodies.cache;

import com.allthegoodies.AllTheGoodies;
import com.allthegoodies.progression.ProgressionTier;
import com.allthegoodies.registry.ATGItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Organic cache drops during normal play (SPEC §3.3 "activity drops"):
 *   – Kill a hostile mob    → 5% chance of a PPT-appropriate colored cache
 *   – Mine an ore block     → 10% chance
 *
 * Drop rates are intentionally low so caches feel like lucky finds, not farm output.
 * An anti-farm limiter will layer on top in a follow-up ticket.
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID)
public final class ActivityDropEvents {

    /** 1-in-20 chance on mob kill */
    private static final int MOB_KILL_CHANCE = 20;

    /** 1-in-50 chance on ore break — keeps drops lucky even through vein mining */
    private static final int ORE_BREAK_CHANCE = 50;

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        // auto-respawn debug zombies
        if (event.getEntity().getTags().contains("atg_debug_zombie")) {
            Vec3 pos = event.getEntity().position();
            level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 1, () -> {
                    Zombie z = new Zombie(EntityType.ZOMBIE, level);
                    z.moveTo(pos.x, pos.y, pos.z, 0f, 0f);
                    z.setNoAi(true);
                    z.setPersistenceRequired();
                    z.addTag("atg_debug_zombie");
                    level.addFreshEntity(z);
                }));
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof Monster)) return;

        if (level.getRandom().nextInt(MOB_KILL_CHANCE) != 0) return;

        grantCache(player, level);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!event.getState().is(BlockTags.COAL_ORES)
                && !event.getState().is(BlockTags.IRON_ORES)
                && !event.getState().is(BlockTags.GOLD_ORES)
                && !event.getState().is(BlockTags.DIAMOND_ORES)
                && !event.getState().is(BlockTags.EMERALD_ORES)
                && !event.getState().is(BlockTags.LAPIS_ORES)
                && !event.getState().is(BlockTags.REDSTONE_ORES)
                && !event.getState().is(BlockTags.COPPER_ORES)) return;

        if (level.getRandom().nextInt(ORE_BREAK_CHANCE) != 0) return;

        grantCache(player, level);
    }

    private static void grantCache(ServerPlayer player, ServerLevel level) {
        if (!AntiFarmLimiter.tryGrant(player)) return;
        int ppt = ProgressionTier.get(player);
        RarityTier rarity = CacheRoller.rollRarity(level.getRandom());
        ItemStack cache = ATGItems.coloredCacheFor(rarity).get().getDefaultInstance();
        player.getInventory().placeItemBackInInventory(cache);
    }

    private ActivityDropEvents() {}
}
