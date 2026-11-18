package net.luckyowlstudios.personality.sleep;

/**
 * Implemented on every {@link net.minecraft.world.entity.Mob} through a mixin.
 */
public interface SleepingMob {

    /** True while the mob is asleep. Synced to clients. */
    boolean personality$isSleeping();

    /** Server only. Puts the mob to sleep or wakes it. */
    void personality$setSleeping(boolean sleeping);

    /** Server only. Wakes the mob and keeps it awake for the given number of ticks. */
    void personality$wakeUp(int stayAwakeTicks);

    /** Client only. 0 = fully awake, 1 = fully lying down with eyes shut. */
    float personality$getSleepProgress(float partialTick);
}
