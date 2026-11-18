package net.luckyowlstudios.personality.mixin;

import net.luckyowlstudios.personality.trait.goal.CuriousFollowGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobTraitsMixin extends Mob {

    protected PathfinderMobTraitsMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /**
     * Trait goals go on every pathfinding mob and only run when the mob has the matching trait.
     * Priority 5 sits below panicking, breeding, tempting and following parents, and above idle wandering.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void personality$addTraitGoals(EntityType<? extends PathfinderMob> type, Level level, CallbackInfo ci) {
        if (level != null && !level.isClientSide) {
            this.goalSelector.addGoal(5, new CuriousFollowGoal((PathfinderMob) (Object) this));
        }
    }
}
