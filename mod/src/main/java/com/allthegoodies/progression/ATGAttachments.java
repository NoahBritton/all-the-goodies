package com.allthegoodies.progression;

import com.allthegoodies.AllTheGoodies;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Data attachments that persist mod state on entities/players. */
public final class ATGAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AllTheGoodies.MODID);

    /**
     * Player Progression Tier (PPT), 0..6. Persists across sessions and is copied on death.
     * Only ever ratchets upward (see {@link ProgressionTier}).
     */
    public static final Supplier<AttachmentType<Integer>> PROGRESSION_TIER =
            ATTACHMENTS.register("progression_tier", () ->
                    AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT)
                            .copyOnDeath()
                            .build());

    /** How many ATM Stars this player has crafted/obtained. ATM10 requires 25 to complete. */
    public static final Supplier<AttachmentType<Integer>> ATM_STAR_COUNT =
            ATTACHMENTS.register("atm_star_count", () ->
                    AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT)
                            .copyOnDeath()
                            .build());

    private ATGAttachments() {}
}
