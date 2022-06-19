package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.math.DoubleMath;

import java.util.function.Consumer;

public class SpreadableSnowyDirtSlab extends SnowyDirtSlab {
    public SpreadableSnowyDirtSlab(Block baseBlock) {
        super(baseBlock);
    }

    public static boolean doesOcclude(BlockGetter blockGetter,
                                      BlockState state,
                                      BlockPos pos,
                                      BlockState aboveState,
                                      BlockPos abovePos,
                                      Direction direction
    ) {


        if (!aboveState.canOcclude()) {
            return false;
        }

        VoxelShape shape = state.getOcclusionShape(blockGetter, pos);
        VoxelShape aboveShape = aboveState.getOcclusionShape(blockGetter, abovePos);
        if (!aboveState.useShapeForLightOcclusion() && aboveShape != Shapes.block()) {
            return false;
        }

        if (aboveShape.isEmpty()) return false;
        Direction.Axis axis = direction.getAxis();
        if (!DoubleMath.fuzzyEquals(shape.max(axis), 1.0, 1.0E-7)) {
            return false;
        }
        if (!DoubleMath.fuzzyEquals(aboveShape.min(axis), 0.0, 1.0E-7)) {
            return false;
        }
        return Shapes.mergedFaceOccludes(shape, aboveShape, direction);
    }

    private static boolean canBeGrassNewSlab(BlockState state,
                                             LevelReader reader,
                                             BlockPos pos
    ) {
        if (state.getValue(TYPE) == SlabType.BOTTOM) {
            return true;
        }

        return canBeGrassNew(state, reader, pos);
    }

    private static boolean canBeGrassNew(BlockState state,
                                         LevelReader reader,
                                         BlockPos pos
    ) {

        BlockPos abovePos = pos.above();
        BlockState aboveState = reader.getBlockState(abovePos);
        if (state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (state.getFluidState().getAmount() == 8) {
            return false;
        }
        return !doesOcclude(reader,
                            state,
                            pos,
                            aboveState,
                            abovePos,
                            Direction.UP
        );
    }

    public static boolean canBeGrass(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        final Block bl = blockState.getBlock();
        if (bl instanceof DirtSlabBlock) {
            return canBeGrassNewSlab(blockState, levelReader, blockPos);
        } else {
            return SpreadingSnowyDirtBlock.canBeGrass(blockState, levelReader, blockPos);
        }
    }

    private static boolean canPropagate(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        return canBeGrass(blockState, levelReader, blockPos) && !levelReader
                .getFluidState(blockPos2)
                .is(FluidTags.WATER);
    }

    public static void spreadingTick(Block me,
                                     BlockState blockState,
                                     ServerLevel level,
                                     BlockPos blockPos,
                                     RandomSource random
    ) {
        if (!Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.DIRT_SLAB)
                || !Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.GRASS_SLAB))
            return;
        if (!canBeGrass(blockState, level, blockPos)) {
            final BlockState testState = level.getBlockState(blockPos);
            if (me instanceof SpreadableSnowyDirtSlab) {
                level.setBlockAndUpdate(
                        blockPos,
                        WunderreichSlabBlocks.DIRT_SLAB
                                .defaultBlockState()
                                .setValue(WATERLOGGED, testState.getValue(WATERLOGGED))
                                .setValue(TYPE, testState.getValue(TYPE))
                );
            } else if (me instanceof SpreadingSnowyDirtBlock) {
                level.setBlockAndUpdate(
                        blockPos,
                        Blocks.DIRT.defaultBlockState()
                );
            }
            return;
        }

        if (level.getMaxLocalRawBrightness(blockPos.above()) >= 9) {
            final BlockState grassSlabBlockState = WunderreichSlabBlocks.GRASS_SLAB.defaultBlockState();
            final BlockState grassBlockState = Blocks.GRASS_BLOCK.defaultBlockState();
            BlockState testState;

            for (int i = 0; i < 2; ++i) {
                BlockPos testPos = blockPos.offset(random.nextInt(3) - 1,
                                                   random.nextInt(5) - 3,
                                                   random.nextInt(3) - 1
                );
                testState = level.getBlockState(testPos);
                if (!canPropagate(testState, level, testPos)) continue;

                if (testState.is(Blocks.DIRT)) {
                    level.setBlockAndUpdate(testPos,
                                            grassBlockState.setValue(SNOWY,
                                                                     level.getBlockState(testPos.above())
                                                                          .is(Blocks.SNOW)
                                            )
                    );
                } else if (testState.is(WunderreichSlabBlocks.DIRT_SLAB)) {
                    final BlockState newState = grassSlabBlockState.setValue(SNOWY,
                                                                             level.getBlockState(testPos.above())
                                                                                  .is(Blocks.SNOW)
                                                                   )
                                                                   .setValue(WATERLOGGED,
                                                                             testState.getValue(WATERLOGGED)
                                                                   )
                                                                   .setValue(TYPE, testState.getValue(TYPE));
                    level.setBlockAndUpdate(testPos, newState);
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource random) {
        spreadingTick(this, blockState, level, blockPos, random);
    }

    public static class GrassSlab extends SpreadableSnowyDirtSlab {
        public GrassSlab(Block baseBlock) {
            super(baseBlock);
        }

        @Override
        public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
            blockTags.accept(BlockTags.SLABS);
            itemTags.accept(ItemTags.SLABS);

            blockTags.accept(BlockTags.MINEABLE_WITH_SHOVEL);
        }
    }
}
