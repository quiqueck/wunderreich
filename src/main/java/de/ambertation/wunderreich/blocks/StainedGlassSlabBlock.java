package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;


public class StainedGlassSlabBlock extends DirtSlabBlock implements BlockTagSupplier, CanDropLoot, ChangeRenderLayer {
    public StainedGlassSlabBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<Tag<Block>> blockTags, Consumer<Tag<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
        blockTags.accept(BlockTags.IMPERMEABLE);
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.translucent();
    }
}

