package net.luckyowlstudios.personality.panic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Runs away from whatever scared the mob (see {@link HerdPanic}), picking a new spot each time it arrives
 * until it calms down.
 */
public class FleeThreatGoal extends Goal {

    private final PathfinderMob mob;
    @Nullable
    private Path path;

    public FleeThreatGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Entity threat = ((ScaredMob) this.mob).personality$getThreat();
        if (threat == null) {
            return false;
        }
        Vec3 target = DefaultRandomPos.getPosAway(this.mob, 16, 7, threat.position());
        if (target == null) {
            // Cornered: any direction will do.
            target = DefaultRandomPos.getPos(this.mob, 8, 4);
        }
        if (target == null) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(target.x, target.y, target.z, 0);
        return this.path != null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, HerdPanic.FLEE_SPEED);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && ((ScaredMob) this.mob).personality$getThreat() != null;
    }

    @Override
    public void stop() {
        this.path = null;
    }
}
