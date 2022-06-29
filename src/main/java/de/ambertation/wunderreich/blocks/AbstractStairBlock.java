package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class AbstractStairBlock extends net.minecraft.world.level.block.StairBlock implements BlockTagSupplier, CanDropLoot {
    public AbstractStairBlock(Block baseBlock) {
        super(baseBlock.defaultBlockState(), BlockBehaviour.Properties.copy(baseBlock));
    }

    @Override
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        blockTags.accept(BlockTags.STAIRS);
    }
}
