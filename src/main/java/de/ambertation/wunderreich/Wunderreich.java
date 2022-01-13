package de.ambertation.wunderreich;

import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.network.ServerBoundPacketHandler;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichReceipes;
import de.ambertation.wunderreich.rei.ImprinterRecipe;
import de.ambertation.wunderreich.utils.Logger;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public class Wunderreich implements ModInitializer {
    public static final String MOD_ID = "wunderreich";
    public static final Logger LOGGER = new Logger();
    public static String VERSION = "0.0.0";

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(Wunderreich.MOD_ID);
        if (optional.isPresent()) {
            ModContainer modContainer = optional.get();
            VERSION = modContainer.getMetadata().getVersion().toString();
        }

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        WunderreichBlockEntities.register();
        WunderreichBlocks.register();
        WunderreichItems.register();
        WunderreichReceipes.register();

        ImprinterRecipe.register();
        ServerBoundPacketHandler.register();

        WunderreichConfigs.saveConfigs();
    }
}
