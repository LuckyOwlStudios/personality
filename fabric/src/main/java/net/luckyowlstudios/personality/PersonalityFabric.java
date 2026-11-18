package net.luckyowlstudios.personality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public class PersonalityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalityCommon.init(FabricLoader.getInstance().getConfigDir());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> PersonalityCommon.onServerStarting());
    }
}
