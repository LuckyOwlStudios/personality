package net.luckyowlstudios.personality.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Walks a rain-shy animal to the nearest cover when it rains, like zombies and skeletons hiding from the sun,
 * then keeps it there (blocking random strolls) until the rain stops. Under cover it is free to fall asleep.
 */
public class ShelterFromRainGoal extends Goal {

    /** Ticks between searches after one comes up empty, so animals in an open field don't search every tick. */
    private static final int SEARCH_COOLDOWN = 40;

    private final PathfinderMob mob;

    @Nullable
    private BlockPos shelter;

    private long nextSearchTime;

    public ShelterFromRainGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private boolean isActive() {
        return WeatherSettings.seekShelterInRain && this.mob.level().isRaining() && RainShelter.dislikesRain(this.mob)
                && !RainShelter.isBrainDriven(this.mob) && RainShelter.canSeekShelter(this.mob);
    }

    @Override
    public boolean canUse() {
        if (!this.isActive() || this.mob.level().getGameTime() < this.nextSearchTime || !RainShelter.isExposedToRain(this.mob)) {
            return false;
        }
        this.nextSearchTime = this.mob.level().getGameTime() + SEARCH_COOLDOWN + this.mob.getRandom().nextInt(20);
        this.shelter = RainShelter.findShelter(this.mob);
        return this.shelter != null;
    }

    @Override
    public void start() {
        if (this.shelter != null) {
            this.mob.getNavigation().moveTo(this.shelter.getX() + 0.5, this.shelter.getY(), this.shelter.getZ() + 0.5, WeatherSettings.shelterSpeed);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.isActive()) {
            return false;
        }
        // Keep walking while the path lasts; once there, stay put under cover until the rain stops.
        return !this.mob.getNavigation().isDone() || !RainShelter.isExposedToRain(this.mob);
    }

    @Override
    public void stop() {
        this.shelter = null;
        this.mob.getNavigation().stop();
    }
}
