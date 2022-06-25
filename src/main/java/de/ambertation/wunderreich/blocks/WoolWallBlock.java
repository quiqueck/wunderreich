package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.registries.WunderreichTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WoolWallBlock extends AbstractWallBlock {
    public WoolWallBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        super.supplyTags(blockTags, itemTags);
        blockTags.accept(BlockTags.WOOL);
        blockTags.accept(WunderreichTags.MINEABLE_SHEARS);
    }
}
