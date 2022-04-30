package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.function.Consumer;

public class DirtSlabBlock extends SlabBlock implements BlockTagSupplier, CanDropLoot {
    public DirtSlabBlock(Block baseBlock) {
        super(FabricBlockSettings.copyOf(baseBlock));
    }

    public static BlockState createStateFrom(Block baseBlock, BlockState currentState) {
        SlabType type = SlabType.BOTTOM;
        boolean waterlogged = false;
        if (currentState.hasProperty(SlabBlock.TYPE)) {
            type = currentState.getValue(SlabBlock.TYPE);
        }
        if (currentState.hasProperty(SlabBlock.WATERLOGGED)) {
            waterlogged = currentState.getValue(SlabBlock.WATERLOGGED);
        }

        return baseBlock
                .defaultBlockState()
                .setValue(SlabBlock.TYPE, type)
                .setValue(SlabBlock.WATERLOGGED, waterlogged);
    }

    @Override
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        blockTags.accept(BlockTags.SLABS);
        itemTags.accept(ItemTags.SLABS);
        blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
        blockTags.accept(BlockTags.DIRT);
        itemTags.accept(ItemTags.DIRT);
    }
}
