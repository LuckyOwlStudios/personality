package net.luckyowlstudios.personality.panic;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented on every {@link net.minecraft.world.entity.PathfinderMob} through a mixin. Server only.
 */
public interface ScaredMob {

    /** Makes the mob run away from {@code threat} for the given number of ticks. */
    void personality$scare(Entity threat, int ticks);

    /** The entity this mob is currently running from, or null once it has calmed down. */
    @Nullable
    Entity personality$getThreat();
}
