package de.ambertation.wunderreich.blocks;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;

public class StainedGlassSlabBlock extends GlassSlabBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassSlabBlock(DyeColor color, Block baseBlock) {
        super(baseBlock);
        this.color = color;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }


}

