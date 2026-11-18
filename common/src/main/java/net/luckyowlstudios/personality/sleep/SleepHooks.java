package net.luckyowlstudios.personality.sleep;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

/**
 * Mob-specific touches when a mob falls asleep or wakes up (server side).
 */
public final class SleepHooks {

    private SleepHooks() {
    }

    public static void onFallAsleep(Mob mob) {
        if (mob instanceof Armadillo armadillo && !armadillo.isScared()) {
            // Curl up into the shell, quietly (vanilla's rollUp() plays a sound).
            armadillo.switchToState(Armadillo.ArmadilloState.ROLLING);
        }
    }

    public static void onWakeUp(Mob mob) {
        // A hurt armadillo stays rolled up and lets vanilla decide when it's safe to come out.
        if (mob instanceof Armadillo armadillo && armadillo.getState() == Armadillo.ArmadilloState.ROLLING && armadillo.hurtTime <= 0) {
            armadillo.rollOut();
        }
    }
}
