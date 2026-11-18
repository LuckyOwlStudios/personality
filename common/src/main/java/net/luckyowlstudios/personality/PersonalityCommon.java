package net.luckyowlstudios.personality;

import net.luckyowlstudios.personality.config.PersonalityConfig;

import java.nio.file.Path;

/**
 * Shared entry point. Called by each loader during mod construction.
 */
public final class PersonalityCommon {

    private PersonalityCommon() {
    }

    public static void init(Path configDir) {
        PersonalityConfig.init(configDir);
        Constants.LOG.info("Personality is giving wildlife a bedtime.");
    }

    /** Called by each loader when a world or server starts, so config edits apply without restarting the game. */
    public static void onServerStarting() {
        PersonalityConfig.load();
    }
}
