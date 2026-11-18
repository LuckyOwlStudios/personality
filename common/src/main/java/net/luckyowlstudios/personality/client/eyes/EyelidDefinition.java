package net.luckyowlstudios.personality.client.eyes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a mob's eyes are on its texture. Loaded from
 * {@code assets/<entity namespace>/personality/eyelids/<entity path>.json}, for example
 * {@code assets/minecraft/personality/eyelids/cow.json}:
 *
 * <pre>
 * {
 *   "texture_width": 64,
 *   "texture_height": 32,
 *   "eyes": [[6, 8, 2, 2], [12, 8, 2, 2]]
 * }
 * </pre>
 *
 * Each eye is {@code [x, y, width, height]} in texture pixels. When the mob sleeps, each eye is painted over:
 * every row but the last takes the skin colour from just above the eye, and the last row becomes a darker
 * "lash" line. Because colours are sampled from the actual texture, every variant (cat breeds, horse coats,
 * resource packs) gets matching eyelids from one definition. Coordinates are scaled automatically for
 * higher-resolution textures.
 */
public record EyelidDefinition(int textureWidth, int textureHeight, List<Eye> eyes, float lashShade) {

    public record Eye(int x, int y, int width, int height) {
    }

    public static EyelidDefinition fromJson(JsonObject json) {
        int width = GsonHelper.getAsInt(json, "texture_width");
        int height = GsonHelper.getAsInt(json, "texture_height");
        float lashShade = GsonHelper.getAsFloat(json, "lash_shade", 0.6F);
        List<Eye> eyes = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "eyes")) {
            JsonArray eye = GsonHelper.convertToJsonArray(element, "eye");
            if (eye.size() != 4) {
                throw new IllegalArgumentException("Each eye must be [x, y, width, height]");
            }
            eyes.add(new Eye(eye.get(0).getAsInt(), eye.get(1).getAsInt(), eye.get(2).getAsInt(), eye.get(3).getAsInt()));
        }
        return new EyelidDefinition(width, height, List.copyOf(eyes), lashShade);
    }
}
