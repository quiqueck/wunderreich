package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blocks.FallingSlab;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Gravity of the sand / red sand slabs ({@link FallingSlab}).
 * <p>
 * Regression for the 26.x port: the scheduled tick read {@code SlabBlock.TYPE} off whatever block was
 * below, which throws for every block that is not a slab - so a sand slab resting on stone, or hanging
 * over air, crashed the server two ticks after it was placed (and again on every load, because the
 * tick is persisted). Any of these tests failing with "Exception ticking world" is that crash.
 */
public class FallingSlabGameTest {
    /** Long enough for the 2-tick placement delay, the fall and the landing. */
    private static final int SETTLE_TICKS = 40;

    /** A supported slab (bottom and top, plain and red sand) on a full non-slab block stays where it is. */
    @GameTest(maxTicks = 100)
    public void sandSlabsOnAFullBlockStayPut(GameTestHelper helper) {
        final BlockPos[] supports = {
                new BlockPos(1, 1, 1), new BlockPos(3, 1, 1), new BlockPos(5, 1, 1), new BlockPos(1, 1, 3)
        };
        final BlockState[] slabs = {
                slab(WunderreichSlabBlocks.SAND_SLAB, SlabType.BOTTOM),
                slab(WunderreichSlabBlocks.SAND_SLAB, SlabType.TOP),
                slab(WunderreichSlabBlocks.RED_SAND_SLAB, SlabType.BOTTOM),
                slab(WunderreichSlabBlocks.RED_SAND_SLAB, SlabType.DOUBLE)
        };

        for (int i = 0; i < supports.length; i++) {
            helper.setBlock(supports[i], Blocks.SMOOTH_STONE);
            helper.setBlock(supports[i].above(), slabs[i]);
        }

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  for (int i = 0; i < supports.length; i++) {
                      final BlockState now = helper.getBlockState(supports[i].above());
                      if (now != slabs[i]) {
                          throw helper.assertionException(Component.literal(
                                  slabs[i] + " on smooth stone changed to " + now
                                          + " - a supported sand slab must not move"
                          ));
                      }
                  }
              })
              .thenSucceed();
    }

    /** An unsupported slab falls like sand and lands as a bottom slab on the first solid block. */
    @GameTest(maxTicks = 100)
    public void aSandSlabOverAirFallsAndLandsAsABottomSlab(GameTestHelper helper) {
        final BlockPos floor = new BlockPos(3, 1, 3);
        final BlockPos landing = floor.above();
        final BlockPos start = new BlockPos(3, 5, 3);

        helper.setBlock(floor, Blocks.STONE);
        for (int y = landing.getY(); y < start.getY(); y++) {
            helper.setBlock(new BlockPos(3, y, 3), Blocks.AIR);
        }
        helper.setBlock(start, slab(WunderreichSlabBlocks.SAND_SLAB, SlabType.TOP));

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  helper.assertBlockPresent(Blocks.AIR, start);
                  final BlockState landed = helper.getBlockState(landing);
                  if (!landed.is(WunderreichSlabBlocks.SAND_SLAB) || landed.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) {
                      throw helper.assertionException(Component.literal(
                              "the falling sand slab should have landed on the stone as a bottom slab, found " + landed
                      ));
                  }
              })
              .thenSucceed();
    }

    private static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }
}
