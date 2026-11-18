package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.panic.FleeThreatGoal;
import net.luckyowlstudios.personality.panic.HerdPanic;
import net.luckyowlstudios.personality.panic.ScaredMob;
import net.luckyowlstudios.personality.retaliation.AngryMob;
import net.luckyowlstudios.personality.retaliation.RetaliateGoal;
import net.luckyowlstudios.personality.retaliation.Retaliation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin extends Mob implements ScaredMob, AngryMob {

    @Unique
    @Nullable
    private Entity personality$threat;

    /** Game time at which the mob calms down. */
    @Unique
    private long personality$scaredUntil;

    @Unique
    @Nullable
    private LivingEntity personality$retaliationTarget;

    /** Game time at which the mob cools off. */
    @Unique
    private long personality$angryUntil;

    protected PathfinderMobMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /**
     * Every pathfinding mob gets the goals; they only ever run once {@link HerdPanic} scares the mob or a player
     * provokes it (see {@link Retaliation}).
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void personality$addFleeGoal(EntityType<? extends PathfinderMob> type, Level level, CallbackInfo ci) {
        if (level != null && !level.isClientSide) {
            this.goalSelector.addGoal(0, new FleeThreatGoal((PathfinderMob) (Object) this));
            this.goalSelector.addGoal(0, new RetaliateGoal((PathfinderMob) (Object) this));
        }
    }

    @Override
    public void personality$scare(Entity threat, int ticks) {
        this.personality$threat = threat;
        this.personality$scaredUntil = Math.max(this.personality$scaredUntil, this.level().getGameTime() + ticks);
    }

    @Override
    @Nullable
    public Entity personality$getThreat() {
        Entity threat = this.personality$threat;
        if (threat == null) {
            return null;
        }
        if (this.level().getGameTime() >= this.personality$scaredUntil || !threat.isAlive() || threat.level() != this.level()
                || threat.distanceToSqr(this) > HerdPanic.SAFE_DISTANCE * HerdPanic.SAFE_DISTANCE
                || !HerdPanic.isSkittish((PathfinderMob) (Object) this)
                || this.personality$getRetaliationTarget() != null) {
            this.personality$threat = null;
            return null;
        }
        return threat;
    }

    @Override
    public void personality$provoke(LivingEntity target, int ticks) {
        this.personality$retaliationTarget = target;
        this.personality$angryUntil = Math.max(this.personality$angryUntil, this.level().getGameTime() + ticks);
    }

    @Override
    @Nullable
    public LivingEntity personality$getRetaliationTarget() {
        LivingEntity target = this.personality$retaliationTarget;
        if (target == null) {
            return null;
        }
        PathfinderMob self = (PathfinderMob) (Object) this;
        if (this.level().getGameTime() >= this.personality$angryUntil || !Retaliation.canRetaliate(self)
                || !Retaliation.isValidTarget(self, target)) {
            this.personality$retaliationTarget = null;
            return null;
        }
        return target;
    }
}
