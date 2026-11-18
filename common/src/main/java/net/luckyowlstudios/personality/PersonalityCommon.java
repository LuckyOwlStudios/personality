package net.luckyowlstudios.personality;

import net.luckyowlstudios.personality.trait.Traits;

/**
 * Shared entry point. Called by each loader during mod construction.
 */
public final class PersonalityCommon {

    private PersonalityCommon() {
    }

    public static void init() {
        Traits.bootstrap();
        Constants.LOG.info("Personality is giving wildlife a bedtime.");
    }
}
