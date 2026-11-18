package net.luckyowlstudios.personality.weather;

import net.luckyowlstudios.personality.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Server-side rules for animals that dislike rain: finding cover, and steering brain-driven animals to it.
 */
public final class RainShelter {

    /** Animals that hide from rain and won't sleep in it. Datapacks can add or remove mobs here. */
    public static final TagKey<EntityType<?>> DISLIKES_RAIN = TagKey.create(Registries.ENTITY_TYPE, Constants.id("dislikes_rain"));

    /** Random spots sampled per search; the closest covered one wins. */
    private static final int SEARCH_ATTEMPTS = 24;

    /** How often (in ticks) brain-driven animals look for cover while out in the rain. */
    private static final int BRAIN_SEARCH_INTERVAL = 40;

    private RainShelter() {
    }

    public static boolean dislikesRain(Mob mob) {
        return mob.getType().is(DISLIKES_RAIN);
    }

    /** True if rain is falling on the mob's feet or head right now. Snow and dry biomes don't count. */
    public static boolean isExposedToRain(Mob mob) {
        Level level = mob.level();
        if (!level.isRaining()) {
            return false;
        }
        BlockPos feet = mob.blockPosition();
        return level.isRainingAt(feet) || level.isRainingAt(BlockPos.containing(mob.getX(), mob.getBoundingBox().maxY, mob.getZ()));
    }

    /** True if something solid (leaves, a roof, an overhang) stops rain from reaching this spot. */
    public static boolean isCovered(Level level, BlockPos pos) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY();
    }

    /** Brain-driven animals (goats, camels, armadillos, sniffers) walk via the WALK_TARGET memory, not goals. */
    public static boolean isBrainDriven(Mob mob) {
        return mob.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
    }

    /** Whether the animal is free to go looking for cover (not leashed, ridden, fighting or following its owner). */
    public static boolean canSeekShelter(Mob mob) {
        if (!mob.isAlive() || mob.isNoAi() || mob.isLeashed() || mob.isPassenger() || mob.isVehicle()) {
            return false;
        }
        if (mob.getTarget() != null || mob.isAggressive()) {
            return false;
        }
        return !(mob instanceof TamableAnimal pet && pet.isTame());
    }

    /** Looks for the closest covered spot the mob could stand on, or null if there's none nearby. */
    @Nullable
    public static BlockPos findShelter(PathfinderMob mob) {
        Level level = mob.level();
        RandomSource random = mob.getRandom();
        BlockPos origin = mob.blockPosition();
        int radius = Math.max(1, WeatherSettings.shelterSearchRadius);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < SEARCH_ATTEMPTS; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            BlockPos spot = findStandingSpot(mob, x, origin.getY(), z);
            if (spot == null || !isCovered(level, spot)) {
                continue;
            }
            double distance = spot.distSqr(origin);
            if (distance < bestDistance) {
                best = spot;
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Scans a short column for a dry, open spot with solid ground underneath. */
    @Nullable
    private static BlockPos findStandingSpot(Mob mob, int x, int y, int z) {
        Level level = mob.level();
        int headroom = Math.max(1, (int) Math.ceil(mob.getBbHeight()));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -4; dy--) {
            pos.set(x, y + dy, z);
            if (!level.isLoaded(pos)) {
                return null;
            }
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                continue;
            }
            if (isOpen(level, pos, headroom)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static boolean isOpen(Level level, BlockPos pos, int headroom) {
        for (int i = 0; i < headroom; i++) {
            BlockPos check = pos.above(i);
            if (!level.getFluidState(check).isEmpty() || !level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called every server AI step. Brain-driven animals out in the rain get a walk target under cover;
     * once covered, any stroll that would lead them back out into the rain is cancelled.
     */
    public static void tickBrain(Mob mob) {
        if (!WeatherSettings.seekShelterInRain || !(mob instanceof PathfinderMob pathfinder) || !mob.level().isRaining()) {
            return;
        }
        if ((mob.tickCount + mob.getId()) % 10 != 0) {
            return;
        }
        if (!dislikesRain(mob) || !isBrainDriven(mob) || !canSeekShelter(mob)) {
            return;
        }
        Brain<?> brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.IS_PANICKING) || brain.hasMemoryValue(MemoryModuleType.TEMPTING_PLAYER)
                || brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) || brain.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
            return;
        }
        Level level = mob.level();
        Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
        boolean headingForCover = walkTarget.isPresent() && isCovered(level, walkTarget.get().getTarget().currentBlockPosition());
        if (isExposedToRain(mob)) {
            if (headingForCover || (mob.tickCount + mob.getId()) % BRAIN_SEARCH_INTERVAL != 0) {
                return;
            }
            BlockPos shelter = findShelter(pathfinder);
            if (shelter != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(shelter, (float) WeatherSettings.shelterSpeed, 0));
            }
        } else if (walkTarget.isPresent() && !headingForCover) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
