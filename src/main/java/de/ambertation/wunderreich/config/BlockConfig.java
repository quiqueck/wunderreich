package de.ambertation.wunderreich.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockConfig extends DynamicConfig<Block> {
    public BlockConfig() {
        super("blocks");
    }

    public boolean isEnabled(Block block) {
        if (block == null) return false;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null;
    }
}
