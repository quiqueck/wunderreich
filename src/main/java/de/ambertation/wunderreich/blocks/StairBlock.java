package de.ambertation.wunderreich.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StairBlock extends net.minecraft.world.level.block.StairBlock {
    public StairBlock(Block baseBlock) {
        super(baseBlock.defaultBlockState(), BlockBehaviour.Properties.copy(baseBlock));
    }
}
