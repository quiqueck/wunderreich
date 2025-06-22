package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class WallBlock extends AbstractWallBlock implements BlockTagSupplier {
    protected WallBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public WallBlock(Block baseBlock, ResourceKey<Block> key) {
        super(baseBlock, key);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        super.supplyTags(blockTags, itemTags);
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
    }
}
