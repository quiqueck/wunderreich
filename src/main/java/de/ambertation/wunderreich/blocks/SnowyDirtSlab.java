package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SnowyDirtSlab extends DirtSlabBlock implements ChangeRenderLayer {
    public static final BooleanProperty SNOWY = SnowyDirtBlock.SNOWY;

    public SnowyDirtSlab(Block baseBlock) {
        super(baseBlock);

        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(TYPE, SlabType.BOTTOM)
                                      .setValue(WATERLOGGED, false)
                                      .setValue(SNOWY, false));
    }


    @Override
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        blockState = super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        if (direction == Direction.UP) {
            return blockState.setValue(SNOWY, isSnowySetting(blockState2));
        }

        return blockState;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
        return super.getStateForPlacement(blockPlaceContext).setValue(SNOWY, isSnowySetting(blockState));
    }

    private static boolean isSnowySetting(BlockState blockState) {
        return blockState.is(BlockTags.SNOW);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, SNOWY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public RenderType getRenderType() {
        return RenderType.cutout();
    }
}
