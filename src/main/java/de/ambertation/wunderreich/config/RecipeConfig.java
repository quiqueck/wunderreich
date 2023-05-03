package de.ambertation.wunderreich.config;

import org.wunder.lib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.resources.ResourceLocation;

public class RecipeConfig extends DynamicConfig<ResourceLocation> {
    public RecipeConfig() {
        super(Wunderreich.VERSION_PROVIDER, "recipes");
    }
}
