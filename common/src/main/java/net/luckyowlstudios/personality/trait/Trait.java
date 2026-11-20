package net.luckyowlstudios.personality.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * A personality trait an individual animal can be born with.
 * <p>
 * Traits are plain data: each one describes how it nudges the animal's behaviour. Systems such as the sleep
 * schedule read the combined effect of all of an animal's traits through {@link TraitEffects}. To add a trait,
 * register it in {@link Traits} with a builder, and add any new kind of effect here as a builder option.
 */
public final class Trait {

    private final ResourceLocation id;
    private final float chance;
    private final int bedtimeShift;
    private final int wakeShift;
    private final double speedModifier;
    private final Set<ResourceLocation> incompatible;

    private Trait(Builder builder) {
        this.id = builder.id;
        this.chance = builder.chance;
        this.bedtimeShift = builder.bedtimeShift;
        this.wakeShift = builder.wakeShift;
        this.speedModifier = builder.speedModifier;
        this.incompatible = Set.copyOf(builder.incompatible);
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation id() {
        return this.id;
    }

    /** Chance (0..1) that a newly met animal is born with this trait. */
    public float chance() {
        return this.chance;
    }

    /** Ticks added to the animal's bedtime. Positive = stays up later, negative = turns in earlier. */
    public int bedtimeShift() {
        return this.bedtimeShift;
    }

    /** Ticks added to the animal's wake-up time. Positive = sleeps in. */
    public int wakeShift() {
        return this.wakeShift;
    }

    /** Movement speed change, as a fraction of total speed. -0.2 = 20% slower. */
    public double speedModifier() {
        return this.speedModifier;
    }

    public boolean isCompatibleWith(Trait other) {
        return !this.incompatible.contains(other.id) && !other.incompatible.contains(this.id);
    }

    public String translationKey() {
        return "trait." + this.id.getNamespace() + "." + this.id.getPath();
    }

    public Component displayName() {
        return Component.translatable(this.translationKey());
    }

    public Component description() {
        return Component.translatable(this.translationKey() + ".desc");
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    public static final class Builder {

        private final ResourceLocation id;
        private float chance = 0.1F;
        private int bedtimeShift;
        private int wakeShift;
        private double speedModifier;
        private final Set<ResourceLocation> incompatible = new HashSet<>();

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder chance(float chance) {
            this.chance = chance;
            return this;
        }

        public Builder bedtimeShift(int ticks) {
            this.bedtimeShift = ticks;
            return this;
        }

        public Builder wakeShift(int ticks) {
            this.wakeShift = ticks;
            return this;
        }

        public Builder speedModifier(double fraction) {
            this.speedModifier = fraction;
            return this;
        }

        /** Traits that never appear together with this one. Only needs declaring on one side. */
        public Builder incompatibleWith(ResourceLocation... others) {
            this.incompatible.addAll(Set.of(others));
            return this;
        }

        public Trait build() {
            return new Trait(this);
        }
    }
}
