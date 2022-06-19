package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.registries.WunderreichTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WoolSlabBlock extends ConcreteSlabBlock {
    public WoolSlabBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        blockTags.accept(BlockTags.WOOL);
        blockTags.accept(WunderreichTags.MINEABLE_SHEARS);
    }
}
