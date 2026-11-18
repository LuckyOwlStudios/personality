package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.weather.RainShelter;
import net.luckyowlstudios.personality.weather.ShelterFromRainGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobWeatherMixin {

    /** Priority 4: below panicking, breeding and temptation, above random strolling. */
    @Unique
    private static final int PERSONALITY$SHELTER_PRIORITY = 4;

    @Shadow
    @Final
    protected GoalSelector goalSelector;

    /** Every pathfinding mob gets the goal; it only runs for mobs in the {@code personality:dislikes_rain} tag. */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void personality$addShelterGoal(EntityType<? extends Mob> type, Level level, CallbackInfo ci) {
        if (level != null && !level.isClientSide && (Object) this instanceof PathfinderMob pathfinder) {
            this.goalSelector.addGoal(PERSONALITY$SHELTER_PRIORITY, new ShelterFromRainGoal(pathfinder));
        }
    }

    /** Brain-driven animals don't use goals, so they're steered to cover here. Skipped while asleep (immobile). */
    @Inject(method = "serverAiStep", at = @At("TAIL"))
    private void personality$tickBrainShelter(CallbackInfo ci) {
        RainShelter.tickBrain((Mob) (Object) this);
    }
}
