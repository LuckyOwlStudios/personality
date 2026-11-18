package net.luckyowlstudios.personality.client.sleep;

import net.minecraft.client.model.geom.ModelPart;

import java.util.List;
import java.util.Map;

/**
 * Implemented on every {@link ModelPart} through a mixin. Gives each part the name it was given in its
 * model definition (for example "head" or "left_hind_leg") so the sleeping pose can find legs and heads on any model.
 */
public interface NamedModelPart {

    String personality$getName();

    void personality$setName(String name);

    Map<String, ModelPart> personality$getChildren();

    List<ModelPart.Cube> personality$getCubes();

    static NamedModelPart of(ModelPart part) {
        return (NamedModelPart) (Object) part;
    }
}
