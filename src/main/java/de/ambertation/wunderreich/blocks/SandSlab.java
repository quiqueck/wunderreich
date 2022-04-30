package de.ambertation.wunderreich.blocks;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class SandSlab extends FallingSlab {
    public SandSlab(Block baseBlock) {
        this(0xDBD3A0, baseBlock);
    }

    protected SandSlab(int dustColor, Block baseBlock) {
        super(dustColor, baseBlock);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.SLABS);
        itemTags.accept(ItemTags.SLABS);

        blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
        blockTags.accept(BlockTags.SAND);
        itemTags.accept(ItemTags.SAND);
    }

    public static class Red extends SandSlab {
        public Red(Block baseBlock) {
            super(0xA95821, baseBlock);
        }
    }
}
