package net.luckyowlstudios.personality.panic;

import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.luckyowlstudios.personality.retaliation.AngryMob;
import net.luckyowlstudios.personality.sleep.PersonalityTags;
import net.luckyowlstudios.personality.sleep.SleepRules;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

/**
 * When a player hurts a skittish animal, every skittish animal around it panics and runs from that player.
 */
public final class HerdPanic {

    /** Vertical reach of the alarm, in blocks. */
    public static final double ALARM_HEIGHT = 6.0;

    /** Animals stop running early once the player is this far away. */
    public static final double SAFE_DISTANCE = 28.0;

    /** Speed multiplier while fleeing. */
    public static final double FLEE_SPEED = 1.5;

    private HerdPanic() {
    }

    /** Called on the server after {@code victim} was hurt by {@code player}. */
    public static void alarm(LivingEntity victim, Player player) {
        if (!PersonalityConfig.HERD_PANIC_ENABLED.get()) {
            return;
        }
        double radius = PersonalityConfig.ALARM_RADIUS.get();
        int scaredTicks = PersonalityConfig.ticks(PersonalityConfig.SCARED_SECONDS);
        if (!(victim instanceof PathfinderMob victimMob) || !isSkittish(victimMob)) {
            return;
        }
        for (PathfinderMob mob : victim.level().getEntitiesOfClass(PathfinderMob.class,
                victim.getBoundingBox().inflate(radius, ALARM_HEIGHT, radius), HerdPanic::isSkittish)) {
            if (mob instanceof SleepingMob sleeper && sleeper.personality$isSleeping()) {
                sleeper.personality$wakeUp(SleepRules.stayAwakeAfterHurt());
            }
            if (((AngryMob) mob).personality$getRetaliationTarget() != null) {
                continue; // Busy fighting back.
            }
            ((ScaredMob) mob).personality$scare(player, scaredTicks);
        }
    }

    /** Wild, unridden animals in the {@code personality:skittish} tag. Pets and mounts trust their owner. */
    public static boolean isSkittish(PathfinderMob mob) {
        if (!mob.isAlive() || mob.isNoAi() || !mob.getType().is(PersonalityTags.SKITTISH)) {
            return false;
        }
        if (mob instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        if (mob instanceof AbstractHorse horse && horse.isTamed()) {
            return false;
        }
        return !mob.isVehicle() && !mob.isLeashed();
    }
}
