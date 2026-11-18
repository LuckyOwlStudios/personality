package net.luckyowlstudios.personality.trait;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Set;

/**
 * Combines the effects of all of a mob's traits.
 */
public final class TraitEffects {

    private TraitEffects() {
    }

    public static Set<Trait> traits(Mob mob) {
        return mob instanceof TraitHolder holder ? holder.personality$getTraits() : Set.of();
    }

    public static int bedtimeShift(Mob mob) {
        int shift = 0;
        for (Trait trait : traits(mob)) {
            shift += trait.bedtimeShift();
        }
        return shift;
    }

    public static int wakeShift(Mob mob) {
        int shift = 0;
        for (Trait trait : traits(mob)) {
            shift += trait.wakeShift();
        }
        return shift;
    }

    /** Adds or removes each trait's attribute modifiers to match the mob's current traits. */
    public static void applyAttributes(Mob mob) {
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        Set<Trait> traits = traits(mob);
        for (Trait trait : Traits.all()) {
            if (trait.speedModifier() == 0) {
                continue;
            }
            if (traits.contains(trait)) {
                speed.addOrUpdateTransientModifier(new AttributeModifier(trait.id(), trait.speedModifier(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else {
                speed.removeModifier(trait.id());
            }
        }
    }
}
