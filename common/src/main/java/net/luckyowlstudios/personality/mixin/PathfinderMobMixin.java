package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.panic.FleeThreatGoal;
import net.luckyowlstudios.personality.panic.HerdPanic;
import net.luckyowlstudios.personality.panic.ScaredMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
public abstract class PathfinderMobMixin extends Mob implements ScaredMob {

    @Unique
    @Nullable
    private Entity personality$threat;

    /** Game time at which the mob calms down. */
    @Unique
    private long personality$scaredUntil;

    protected PathfinderMobMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /** Every pathfinding mob gets the goal; it only ever runs once {@link HerdPanic} scares the mob. */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void personality$addFleeGoal(EntityType<? extends PathfinderMob> type, Level level, CallbackInfo ci) {
        if (level != null && !level.isClientSide) {
            this.goalSelector.addGoal(0, new FleeThreatGoal((PathfinderMob) (Object) this));
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
                || !HerdPanic.isSkittish((PathfinderMob) (Object) this)) {
            this.personality$threat = null;
            return null;
        }
        return threat;
    }
}
