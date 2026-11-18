package net.luckyowlstudios.personality;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.luckyowlstudios.personality.client.eyes.EyelidManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class PersonalityFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Constants.id("eyelids");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                EyelidManager.reload(resourceManager);
            }
        });
    }
}
