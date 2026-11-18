package net.luckyowlstudios.personality.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.luckyowlstudios.personality.sleep.SleepHooks;
import net.luckyowlstudios.personality.sleep.SleepRules;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements SleepingMob {

    @Unique
    private static final EntityDataAccessor<Boolean> PERSONALITY$SLEEPING = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BOOLEAN);

    /** Ticks the mob must stay awake before it may sleep again (server). */
    @Unique
    private int personality$stayAwakeTicks;

    /** Lying-down animation progress, 0..1 (client). */
    @Unique
    private float personality$sleepProgress;

    @Unique
    private float personality$prevSleepProgress;

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void personality$defineSleepData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PERSONALITY$SLEEPING, false);
    }

    @Override
    public boolean personality$isSleeping() {
        return this.entityData.get(PERSONALITY$SLEEPING);
    }

    @Override
    public void personality$setSleeping(boolean sleeping) {
        if (sleeping == this.personality$isSleeping()) {
            return;
        }
        this.entityData.set(PERSONALITY$SLEEPING, sleeping);
        if (sleeping) {
            Mob self = (Mob) (Object) this;
            self.getNavigation().stop();
            self.setJumping(false);
            self.setSprinting(false);
            self.xxa = 0;
            self.yya = 0;
            self.zza = 0;
            self.setXRot(0);
            Vec3 motion = self.getDeltaMovement();
            self.setDeltaMovement(0, Math.min(motion.y, 0), 0);
            SleepHooks.onFallAsleep(self);
        } else {
            SleepHooks.onWakeUp((Mob) (Object) this);
        }
    }

    @Override
    public void personality$wakeUp(int stayAwakeTicks) {
        this.personality$stayAwakeTicks = Math.max(this.personality$stayAwakeTicks, stayAwakeTicks);
        this.personality$setSleeping(false);
    }

    @Override
    public float personality$getSleepProgress(float partialTick) {
        return Mth.lerp(partialTick, this.personality$prevSleepProgress, this.personality$sleepProgress);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void personality$tickSleep(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (this.level().isClientSide) {
            this.personality$prevSleepProgress = this.personality$sleepProgress;
            float target = this.personality$isSleeping() ? 1.0F : 0.0F;
            // Lie down over ~1.5 seconds, get up a bit quicker.
            float step = target > this.personality$sleepProgress ? 0.035F : 0.1F;
            this.personality$sleepProgress = Mth.approach(this.personality$sleepProgress, target, step);
            return;
        }

        if (this.personality$stayAwakeTicks > 0) {
            this.personality$stayAwakeTicks--;
        }

        boolean sleeping = this.personality$isSleeping();
        if (!sleeping && !SleepRules.isSleeper(self)) {
            return;
        }

        if (sleeping) {
            // Checked every tick so the mob reacts at once to danger or being moved.
            if (!SleepRules.isSleeper(self) || !SleepRules.isSettled(self)) {
                this.personality$wakeUp(0);
                return;
            }
            if ((self.tickCount + self.getId()) % 10 == 0 && SleepRules.isStartled(self)) {
                this.personality$wakeUp(SleepRules.STAY_AWAKE_AFTER_STARTLED);
                return;
            }
        }

        if ((self.tickCount + self.getId()) % SleepRules.CHECK_INTERVAL != 0) {
            return;
        }

        boolean bedtime = SleepRules.isBedtime(self);
        if (sleeping && !bedtime) {
            this.personality$setSleeping(false);
        } else if (!sleeping && bedtime && this.personality$stayAwakeTicks <= 0 && SleepRules.isSettled(self)) {
            this.personality$setSleeping(true);
        }
    }

    /** Sleeping animals make no ambient noise. */
    @WrapWithCondition(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;playAmbientSound()V"))
    private boolean personality$silenceWhileSleeping(Mob mob) {
        return !this.personality$isSleeping();
    }

    /** Interacting with a sleeping animal (feeding, leashing, petting) wakes it. */
    @Inject(method = "interact", at = @At("HEAD"))
    private void personality$wakeOnInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!this.level().isClientSide && this.personality$isSleeping()) {
            this.personality$wakeUp(SleepRules.STAY_AWAKE_AFTER_INTERACT);
        }
    }
}
