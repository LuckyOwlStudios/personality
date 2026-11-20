package net.luckyowlstudios.personality.sleep;

import net.luckyowlstudios.personality.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class PersonalityTags {

    /** Mobs that follow a sleep schedule. Datapacks can add or remove mobs here. */
    public static final TagKey<EntityType<?>> SLEEPS = TagKey.create(Registries.ENTITY_TYPE, Constants.id("sleeps"));

    /** Sleepers in this tag sleep through the day and are awake at night. */
    public static final TagKey<EntityType<?>> NOCTURNAL = TagKey.create(Registries.ENTITY_TYPE, Constants.id("nocturnal"));

    /** Sleepers in this tag doze standing up (like horses) instead of lying down. */
    public static final TagKey<EntityType<?>> SLEEPS_STANDING = TagKey.create(Registries.ENTITY_TYPE, Constants.id("sleeps_standing"));

    /** Wild animals that panic and run from a player who hurts one of them or a neighbour. */
    public static final TagKey<EntityType<?>> SKITTISH = TagKey.create(Registries.ENTITY_TYPE, Constants.id("skittish"));

    /** Animals that fight back against a player who hurts them, instead of running away. */
    public static final TagKey<EntityType<?>> RETALIATES = TagKey.create(Registries.ENTITY_TYPE, Constants.id("retaliates"));

    /** Mobs that are born with personality traits. Defaults to every sleeper. */
    public static final TagKey<EntityType<?>> HAS_TRAITS = TagKey.create(Registries.ENTITY_TYPE, Constants.id("has_traits"));

    private PersonalityTags() {
    }
}
