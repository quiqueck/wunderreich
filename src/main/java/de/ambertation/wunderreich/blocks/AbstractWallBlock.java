package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

public class AbstractWallBlock extends net.minecraft.world.level.block.WallBlock implements BlockTagSupplier, CanDropLoot {
    protected AbstractWallBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public AbstractWallBlock(Block baseBlock, ResourceKey<Block> key) {
        this(BlockBehaviour.Properties.ofFullCopy(baseBlock).setId(key));
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.WALLS);
    }
}
