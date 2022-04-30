package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class DirtPathSlabBlock extends DirtSlabBlock {
    protected static final VoxelShape BOTTOM_AABB;
    protected static final VoxelShape TOP_AABB;
    protected static final VoxelShape DOUBLE_AABB;

    static {
        BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
        TOP_AABB = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 15.0D, 16.0D);
        DOUBLE_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    }

    public DirtPathSlabBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState blockState,
                                  BlockGetter blockGetter,
                                  BlockPos blockPos,
                                  PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random) {
        final BlockState newState = DirtSlabBlock.createStateFrom(WunderreichBlocks.DIRT_SLAB, blockState);
        level.setBlockAndUpdate(blockPos, pushEntitiesUp(blockState, newState, level, blockPos));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos blockPos) {
        if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) return true;

        final BlockState aboveState = levelReader.getBlockState(blockPos.above());
        return !aboveState.getMaterial().isSolid() || aboveState.getBlock() instanceof FenceGateBlock;
    }

    @Override
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        if (direction == Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.scheduleTick(blockPos, this, 1);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public VoxelShape getShape(BlockState blockState,
                               BlockGetter blockGetter,
                               BlockPos blockPos,
                               CollisionContext collisionContext) {
        SlabType slabType = blockState.getValue(TYPE);
        switch (slabType) {
            case DOUBLE:
                return DOUBLE_AABB;
            case TOP:
                return TOP_AABB;
            default:
                return BOTTOM_AABB;
        }
    }
}
