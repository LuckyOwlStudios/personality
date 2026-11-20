package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.panic.HerdPanic;
import net.luckyowlstudios.personality.retaliation.Retaliation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPanicMixin {

    /**
     * A player hurting an animal (directly or with a projectile) scares the whole herd away from them. Animals that
     * fight back (see {@link Retaliation}) go after the player instead of joining the stampede.
     */
    @Inject(method = "hurt", at = @At("RETURN"))
    private void personality$alarmHerd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (cir.getReturnValueZ() && !self.level().isClientSide && source.getEntity() instanceof Player player) {
            Retaliation.provoke(self, player);
            HerdPanic.alarm(self, player);
        }
    }
}
