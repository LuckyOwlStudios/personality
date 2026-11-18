package net.luckyowlstudios.personality.trait;

import java.util.Collection;
import java.util.Set;

/**
 * Implemented on every {@link net.minecraft.world.entity.Mob} through a mixin.
 */
public interface TraitHolder {

    /** This animal's traits. Saved with the entity and synced to clients. Empty until the first server tick. */
    Set<Trait> personality$getTraits();

    default boolean personality$hasTrait(Trait trait) {
        return this.personality$getTraits().contains(trait);
    }

    /** Server only. Replaces the animal's traits. */
    void personality$setTraits(Collection<Trait> traits);
}
