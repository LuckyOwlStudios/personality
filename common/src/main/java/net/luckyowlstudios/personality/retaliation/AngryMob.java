package net.luckyowlstudios.personality.retaliation;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented on every {@link net.minecraft.world.entity.PathfinderMob} through a mixin. Server only.
 */
public interface AngryMob {

    /** Makes the mob go after {@code target} for the given number of ticks. */
    void personality$provoke(LivingEntity target, int ticks);

    /** Whoever this mob is currently fighting back against, or null once it has cooled off. */
    @Nullable
    LivingEntity personality$getRetaliationTarget();
}
