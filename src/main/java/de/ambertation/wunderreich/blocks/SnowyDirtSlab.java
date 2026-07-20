package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SnowyDirtSlab extends DirtSlabBlock implements ChangeRenderLayer {
    public static final BooleanProperty SNOWY = SnowyDirtBlock.SNOWY;

    public SnowyDirtSlab(Block baseBlock, ResourceKey<Block> key) {
        super(baseBlock, key);

        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(TYPE, SlabType.BOTTOM)
                                      .setValue(WATERLOGGED, false)
                                      .setValue(SNOWY, false));
    }

    private static boolean isSnowySetting(BlockState blockState) {
        return blockState.is(BlockTags.SNOW);
    }

    @Override
    protected BlockState updateShape(
            BlockState blockState,
            LevelReader levelReader,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos blockPos,
            Direction direction,
            BlockPos blockPos2,
            BlockState blockState2,
            RandomSource randomSource
    ) {

        blockState = super.updateShape(
                blockState,
                levelReader,
                scheduledTickAccess,
                blockPos,
                direction,
                blockPos2,
                blockState2,
                randomSource
        );
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, SNOWY);
    }

    @Override
    public ChangeRenderLayer.RenderLayer getRenderType() {
        return ChangeRenderLayer.RenderLayer.CUTOUT;
    }
}
