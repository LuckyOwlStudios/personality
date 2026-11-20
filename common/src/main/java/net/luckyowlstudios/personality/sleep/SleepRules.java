package net.luckyowlstudios.personality.sleep;

import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.luckyowlstudios.personality.weather.RainShelter;
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

    // Tunable values live in PersonalityConfig (config/personality.json).

    /** Ticks a mob stays awake after being hurt. */
    public static int stayAwakeAfterHurt() {
        return PersonalityConfig.ticks(PersonalityConfig.STAY_AWAKE_AFTER_HURT);
    }

    /** Ticks a mob stays awake after a player interacts with it. */
    public static int stayAwakeAfterInteract() {
        return PersonalityConfig.ticks(PersonalityConfig.STAY_AWAKE_AFTER_INTERACT);
    }

    /** Ticks a mob stays awake after a running player startles it. */
    public static int stayAwakeAfterStartled() {
        return PersonalityConfig.ticks(PersonalityConfig.STAY_AWAKE_AFTER_STARTLED);
    }

    private SleepRules() {
    }

    public static boolean isSleeper(Mob mob) {
        return PersonalityConfig.SLEEP_ENABLED.get() && mob.getType().is(PersonalityTags.SLEEPS);
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
        if (PersonalityConfig.NO_SLEEPING_IN_RAIN.get() && RainShelter.dislikesRain(mob) && RainShelter.isExposedToRain(mob)) {
            // Too wet to sleep: the animal stays up (and goes looking for cover) until it's dry.
            return false;
        }
        if (mob.getTarget() != null || mob.isAggressive()) {
            return false;
        }
        if (mob.getLastHurtByMob() != null && mob.tickCount - mob.getLastHurtByMobTimestamp() < PersonalityConfig.ticks(PersonalityConfig.FRIGHTENED_SECONDS)) {
            return false;
        }
        if (mob instanceof Animal animal && animal.isInLove()) {
            return false;
        }
        if (PersonalityConfig.PETS_WAIT_FOR_OWNER.get() && mob instanceof TamableAnimal pet && pet.isTame() && !pet.isOrderedToSit()) {
            // A pet following its owner stays up until the owner goes to bed or wanders off.
            LivingEntity owner = pet.getOwner();
            double awayDistance = PersonalityConfig.OWNER_AWAY_DISTANCE.get();
            if (owner != null && !owner.isSleeping() && owner.distanceToSqr(pet) < awayDistance * awayDistance) {
                return false;
            }
        }
        return true;
    }

    /** True if a player is sprinting close enough to startle the mob awake. */
    public static boolean isStartled(Mob mob) {
        double radius = PersonalityConfig.STARTLE_RADIUS.get();
        if (radius <= 0.0) {
            return false;
        }
        Player player = mob.level().getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), radius, EntitySelector.NO_SPECTATORS);
        return player != null && player.isSprinting();
    }
}
