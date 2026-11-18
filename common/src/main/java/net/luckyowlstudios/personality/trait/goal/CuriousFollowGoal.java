package net.luckyowlstudios.personality.trait.goal;

import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.luckyowlstudios.personality.trait.TraitHolder;
import net.luckyowlstudios.personality.trait.Traits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Curious animals now and then wander over to a nearby player or villager and tag along for a bit,
 * keeping a polite distance and watching what they do.
 */
public class CuriousFollowGoal extends Goal {

    /** Average ticks between moments of curiosity while someone is around. */
    private static final int CURIOSITY_INTERVAL = 600;
    private static final double NOTICE_RANGE = 10.0;
    private static final double GIVE_UP_RANGE = 16.0;
    private static final double STOP_DISTANCE = 3.0;
    private static final int MIN_FOLLOW_TICKS = 200;
    private static final int MAX_FOLLOW_TICKS = 500;
    private static final int MIN_COOLDOWN = 1200;
    private static final int MAX_COOLDOWN = 2400;
    private static final double SPEED = 1.0;

    private final PathfinderMob mob;
    private final TargetingConditions conditions = TargetingConditions.forNonCombat().range(NOTICE_RANGE);

    @Nullable
    private LivingEntity target;
    private int followTicks;
    private int timeToRecalcPath;
    private long nextCuriousTime;

    public CuriousFollowGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob instanceof TraitHolder holder) || !holder.personality$hasTrait(Traits.CURIOUS)) {
            return false;
        }
        // Brain-driven mobs (goats, camels, frogs...) steer themselves; a goal pathing them around would fight the brain.
        if (this.mob.getBrain().getActiveNonCoreActivity().isPresent()) {
            return false;
        }
        if (this.mob.level().getGameTime() < this.nextCuriousTime || !this.isFreeToWander()) {
            return false;
        }
        if (this.mob.getRandom().nextInt(reducedTickDelay(CURIOSITY_INTERVAL)) != 0) {
            return false;
        }
        this.target = this.findTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && !this.target.isSpectator()
                && this.followTicks > 0 && this.isFreeToWander()
                && this.mob.distanceToSqr(this.target) < GIVE_UP_RANGE * GIVE_UP_RANGE;
    }

    @Override
    public void start() {
        this.followTicks = this.adjustedTickDelay(MIN_FOLLOW_TICKS + this.mob.getRandom().nextInt(MAX_FOLLOW_TICKS - MIN_FOLLOW_TICKS));
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
        this.nextCuriousTime = this.mob.level().getGameTime() + MIN_COOLDOWN + this.mob.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }
        this.followTicks--;
        this.mob.getLookControl().setLookAt(this.target, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);
        if (this.mob.distanceToSqr(this.target) > STOP_DISTANCE * STOP_DISTANCE) {
            this.mob.getNavigation().moveTo(this.target, SPEED);
        } else {
            this.mob.getNavigation().stop();
        }
    }

    private boolean isFreeToWander() {
        if (this.mob.isLeashed() || this.mob.isPassenger() || this.mob.isVehicle()) {
            return false;
        }
        if (this.mob instanceof SleepingMob sleeper && sleeper.personality$isSleeping()) {
            return false;
        }
        return !(this.mob instanceof TamableAnimal pet && pet.isOrderedToSit());
    }

    @Nullable
    private LivingEntity findTarget() {
        Player player = this.mob.level().getNearestPlayer(this.conditions, this.mob);
        AbstractVillager villager = this.mob.level().getNearestEntity(AbstractVillager.class, this.conditions, this.mob,
                this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(NOTICE_RANGE, 4.0, NOTICE_RANGE));
        if (player == null) {
            return villager;
        }
        if (villager == null) {
            return player;
        }
        return this.mob.distanceToSqr(player) <= this.mob.distanceToSqr(villager) ? player : villager;
    }
}
