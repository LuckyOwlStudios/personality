package net.luckyowlstudios.personality.sleep;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Server-side rules for falling asleep and waking up.
 */
public final class SleepRules {

    /** How often (in ticks) a mob re-checks its schedule. Mobs are staggered by entity id. */
    public static final int CHECK_INTERVAL = 20;

    /** Ticks a mob stays awake after being hurt. */
    public static final int STAY_AWAKE_AFTER_HURT = 1200;

    /** Ticks a mob stays awake after a player interacts with it. */
    public static final int STAY_AWAKE_AFTER_INTERACT = 200;

    /** Ticks a mob stays awake after a running player startles it. */
    public static final int STAY_AWAKE_AFTER_STARTLED = 400;

    /** Players who sprint within this many blocks wake sleeping animals. Sneak or walk to let them be. */
    private static final double STARTLE_RADIUS = 4.0;

    /** Tamed pets that follow their owner only sleep if the owner is asleep or this far away. */
    private static final double OWNER_AWAY_DISTANCE = 24.0;

    /** A mob hurt by something this recently is too frightened to sleep. */
    private static final int FRIGHTENED_TICKS = 600;

    private SleepRules() {
    }

    public static boolean isSleeper(Mob mob) {
        return mob.getType().is(PersonalityTags.SLEEPS);
    }

    public static boolean isNocturnal(Mob mob) {
        return mob.getType().is(PersonalityTags.NOCTURNAL);
    }

    /** Whether the clock says it is this mob's bedtime. Dimensions with a fixed time (Nether, End) have no bedtime. */
    public static boolean isBedtime(Mob mob) {
        Level level = mob.level();
        if (level.dimensionType().hasFixedTime()) {
            return false;
        }
        return SleepSchedule.isBedtime(mob, level.getDayTime(), isNocturnal(mob));
    }

    /** Everything other than the clock that must be true for a mob to fall or stay asleep. */
    public static boolean isSettled(Mob mob) {
        if (!mob.isAlive() || mob.isNoAi()) {
            return false;
        }
        if (!mob.onGround() || mob.isInWater() || mob.isInLava() || mob.isInPowderSnow) {
            return false;
        }
        if (mob.isPassenger() || mob.isVehicle() || mob.isLeashed()) {
            return false;
        }
        if (mob.getTarget() != null || mob.isAggressive()) {
            return false;
        }
        if (mob.getLastHurtByMob() != null && mob.tickCount - mob.getLastHurtByMobTimestamp() < FRIGHTENED_TICKS) {
            return false;
        }
        if (mob instanceof Animal animal && animal.isInLove()) {
            return false;
        }
        if (mob instanceof TamableAnimal pet && pet.isTame() && !pet.isOrderedToSit()) {
            // A pet following its owner stays up until the owner goes to bed or wanders off.
            LivingEntity owner = pet.getOwner();
            if (owner != null && !owner.isSleeping() && owner.distanceToSqr(pet) < OWNER_AWAY_DISTANCE * OWNER_AWAY_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    /** True if a player is sprinting close enough to startle the mob awake. */
    public static boolean isStartled(Mob mob) {
        Player player = mob.level().getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), STARTLE_RADIUS, EntitySelector.NO_SPECTATORS);
        return player != null && player.isSprinting();
    }
}
