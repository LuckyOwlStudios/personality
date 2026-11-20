package net.luckyowlstudios.personality.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.luckyowlstudios.personality.Constants;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All of Personality's options, stored in {@code config/personality.json}.
 * <p>
 * The file is plain JSON with {@code //} comments, so it works the same on Fabric and NeoForge without a
 * config library. It is read when the game starts and again each time a world or server starts, and is
 * rewritten afterwards so new options appear with their defaults and bad values are corrected.
 * <p>
 * To add an option: declare it below with {@link #bool}, {@link #integer} or {@link #decimal}, then read it
 * with {@code PersonalityConfig.MY_OPTION.get()}.
 */
public final class PersonalityConfig {

    private static final String FILE_NAME = Constants.MOD_ID + ".json";
    private static final Gson GSON = new Gson();

    private static final Map<String, String> SECTION_COMMENTS = new LinkedHashMap<>();
    private static final List<ConfigEntry<?>> ENTRIES = new ArrayList<>();

    private static Path file;

    // --- Sleep ---

    static {
        SECTION_COMMENTS.put("sleep", "Animals in the personality:sleeps tag fall asleep at night (or by day, if personality:nocturnal).");
    }

    public static final ConfigEntry<Boolean> SLEEP_ENABLED = bool("sleep", "enabled",
            "Whether animals sleep at all. Turning this off wakes every sleeping animal.", true);

    public static final ConfigEntry<Double> STARTLE_RADIUS = decimal("sleep", "startleRadius",
            "A player sprinting within this many blocks wakes sleeping animals. Walk or sneak to let them be. 0 turns this off.",
            4.0, 0.0, 32.0);

    public static final ConfigEntry<Integer> STAY_AWAKE_AFTER_HURT = integer("sleep", "stayAwakeAfterHurtSeconds",
            "Seconds an animal stays awake after it is hurt.", 60, 0, 3600);

    public static final ConfigEntry<Integer> STAY_AWAKE_AFTER_INTERACT = integer("sleep", "stayAwakeAfterInteractSeconds",
            "Seconds an animal stays awake after a player feeds, leashes or otherwise interacts with it.", 10, 0, 3600);

    public static final ConfigEntry<Integer> STAY_AWAKE_AFTER_STARTLED = integer("sleep", "stayAwakeAfterStartledSeconds",
            "Seconds an animal stays awake after a sprinting player startles it.", 20, 0, 3600);

    public static final ConfigEntry<Integer> FRIGHTENED_SECONDS = integer("sleep", "frightenedSeconds",
            "An animal hurt by something this many seconds ago is too frightened to fall asleep.", 30, 0, 3600);

    public static final ConfigEntry<Boolean> PETS_WAIT_FOR_OWNER = bool("sleep", "petsWaitForOwner",
            "Tamed pets that are not sitting stay up until their owner goes to bed or wanders off.", true);

    public static final ConfigEntry<Double> OWNER_AWAY_DISTANCE = decimal("sleep", "ownerAwayDistance",
            "How many blocks away an owner must be before their (non-sitting) pet will sleep without them.",
            24.0, 0.0, 256.0);

    public static final ConfigEntry<Boolean> SILENT_WHILE_SLEEPING = bool("sleep", "silentWhileSleeping",
            "Sleeping animals make no ambient sounds.", true);

    // --- Schedule ---

    static {
        SECTION_COMMENTS.put("schedule", "When animals go to bed and wake up, as hours on a 24-hour clock (18.5 = 18:30). "
                + "Each animal gets its own times, between the value here and that value plus 'spreadHours'.");
    }

    public static final ConfigEntry<Double> DIURNAL_BEDTIME = decimal("schedule", "bedtime",
            "Earliest bedtime for day-active animals.", 18.5, 0.0, 24.0);

    public static final ConfigEntry<Double> DIURNAL_WAKE = decimal("schedule", "wakeTime",
            "Earliest wake-up time for day-active animals.", 4.8, 0.0, 24.0);

    public static final ConfigEntry<Double> NOCTURNAL_BEDTIME = decimal("schedule", "nocturnalBedtime",
            "Earliest bedtime for night-active animals (personality:nocturnal).", 6.5, 0.0, 24.0);

    public static final ConfigEntry<Double> NOCTURNAL_WAKE = decimal("schedule", "nocturnalWakeTime",
            "Earliest wake-up time for night-active animals.", 16.5, 0.0, 24.0);

    public static final ConfigEntry<Double> SPREAD_HOURS = decimal("schedule", "spreadHours",
            "How far each animal's own bedtime and wake-up time can drift past the times above, so a herd drifts off one by one. 0 = all at once.",
            2.4, 0.0, 12.0);

    // --- Herd panic ---

    static {
        SECTION_COMMENTS.put("herdPanic", "Hit a skittish animal and the herd bolts. Which animals are skittish is set by the personality:skittish tag.");
    }

    public static final ConfigEntry<Boolean> HERD_PANIC_ENABLED = bool("herdPanic", "enabled",
            "Whether hurting an animal makes its neighbours run away from you.", true);

    public static final ConfigEntry<Double> ALARM_RADIUS = decimal("herdPanic", "alarmRadius",
            "How many blocks around the hurt animal other animals hear the alarm.", 12.0, 1.0, 48.0);

    public static final ConfigEntry<Integer> SCARED_SECONDS = integer("herdPanic", "scaredSeconds",
            "How long, in seconds, a frightened animal keeps running.", 7, 1, 60);

    // --- Retaliation ---

    static {
        SECTION_COMMENTS.put("retaliation", "Some animals fight back instead of running (cows, mooshrooms and goats by default, see the personality:retaliates tag).");
    }

    public static final ConfigEntry<Boolean> RETALIATION_ENABLED = bool("retaliation", "enabled",
            "Whether animals in the personality:retaliates tag charge and headbutt a player who hurts them. Off = they panic like everyone else.", true);

    public static final ConfigEntry<Integer> ANGRY_SECONDS = integer("retaliation", "angrySeconds",
            "How long, in seconds, an animal stays angry after the last time it was hit.", 10, 1, 120);

    public static final ConfigEntry<Double> RETALIATION_DAMAGE_MULTIPLIER = decimal("retaliation", "damageMultiplier",
            "Scales how hard headbutts hit. 1 = vanilla goat damage and one heart for cows, 0.5 = half as much.", 1.0, 0.1, 10.0);

    // --- Rain ---

    static {
        SECTION_COMMENTS.put("rain", "Rain-shy animals (personality:dislikes_rain tag) look for cover when it rains.");
    }

    public static final ConfigEntry<Boolean> SEEK_SHELTER_IN_RAIN = bool("rain", "seekShelter",
            "Rain-shy animals walk to the nearest cover when it starts to rain.", true);

    public static final ConfigEntry<Boolean> NO_SLEEPING_IN_RAIN = bool("rain", "noSleepingInRain",
            "Rain-shy animals can't fall or stay asleep while rain falls on them.", true);

    public static final ConfigEntry<Integer> SHELTER_SEARCH_RADIUS = integer("rain", "shelterSearchRadius",
            "How far, in blocks, an animal looks around for cover. Smaller is cheaper on big farms.", 12, 2, 32);

    // --- Traits ---

    static {
        SECTION_COMMENTS.put("traits", "Each animal is born with a random set of personality traits. Chances are 0 (never) to 1 (always).");
    }

    public static final ConfigEntry<Boolean> TRAITS_ENABLED = bool("traits", "enabled",
            "Turns every trait off: no new rolls and no effects. Animals keep the traits they already have and get their effects back if you turn this on again.", true);

    public static final ConfigEntry<Double> SILLY_CHANCE = decimal("traits", "sillyChance",
            "Chance an animal is Silly (stays up late, sleeps in).", 0.15, 0.0, 1.0);

    public static final ConfigEntry<Double> SLEEPY_CHANCE = decimal("traits", "sleepyChance",
            "Chance an animal is Sleepy (early to bed, sleeps in, 20% slower). Never together with Silly.", 0.15, 0.0, 1.0);

    public static final ConfigEntry<Double> CURIOUS_CHANCE = decimal("traits", "curiousChance",
            "Chance an animal is Curious (follows nearby players and villagers for a while).", 0.15, 0.0, 1.0);

    // --- Client ---

    static {
        SECTION_COMMENTS.put("client", "How sleeping animals look. Only read on the client.");
    }

    public static final ConfigEntry<Boolean> LIE_DOWN = bool("client", "lieDown",
            "Sleeping animals fold their legs and lie down. Turn off to have them sleep standing up.", true);

    public static final ConfigEntry<Boolean> CLOSED_EYES = bool("client", "closedEyes",
            "Sleeping animals close their eyes.", true);

    private PersonalityConfig() {
    }

    // --- Declaring options ---

    private static <E extends ConfigEntry<?>> E add(E entry) {
        ENTRIES.add(entry);
        return entry;
    }

    static ConfigEntry<Boolean> bool(String section, String key, String comment, boolean defaultValue) {
        return add(new ConfigEntry.Bool(section, key, comment, defaultValue));
    }

    static ConfigEntry<Integer> integer(String section, String key, String comment, int defaultValue, int min, int max) {
        return add(new ConfigEntry.Int(section, key, comment, defaultValue, min, max));
    }

    static ConfigEntry<Double> decimal(String section, String key, String comment, double defaultValue, double min, double max) {
        return add(new ConfigEntry.Decimal(section, key, comment, defaultValue, min, max));
    }

    /** The configured chance for a built-in trait, or null for traits added by other code. */
    public static ConfigEntry<Double> traitChance(ResourceLocation id) {
        if (!id.getNamespace().equals(Constants.MOD_ID)) {
            return null;
        }
        return switch (id.getPath()) {
            case "silly" -> SILLY_CHANCE;
            case "sleepy" -> SLEEPY_CHANCE;
            case "curious" -> CURIOUS_CHANCE;
            default -> null;
        };
    }

    /** Converts a config value in seconds to game ticks. */
    public static int ticks(ConfigEntry<Integer> seconds) {
        return seconds.get() * 20;
    }

    /** Converts a config value in clock hours (0..24) to Minecraft day time ticks (0 = 06:00). */
    public static int dayTime(ConfigEntry<Double> hours) {
        return Math.floorMod((int) Math.round((hours.get() - 6.0) * 1000.0), 24000);
    }

    // --- Loading and saving ---

    /** Sets where the config lives and loads it. Called once by each loader at startup. */
    public static void init(Path configDir) {
        file = configDir.resolve(FILE_NAME);
        load();
    }

    /** Re-reads the file, keeping defaults for anything missing or invalid, then writes it back out. */
    public static synchronized void load() {
        if (file == null) {
            return;
        }
        JsonObject root = new JsonObject();
        boolean exists = Files.exists(file);
        if (exists) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                // JsonParser reads leniently, so // and /* */ comments are allowed.
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                } else {
                    Constants.LOG.warn("{} is not a JSON object; using defaults", file);
                }
            } catch (IOException | RuntimeException e) {
                Constants.LOG.error("Could not read {}; using defaults. Fix or delete the file to change settings.", file, e);
                ENTRIES.forEach(ConfigEntry::reset);
                return; // Don't overwrite a file the player may be in the middle of editing.
            }
        }

        for (ConfigEntry<?> entry : ENTRIES) {
            JsonElement section = root.get(entry.section());
            JsonElement value = section != null && section.isJsonObject() ? section.getAsJsonObject().get(entry.key()) : null;
            if (!entry.read(value) && value != null) {
                Constants.LOG.warn("Invalid value for {}.{} in {}; using default {}", entry.section(), entry.key(), file, entry.defaultValue());
            }
        }
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, write(), StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.writeString(file, write(), StandardCharsets.UTF_8);
            } catch (IOException e2) {
                Constants.LOG.error("Could not write {}", file, e2);
            }
        }
    }

    private static String write() {
        StringBuilder out = new StringBuilder();
        out.append("// Personality config. Lines starting with // are comments.\n");
        out.append("// Changes apply the next time a world or server starts.\n");
        out.append("{\n");
        List<String> sections = new ArrayList<>();
        for (ConfigEntry<?> entry : ENTRIES) {
            if (!sections.contains(entry.section())) {
                sections.add(entry.section());
            }
        }
        for (int s = 0; s < sections.size(); s++) {
            String section = sections.get(s);
            String sectionComment = SECTION_COMMENTS.get(section);
            if (sectionComment != null) {
                out.append("  // ").append(sectionComment).append('\n');
            }
            out.append("  ").append(GSON.toJson(section)).append(": {\n");
            List<ConfigEntry<?>> inSection = ENTRIES.stream().filter(e -> e.section().equals(section)).toList();
            for (int i = 0; i < inSection.size(); i++) {
                ConfigEntry<?> entry = inSection.get(i);
                out.append("    // ").append(entry.comment());
                String hint = entry.rangeHint();
                if (!hint.isEmpty()) {
                    out.append(' ').append(hint);
                }
                out.append('\n');
                out.append("    ").append(GSON.toJson(entry.key())).append(": ").append(format(entry.get()));
                out.append(i < inSection.size() - 1 ? ",\n" : "\n");
            }
            out.append("  }").append(s < sections.size() - 1 ? ",\n\n" : "\n");
        }
        out.append("}\n");
        return out.toString();
    }

    private static String format(Object value) {
        if (value instanceof Double d) {
            return ConfigEntry.Decimal.format(d);
        }
        return String.valueOf(value);
    }
}
