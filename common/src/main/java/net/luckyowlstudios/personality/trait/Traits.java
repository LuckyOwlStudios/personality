package net.luckyowlstudios.personality.trait;

import net.luckyowlstudios.personality.Constants;
import net.luckyowlstudios.personality.config.ConfigEntry;
import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every trait the mod knows about.
 */
public final class Traits {

    private static final Map<ResourceLocation, Trait> REGISTRY = new LinkedHashMap<>();

    /** Stays up past bedtime and sleeps in the next morning. */
    public static final Trait SILLY = register(Trait.builder(Constants.id("silly"))
            .chance(0.15F)
            .bedtimeShift(1800)
            .wakeShift(1500)
            .incompatibleWith(Constants.id("sleepy"))
            .build());

    /** Turns in early, sleeps in, and dawdles around during the day. */
    public static final Trait SLEEPY = register(Trait.builder(Constants.id("sleepy"))
            .chance(0.15F)
            .bedtimeShift(-1500)
            .wakeShift(1500)
            .speedModifier(-0.2)
            .build());

    /** Now and then wanders over to a nearby player or villager and tags along for a while. */
    public static final Trait CURIOUS = register(Trait.builder(Constants.id("curious"))
            .chance(0.15F)
            .build());

    private Traits() {
    }

    /** Registers a trait. Call during mod construction, before any world loads. */
    public static Trait register(Trait trait) {
        if (REGISTRY.putIfAbsent(trait.id(), trait) != null) {
            throw new IllegalStateException("Duplicate personality trait: " + trait.id());
        }
        return trait;
    }

    public static Trait get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Collection<Trait> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /** Rolls a fresh set of traits for a newly met animal. Each trait is rolled on its own, in a random order. */
    public static Set<Trait> roll(RandomSource random) {
        List<Trait> candidates = new ArrayList<>(REGISTRY.values());
        shuffle(candidates, random);
        Set<Trait> picked = new LinkedHashSet<>();
        for (Trait trait : candidates) {
            if (random.nextFloat() < chanceOf(trait) && picked.stream().allMatch(trait::isCompatibleWith)) {
                picked.add(trait);
            }
        }
        return picked;
    }

    /** The chance from the config for built-in traits, else the trait's own default. */
    private static float chanceOf(Trait trait) {
        ConfigEntry<Double> configured = PersonalityConfig.traitChance(trait.id());
        return configured != null ? configured.get().floatValue() : trait.chance();
    }

    /** Loads the class so the built-in traits register early. */
    public static void bootstrap() {
    }

    private static <T> void shuffle(List<T> list, RandomSource random) {
        for (int i = list.size() - 1; i > 0; i--) {
            Collections.swap(list, i, random.nextInt(i + 1));
        }
    }
}
