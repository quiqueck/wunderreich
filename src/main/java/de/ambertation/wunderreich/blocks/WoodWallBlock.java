package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WoodWallBlock extends AbstractWallBlock implements BlockTagSupplier {

    public WoodWallBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        super.supplyTags(blockTags, itemTags);
        blockTags.accept(BlockTags.MINEABLE_WITH_AXE);
    }
}
