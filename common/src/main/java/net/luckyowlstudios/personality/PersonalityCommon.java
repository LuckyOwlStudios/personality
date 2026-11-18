package net.luckyowlstudios.personality;

/**
 * Shared entry point. Called by each loader during mod construction.
 */
public final class PersonalityCommon {

    private PersonalityCommon() {
    }

    public static void init() {
        Constants.LOG.info("Personality is giving wildlife a bedtime.");
    }
}
