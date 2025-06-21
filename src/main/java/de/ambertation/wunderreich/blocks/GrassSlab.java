package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.loot.LootTableHelper;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class GrassSlab extends SpreadableSnowyDirtSlab {
    public GrassSlab(Block baseBlock, ResourceKey<Block> key) {
        super(baseBlock, key);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.SLABS);
        itemTags.accept(ItemTags.SLABS);

        blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public void buildLootTable(LootTableHelper.BlockLootProvider provider) {
        provider.dropSlabWhenSilkTouch(this, WunderreichSlabBlocks.DIRT_SLAB);
    }
}
