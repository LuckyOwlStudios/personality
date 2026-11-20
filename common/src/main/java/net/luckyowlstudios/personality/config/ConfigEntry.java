package net.luckyowlstudios.personality.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * One option in {@code config/personality.json}. Read the current value with {@link #get()}.
 */
public abstract class ConfigEntry<T> {

    private final String section;
    private final String key;
    private final String comment;
    private final T defaultValue;
    private volatile T value;

    ConfigEntry(String section, String key, String comment, T defaultValue) {
        this.section = section;
        this.key = key;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public T get() {
        return this.value;
    }

    public String section() {
        return this.section;
    }

    public String key() {
        return this.key;
    }

    public String comment() {
        return this.comment;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    void reset() {
        this.value = this.defaultValue;
    }

    /** Reads the value from JSON. Returns false (and keeps the default) if it is missing or the wrong type. */
    boolean read(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            this.value = this.defaultValue;
            return false;
        }
        try {
            T parsed = this.parse(json.getAsJsonPrimitive());
            if (parsed == null) {
                this.value = this.defaultValue;
                return false;
            }
            this.value = parsed;
            return true;
        } catch (RuntimeException e) {
            this.value = this.defaultValue;
            return false;
        }
    }

    abstract T parse(JsonPrimitive json);

    /** Extra text for the comment, such as the allowed range. */
    String rangeHint() {
        return "";
    }

    static final class Bool extends ConfigEntry<Boolean> {

        Bool(String section, String key, String comment, boolean defaultValue) {
            super(section, key, comment, defaultValue);
        }

        @Override
        Boolean parse(JsonPrimitive json) {
            return json.isBoolean() ? json.getAsBoolean() : null;
        }
    }

    static final class Int extends ConfigEntry<Integer> {

        private final int min;
        private final int max;

        Int(String section, String key, String comment, int defaultValue, int min, int max) {
            super(section, key, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        Integer parse(JsonPrimitive json) {
            if (!json.isNumber()) {
                return null;
            }
            return Math.clamp(Math.round(json.getAsDouble()), this.min, this.max);
        }

        @Override
        String rangeHint() {
            return "Range: " + this.min + " to " + this.max + ". Default: " + this.defaultValue() + ".";
        }
    }

    static final class Decimal extends ConfigEntry<Double> {

        private final double min;
        private final double max;

        Decimal(String section, String key, String comment, double defaultValue, double min, double max) {
            super(section, key, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        Double parse(JsonPrimitive json) {
            if (!json.isNumber()) {
                return null;
            }
            double v = json.getAsDouble();
            return Double.isFinite(v) ? Math.clamp(v, this.min, this.max) : null;
        }

        @Override
        String rangeHint() {
            return "Range: " + format(this.min) + " to " + format(this.max) + ". Default: " + format(this.defaultValue()) + ".";
        }

        static String format(double v) {
            return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
        }
    }
}
