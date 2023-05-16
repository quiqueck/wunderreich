package de.ambertation.wunderreich.config;

import de.ambertation.wunderlib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.resources.ResourceLocation;

public class RecipeConfig extends DynamicConfig<ResourceLocation> {
    public RecipeConfig() {
        super(Wunderreich.VERSION_PROVIDER, "recipes");
    }
}
