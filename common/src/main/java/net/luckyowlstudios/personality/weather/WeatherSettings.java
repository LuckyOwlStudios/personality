package net.luckyowlstudios.personality.weather;

/**
 * Switches for the weather behaviours. Plain fields for now so a config screen or file can set them
 * without touching the behaviour code.
 */
public final class WeatherSettings {

    /** Rain-shy animals walk to the nearest cover (trees, roofs, overhangs) when it starts to rain. */
    public static boolean seekShelterInRain = true;

    /** Rain-shy animals can't fall or stay asleep while rain is falling on them. */
    public static boolean noSleepingInRain = true;

    /** How far, in blocks, an animal looks around for cover. */
    public static int shelterSearchRadius = 12;

    /** Walking speed multiplier when heading for cover. */
    public static double shelterSpeed = 1.2;

    private WeatherSettings() {
    }
}
