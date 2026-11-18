package net.luckyowlstudios.personality.client.sleep;

import net.luckyowlstudios.personality.sleep.PersonalityTags;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The generic "lying down" pose used for every sleeping mob.
 * <p>
 * Instead of hand-animating each animal, legs are found by their part names ("left_front_leg",
 * "right_hind_leg", "left_leg" ...) and folded forward under the body, the head droops a little, and the
 * whole model is lowered by its leg length so the belly rests on the ground. This works for any model that
 * follows vanilla's naming, including most modded animals.
 */
public final class SleepPose {

    private static final float LEG_FOLD = -Mth.HALF_PI;
    private static final float HEAD_DROOP = 0.35F;
    /** Horses carry their head on a neck part ("head_parts"); a sleeping horse lets its neck hang low. */
    private static final float NECK_DROOP = 0.9F;

    private static final Map<EntityModel<?>, ModelInfo> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private SleepPose() {
    }

    /**
     * Whether legs should fold for this entity right now. Standing sleepers (horses) keep their legs straight,
     * and sitting pets and camels are already resting.
     */
    public static boolean shouldFoldLegs(LivingEntity entity, EntityModel<?> model) {
        if (entity.getType().is(PersonalityTags.SLEEPS_STANDING)) {
            return false;
        }
        if (entity.hasPose(Pose.SITTING)) {
            return false;
        }
        if (entity instanceof TamableAnimal pet && pet.isInSittingPose()) {
            return false;
        }
        return info(model).canFold();
    }

    /** How far to lower the model, in model pixels, when fully asleep. */
    public static float loweringPixels(LivingEntity entity, EntityModel<?> model) {
        float lower = info(model).lowering();
        return entity.isBaby() ? lower * 0.5F : lower;
    }

    // --- Per-part hooks, called from ModelPart#render ---

    /** Applies the sleeping pose to a part about to render. Returns the pose to restore afterwards, or null. */
    public static PartPose apply(ModelPart part) {
        SleepRenderContext.State state = SleepRenderContext.current();
        if (!state.active()) {
            return null;
        }
        String name = NamedModelPart.of(part).personality$getName();
        float p = ease(state.progress());
        if (state.foldLegs() && isLeg(name)) {
            PartPose saved = part.storePose();
            part.xRot = Mth.lerp(p, part.xRot, LEG_FOLD);
            part.yRot = Mth.lerp(p, part.yRot, 0.0F);
            part.zRot = Mth.lerp(p, part.zRot, 0.0F);
            return saved;
        }
        if ("head_parts".equals(name)) {
            PartPose saved = part.storePose();
            part.xRot = Mth.lerp(p, part.xRot, NECK_DROOP);
            part.yRot = Mth.lerp(p, part.yRot, 0.0F);
            return saved;
        }
        if ("head".equals(name)) {
            PartPose saved = part.storePose();
            part.xRot = Mth.lerp(p, part.xRot, HEAD_DROOP);
            part.yRot = Mth.lerp(p, part.yRot, 0.0F);
            return saved;
        }
        return null;
    }

    public static float ease(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    static boolean isLeg(String name) {
        return name.contains("leg") || name.endsWith("_hand");
    }

    // --- Model analysis ---

    private static ModelInfo info(EntityModel<?> model) {
        return CACHE.computeIfAbsent(model, SleepPose::analyze);
    }

    private static ModelInfo analyze(EntityModel<?> model) {
        Set<ModelPart> parts = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<ModelPart> queue = new ArrayDeque<>(rootParts(model));
        while (!queue.isEmpty()) {
            ModelPart part = queue.poll();
            if (parts.add(part)) {
                queue.addAll(NamedModelPart.of(part).personality$getChildren().values());
            }
        }

        float lowering = 0.0F;
        boolean hasLegs = false;
        for (ModelPart part : parts) {
            String name = NamedModelPart.of(part).personality$getName();
            if (name.contains("haunch")) {
                // Rabbits: haunches are already on the ground, folding the front legs would look broken.
                return ModelInfo.NONE;
            }
            if (!isLeg(name)) {
                continue;
            }
            for (ModelPart.Cube cube : NamedModelPart.of(part).personality$getCubes()) {
                hasLegs = true;
                float length = cube.maxY;
                float halfThickness = (cube.maxZ - cube.minZ) / 2.0F;
                lowering = Math.max(lowering, length - halfThickness);
            }
        }
        return hasLegs ? new ModelInfo(true, Math.max(0.0F, lowering)) : ModelInfo.NONE;
    }

    private static List<ModelPart> rootParts(EntityModel<?> model) {
        List<ModelPart> roots = new ArrayList<>();
        if (model instanceof HierarchicalModel<?> hierarchical) {
            roots.add(hierarchical.root());
        }
        // Older-style models keep their parts in fields; the field types tell us which ones they are.
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType() != ModelPart.class) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
                        roots.add(part);
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                    // Not accessible on this platform; skip this field.
                }
            }
        }
        return roots;
    }

    private record ModelInfo(boolean canFold, float lowering) {
        static final ModelInfo NONE = new ModelInfo(false, 0.0F);
    }
}
