package de.ambertation.wunderreich.config;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockConfig extends DynamicConfig<Block> {
    public BlockConfig() {
        super("blocks");
    }

    public boolean isEnabled(Block block) {
        ResourceLocation id = Registry.BLOCK.getKey(block);
        return id != null;
    }
}
