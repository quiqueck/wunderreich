package de.ambertation.wunderreich.config;

import de.ambertation.wunderlib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.resources.Identifier;

public class RecipeConfig extends DynamicConfig<Identifier> {
    public RecipeConfig() {
        super(Wunderreich.VERSION_PROVIDER, "recipes");
    }
}
