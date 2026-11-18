package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.retaliation.AngryMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Brain-driven animals (goats) don't panic while they're fighting back. */
@Mixin(AnimalPanic.class)
public abstract class AnimalPanicMixin {

    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;)Z",
            at = @At("HEAD"), cancellable = true)
    private void personality$noPanicWhenAngry(ServerLevel level, PathfinderMob mob, CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof AngryMob angry && angry.personality$getRetaliationTarget() != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)Z",
            at = @At("HEAD"), cancellable = true)
    private void personality$stopPanicWhenAngry(ServerLevel level, PathfinderMob mob, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof AngryMob angry && angry.personality$getRetaliationTarget() != null) {
            cir.setReturnValue(false);
        }
    }
}
