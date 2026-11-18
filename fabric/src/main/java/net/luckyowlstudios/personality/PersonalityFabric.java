package net.luckyowlstudios.personality;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckyowlstudios.personality.trait.TraitCommands;

public class PersonalityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalityCommon.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TraitCommands.register(dispatcher));
    }
}
