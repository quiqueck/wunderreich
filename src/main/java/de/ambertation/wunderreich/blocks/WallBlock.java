package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WallBlock extends AbstractWallBlock implements BlockTagSupplier {

    public WallBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        super.supplyTags(blockTags, itemTags);
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
    }
}
