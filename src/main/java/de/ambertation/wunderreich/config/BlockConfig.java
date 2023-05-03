package de.ambertation.wunderreich.config;

import org.wunder.lib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockConfig extends DynamicConfig<Block> {
    public BlockConfig() {
        super(Wunderreich.VERSION_PROVIDER, "blocks");
    }

    public boolean isEnabled(Block block) {
        if (block == null) return false;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null;
    }
}
