package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.sleep.SleepRules;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * A sleeping mob is immobile: vanilla then skips its whole AI step (goals, brain, pathfinding,
     * looking around) and zeroes its movement input, while gravity still applies.
     */
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void personality$immobileWhileSleeping(CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof SleepingMob sleeper && sleeper.personality$isSleeping()) {
            cir.setReturnValue(true);
        }
    }

    /** Getting hurt wakes a sleeping mob and keeps it up for a while. */
    @Inject(method = "hurt", at = @At("RETURN"))
    private void personality$wakeWhenHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && this instanceof SleepingMob sleeper && !((LivingEntity) (Object) this).level().isClientSide) {
            sleeper.personality$wakeUp(SleepRules.stayAwakeAfterHurt());
        }
    }
}
