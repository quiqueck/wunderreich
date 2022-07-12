package de.ambertation.wunderreich;

import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.network.ServerBoundPacketHandler;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.StonecutterJsonBuilder;
import de.ambertation.wunderreich.registries.*;
import de.ambertation.wunderreich.utils.Logger;
import de.ambertation.wunderreich.utils.Version;

public class Wunderreich implements ModInitializer {
    public static final String MOD_ID = "wunderreich";
    public static final Logger LOGGER = new Logger();
    public static Version VERSION = new Version("0.0.0");

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize(ModContainer modContainer) {
        VERSION = new Version(modContainer.metadata().version().toString());


        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        WunderreichBlockEntities.register();
        WunderreichBlocks.register();
        WunderreichItems.register();
        WunderreichPoi.register();
        WunderreichRecipes.register();
        WunderreichAdvancements.register();
        WunderreichRules.register();
        WunderreichMenuTypes.ensureStaticallyLoaded();

        ImprinterRecipe.register();
        ServerBoundPacketHandler.register();

        Configs.saveConfigs();

        RecipeJsonBuilder.invalidate();
        StonecutterJsonBuilder.invalidate();
        AdvancementsJsonBuilder.invalidate();
    }
}
