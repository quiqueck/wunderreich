package de.ambertation.wunderreich;

import de.ambertation.lib.general.Logger;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.network.ServerBoundPacketHandler;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.StonecutterJsonBuilder;
import de.ambertation.wunderreich.registries.*;
import de.ambertation.wunderreich.utils.Version;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public class Wunderreich implements ModInitializer {
    public static final String MOD_ID = "wunderreich";
    public static final Logger LOGGER = new Logger();
    public static Version VERSION = new Version("0.0.0");

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(Wunderreich.MOD_ID);
        if (optional.isPresent()) {
            ModContainer modContainer = optional.get();
            VERSION = new Version(modContainer.getMetadata().getVersion().toString());
        }

        SDF.ensureStaticallyLoaded();

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
