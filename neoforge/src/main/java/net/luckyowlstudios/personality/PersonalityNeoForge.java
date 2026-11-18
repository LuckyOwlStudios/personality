package net.luckyowlstudios.personality;

import net.luckyowlstudios.personality.client.eyes.EyelidManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(Constants.MOD_ID)
public class PersonalityNeoForge {

    public PersonalityNeoForge(IEventBus modBus, Dist dist) {
        PersonalityCommon.init();
        if (dist.isClient()) {
            modBus.addListener(PersonalityNeoForge::registerClientReloadListeners);
        }
    }

    private static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) EyelidManager::reload);
    }
}
