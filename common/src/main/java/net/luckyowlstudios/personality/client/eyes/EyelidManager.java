package net.luckyowlstudios.personality.client.eyes;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.luckyowlstudios.personality.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads eyelid definitions and builds an eyelid overlay texture for each mob texture on demand.
 */
public final class EyelidManager {

    private static final String DIRECTORY = "personality/eyelids";
    private static final ResourceLocation NONE = Constants.id("none");

    private static final Map<EntityType<?>, EyelidDefinition> DEFINITIONS = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> OVERLAYS = new HashMap<>();

    private EyelidManager() {
    }

    /** Reloads definitions. Registered as a client resource reload listener by each loader. */
    public static void reload(ResourceManager resourceManager) {
        releaseOverlays();
        DEFINITIONS.clear();
        resourceManager.listResources(DIRECTORY, location -> location.getPath().endsWith(".json")).forEach((file, resource) -> {
            String path = file.getPath();
            ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath(file.getNamespace(),
                    path.substring(DIRECTORY.length() + 1, path.length() - ".json".length()));
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
            if (type.isEmpty()) {
                return; // Definition for a mob from a mod that isn't installed.
            }
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                DEFINITIONS.put(type.get(), EyelidDefinition.fromJson(json));
            } catch (Exception e) {
                Constants.LOG.error("Couldn't load eyelid definition {}", file, e);
            }
        });
        Constants.LOG.info("Loaded {} eyelid definitions", DEFINITIONS.size());
    }

    /** The eyelid overlay for a mob texture, or null if this mob has no eyelids defined. */
    @Nullable
    public static ResourceLocation getEyelidTexture(EntityType<?> type, ResourceLocation texture) {
        EyelidDefinition definition = DEFINITIONS.get(type);
        if (definition == null) {
            return null;
        }
        ResourceLocation overlay = OVERLAYS.computeIfAbsent(texture, base -> build(base, definition));
        return overlay == NONE ? null : overlay;
    }

    private static ResourceLocation build(ResourceLocation base, EyelidDefinition definition) {
        Minecraft minecraft = Minecraft.getInstance();
        Optional<Resource> resource = minecraft.getResourceManager().getResource(base);
        if (resource.isEmpty()) {
            return NONE; // Generated at runtime (e.g. downloaded skins); nothing to sample.
        }
        try (InputStream stream = resource.get().open(); NativeImage source = NativeImage.read(stream)) {
            NativeImage overlay = paintEyelids(source, definition);
            ResourceLocation location = Constants.id("eyelids/" + base.getNamespace() + "/" + base.getPath());
            minecraft.getTextureManager().register(location, new DynamicTexture(overlay));
            return location;
        } catch (Exception e) {
            Constants.LOG.error("Couldn't build eyelids for {}", base, e);
            return NONE;
        }
    }

    static NativeImage paintEyelids(NativeImage source, EyelidDefinition definition) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scaleX = (float) width / definition.textureWidth();
        float scaleY = (float) height / definition.textureHeight();
        NativeImage overlay = new NativeImage(width, height, true);

        for (EyelidDefinition.Eye eye : definition.eyes()) {
            int x0 = Math.round(eye.x() * scaleX);
            int y0 = Math.round(eye.y() * scaleY);
            int x1 = Math.min(width, Math.round((eye.x() + eye.width()) * scaleX));
            int y1 = Math.min(height, Math.round((eye.y() + eye.height()) * scaleY));
            int lashRows = Math.max(1, Math.round(scaleY));
            for (int x = x0; x < x1; x++) {
                int skin = sampleSkin(source, x, x0, x1, y0);
                int lash = shade(skin, definition.lashShade());
                for (int y = y0; y < y1; y++) {
                    overlay.setPixelRGBA(x, y, y >= y1 - lashRows ? lash : skin);
                }
            }
        }
        return overlay;
    }

    /** Skin colour just above the eye; falls back to the pixels beside it. Always fully opaque. */
    private static int sampleSkin(NativeImage image, int x, int x0, int x1, int y0) {
        int[][] candidates = {{x, y0 - 1}, {x0 - 1, y0}, {x1, y0}, {x, y0 - 2}};
        for (int[] c : candidates) {
            if (c[0] >= 0 && c[1] >= 0 && c[0] < image.getWidth() && c[1] < image.getHeight()) {
                int pixel = image.getPixelRGBA(c[0], c[1]);
                if ((pixel >>> 24) != 0) {
                    return pixel | 0xFF000000;
                }
            }
        }
        return 0xFF000000;
    }

    /** Darkens a colour. Channel order doesn't matter since all channels scale equally. */
    private static int shade(int color, float factor) {
        int c0 = (int) ((color & 0xFF) * factor);
        int c1 = (int) (((color >> 8) & 0xFF) * factor);
        int c2 = (int) (((color >> 16) & 0xFF) * factor);
        return 0xFF000000 | (c2 << 16) | (c1 << 8) | c0;
    }

    private static void releaseOverlays() {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation overlay : OVERLAYS.values()) {
            if (overlay != NONE) {
                textures.release(overlay);
            }
        }
        OVERLAYS.clear();
    }
}
