package net.luckyowlstudios.personality;

import net.fabricmc.api.ModInitializer;

public class PersonalityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalityCommon.init();
    }
}
