package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class AbstractStairBlock extends net.minecraft.world.level.block.StairBlock implements BlockTagSupplier, CanDropLoot {
    protected AbstractStairBlock(BlockState blockState, BlockBehaviour.Properties properties) {
        super(blockState, properties);
    }

    public AbstractStairBlock(Block baseBlock, ResourceKey<Block> key) {
        super(baseBlock.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(baseBlock).setId(key));
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.STAIRS);
    }
}
