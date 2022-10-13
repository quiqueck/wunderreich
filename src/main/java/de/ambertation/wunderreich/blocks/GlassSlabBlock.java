package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class GlassSlabBlock extends DirtSlabBlock implements BlockTagSupplier, CanDropLoot, ChangeRenderLayer {
    public GlassSlabBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
        blockTags.accept(BlockTags.IMPERMEABLE);
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.translucent();
    }

    //see Mojang: HalfTransparentBlock
    @Override
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.is(this)) {
            if (blockState.getValues().get(TYPE) == blockState2.getValue(TYPE)) {
                return true;
            }
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    //see Mojang: AbstractGlassBlock
//    @Override
//    public VoxelShape getVisualShape(BlockState blockState,
//                                     BlockGetter blockGetter,
//                                     BlockPos blockPos,
//                                     CollisionContext collisionContext) {
//        return Shapes.empty();
//    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0f;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean needsSilkTouch() {
        return true;
    }
}
