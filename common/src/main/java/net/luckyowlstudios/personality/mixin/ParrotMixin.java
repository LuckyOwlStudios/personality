package net.luckyowlstudios.personality.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Parrot.class)
public abstract class ParrotMixin {

    /** Sleeping parrots don't imitate nearby mobs. */
    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Parrot;imitateNearbyMobs(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean personality$quietWhileSleeping(Level level, Entity parrot) {
        return !(this instanceof SleepingMob sleeper && sleeper.personality$isSleeping());
    }
}
