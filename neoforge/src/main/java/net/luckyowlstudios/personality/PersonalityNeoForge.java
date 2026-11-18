package net.luckyowlstudios.personality;

import net.luckyowlstudios.personality.client.eyes.EyelidManager;
import net.luckyowlstudios.personality.trait.TraitCommands;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Constants.MOD_ID)
public class PersonalityNeoForge {

    public PersonalityNeoForge(IEventBus modBus, Dist dist) {
        PersonalityCommon.init();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> TraitCommands.register(event.getDispatcher()));
        if (dist.isClient()) {
            modBus.addListener(PersonalityNeoForge::registerClientReloadListeners);
        }
    }

    private static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) EyelidManager::reload);
    }
}
