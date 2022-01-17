package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;

import java.util.Random;

public class SpreadableSnowyDirtSlab extends SnowyDirtSlab {
    public SpreadableSnowyDirtSlab(Block baseBlock) {
        super(baseBlock);
    }

    private static boolean canBeGrass(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        if (blockState2.is(Blocks.SNOW) && blockState2.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (blockState2.getFluidState().getAmount() == 8) {
            return false;
        }
        int i = LayerLightEngine.getLightBlockInto(levelReader,
                                                   blockState,
                                                   blockPos,
                                                   blockState2,
                                                   blockPos2,
                                                   Direction.UP,
                                                   blockState2.getLightBlock(levelReader, blockPos2));
        return i < levelReader.getMaxLightLevel();
    }

    private static boolean canPropagate(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        return canBeGrass(blockState, levelReader, blockPos) && !levelReader
                .getFluidState(blockPos2)
                .is(FluidTags.WATER);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random) {
        if (!canBeGrass(blockState, level, blockPos)) {
            final BlockState testState = level.getBlockState(blockPos);
            level.setBlockAndUpdate(
                    blockPos,
                    WunderreichBlocks.DIRT_SLAB
                            .defaultBlockState()
                            .setValue(WATERLOGGED, testState.getValue(WATERLOGGED))
                            .setValue(TYPE, testState.getValue(TYPE))
                                   );
            return;
        }
        if (level.getMaxLocalRawBrightness(blockPos.above()) >= 9) {
            final BlockState grassSlabBlockState = this.defaultBlockState();
            final BlockState grassBlockState = Blocks.GRASS_BLOCK.defaultBlockState();
            BlockState testState;

            for (int i = 0; i < 4; ++i) {
                BlockPos testPos = blockPos.offset(random.nextInt(3) - 1,
                                                   random.nextInt(5) - 3,
                                                   random.nextInt(3) - 1);
                if (!canPropagate(grassSlabBlockState, level, testPos)) continue;
                testState = level.getBlockState(testPos);
                if (testState.is(Blocks.DIRT)) {
                    level.setBlockAndUpdate(testPos,
                                            grassBlockState.setValue(SNOWY,
                                                                     level.getBlockState(testPos.above())
                                                                          .is(Blocks.SNOW)));
                } else if (testState.is(WunderreichBlocks.DIRT_SLAB)) {
                    final BlockState newState = grassSlabBlockState.setValue(SNOWY,
                                                                             level.getBlockState(testPos.above())
                                                                                  .is(Blocks.SNOW))
                                                                   .setValue(WATERLOGGED,
                                                                             testState.getValue(WATERLOGGED))
                                                                   .setValue(TYPE, testState.getValue(TYPE));
                    level.setBlockAndUpdate(testPos, newState);
                }
            }
        }
    }
}
