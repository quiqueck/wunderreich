package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.function.Consumer;

public class DirtSlabBlock extends SlabBlock implements BlockTagSupplier, CanDropLoot {
    public DirtSlabBlock(Block baseBlock) {
        super(FabricBlockSettings.copyOf(baseBlock));
    }

    @Override
    public void supplyTags(Consumer<Tag<Block>> blockTags, Consumer<Tag<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
        blockTags.accept(BlockTags.DIRT);
    }
}
