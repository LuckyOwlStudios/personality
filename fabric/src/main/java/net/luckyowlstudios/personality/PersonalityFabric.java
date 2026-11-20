package net.luckyowlstudios.personality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckyowlstudios.personality.trait.TraitCommands;

public class PersonalityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalityCommon.init(FabricLoader.getInstance().getConfigDir());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> PersonalityCommon.onServerStarting());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TraitCommands.register(dispatcher));
    }
}
