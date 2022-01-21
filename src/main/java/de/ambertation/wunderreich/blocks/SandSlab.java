package de.ambertation.wunderreich.blocks;

import net.minecraft.world.level.block.Block;

public class SandSlab extends FallingSlab {
    public static class Red extends SandSlab {
        public Red(Block baseBlock) {
            super(0xA95821, baseBlock);
        }
    }

    public SandSlab(Block baseBlock) {
        this(0xDBD3A0, baseBlock);
    }

    protected SandSlab(int dustColor, Block baseBlock) {
        super(dustColor, baseBlock);
    }
}
