package net.luckyowlstudios.personality.retaliation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.EnumSet;

/**
 * Charges whoever provoked the mob (see {@link Retaliation}) and headbutts them.
 *
 * <p>Goal-driven mobs (cows) are steered through their navigation. Brain-driven mobs (goats) still tick their
 * goal selector, so the goal runs for them too, but it steers them through their walk and look target memories
 * so their brain doesn't fight it.
 */
public class RetaliateGoal extends Goal {

    private final PathfinderMob mob;
    private int attackCooldown;
    private int repathDelay;

    public RetaliateGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ((AngryMob) this.mob).personality$getRetaliationTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repathDelay = 0;
        // A short wind-up so the first headbutt doesn't land the same tick the player hits.
        this.attackCooldown = Math.max(this.attackCooldown, 10);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        Brain<?> brain = this.mob.getBrain();
        if (usesBrain(brain)) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = ((AngryMob) this.mob).personality$getRetaliationTarget();
        if (target == null) {
            return;
        }
        Brain<?> brain = this.mob.getBrain();
        if (usesBrain(brain)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(target, false), Retaliation.CHARGE_SPEED, 0));
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        } else if (--this.repathDelay <= 0) {
            this.repathDelay = 4 + this.mob.getRandom().nextInt(5);
            this.mob.getNavigation().moveTo(target, Retaliation.CHARGE_SPEED);
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        } else if (this.mob.isWithinMeleeAttackRange(target) && this.mob.getSensing().hasLineOfSight(target)) {
            this.attackCooldown = Retaliation.ATTACK_COOLDOWN;
            Retaliation.headbutt(this.mob, target);
        }
    }

    private static boolean usesBrain(Brain<?> brain) {
        return brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
    }
}
