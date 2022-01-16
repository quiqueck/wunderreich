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
            level.setBlockAndUpdate(blockPos, WunderreichBlocks.DIRT_SLAB.defaultBlockState());
            return;
        }
        if (level.getMaxLocalRawBrightness(blockPos.above()) >= 9) {
            BlockState dirtSlabBlockState = this.defaultBlockState();
            BlockState dirtBlockState = Blocks.DIRT.defaultBlockState();
            for (int i = 0; i < 4; ++i) {
                BlockPos testPos = blockPos.offset(random.nextInt(3) - 1,
                                                   random.nextInt(5) - 3,
                                                   random.nextInt(3) - 1);
                if (!canPropagate(dirtSlabBlockState, level, testPos)) continue;

                if (level.getBlockState(testPos).is(Blocks.DIRT)) {
                    level.setBlockAndUpdate(testPos, dirtBlockState);
                } else if (level.getBlockState(testPos).is(WunderreichBlocks.DIRT_SLAB)) {
                    level.setBlockAndUpdate(testPos,
                                            dirtSlabBlockState.setValue(SNOWY,
                                                                        level
                                                                                .getBlockState(testPos.above())
                                                                                .is(Blocks.SNOW)));
                }
            }
        }
    }
}
