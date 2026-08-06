package de.ambertation.wunderreich.blocks;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class SandSlab extends FallingSlab {
    public SandSlab(Block baseBlock, ResourceKey<Block> key) {
        this(0xDBD3A0, baseBlock, key);
    }

    protected SandSlab(int dustColor, Block baseBlock, ResourceKey<Block> key) {
        super(dustColor, baseBlock, key);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.SLABS);

        blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
        blockTags.accept(BlockTags.SAND);
        itemTags.accept(ItemTags.SAND);
    }

    public static class Red extends SandSlab {
        public Red(Block baseBlock, ResourceKey<Block> key) {
            super(0xA95821, baseBlock, key);
        }
    }
}
