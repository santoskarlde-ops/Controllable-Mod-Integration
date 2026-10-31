package dev.taczcompat.controllable;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/** Client-only: enables the Config button in the Mods list. */
final class ConfigScreenRegistrar {

    static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new CompatConfigScreen(parent)));
    }

    private ConfigScreenRegistrar() {}
}
