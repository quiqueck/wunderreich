package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The Suction Tube's redstone output: every transfer emits a pulse that redstone can actually see.
 * <p>
 * Regression: the pulse was set during the transfer and cleared again later in the same server tick,
 * and only comparators were notified - in game a comparator facing away from the tube never read
 * anything and a lamp next to it never lit.
 * <p>
 * Rig: chest below the tube (the DOWN intake, no filter = pull everything), chest above as the
 * destination, a redstone lamp on the east side and a comparator on the west side facing away.
 */
public class SuctionTubeRedstoneGameTest {
    private static final BlockPos TUBE = new BlockPos(3, 2, 3);
    private static final BlockPos SOURCE = TUBE.below();
    private static final BlockPos TARGET = TUBE.above();
    private static final BlockPos LAMP = TUBE.east();
    private static final BlockPos COMPARATOR = TUBE.west();

    @GameTest(maxTicks = 200)
    public void aTransferEmitsAPulseThatLampsAndComparatorsSee(GameTestHelper helper) {
        helper.setBlock(SOURCE, Blocks.CHEST);
        helper.setBlock(TARGET, Blocks.CHEST);
        helper.setBlock(COMPARATOR.below(), Blocks.STONE);
        // a comparator's FACING points at its input side's opposite: EAST here means it reads the tube
        helper.setBlock(
                COMPARATOR,
                Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, helper.getTestRotation().rotate(Direction.EAST))
        );
        helper.setBlock(LAMP, Blocks.REDSTONE_LAMP);
        helper.setBlock(TUBE, WunderreichBlocks.SUCTION_TUBE);

        container(helper, SOURCE).setItem(0, new ItemStack(Items.COBBLESTONE, 3));

        final int[] longestPulse = {0};
        final int[] currentPulse = {0};
        final boolean[] lampLit = {false};
        final int[] maxComparator = {0};

        helper.startSequence()
              .thenExecuteFor(60, () -> {
                  final SuctionTubeBlockEntity tube = tube(helper);
                  if (tube.getRedstoneSignal() > 0) {
                      currentPulse[0]++;
                      longestPulse[0] = Math.max(longestPulse[0], currentPulse[0]);
                  } else {
                      currentPulse[0] = 0;
                  }
                  if (helper.getBlockState(LAMP).getValue(RedstoneLampBlock.LIT)) lampLit[0] = true;
                  if (helper.getLevel().getBlockEntity(helper.absolutePos(COMPARATOR)) instanceof ComparatorBlockEntity c) {
                      maxComparator[0] = Math.max(maxComparator[0], c.getOutputSignal());
                  }
              })
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack moved = container(helper, TARGET).getItem(0);
                  if (!moved.is(Items.COBBLESTONE) || moved.getCount() != 3) {
                      failures.add("the rig is broken: the tube should have moved 3 cobblestone up, the target holds "
                              + ChronariumTestSupport.describe(moved));
                  }
                  if (longestPulse[0] < 2) {
                      failures.add("the output pulse lasted " + longestPulse[0] + " observed tick(s), expected at least 2");
                  }
                  if (!lampLit[0]) {
                      failures.add("a redstone lamp next to the tube never lit");
                  }
                  // a transfer from DOWN emits strength 5 (see SuctionInputs#emitRedstoneSignalForTransfer)
                  if (maxComparator[0] != 5) {
                      failures.add("a comparator facing away from the tube read at most " + maxComparator[0]
                              + ", expected 5 for a transfer from below");
                  }
                  ChronariumTestSupport.failIfAny(helper, "Suction Tube redstone output regression", failures);
              })
              .thenSucceed();
    }

    private static SuctionTubeBlockEntity tube(GameTestHelper helper) {
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(TUBE));
        if (!(be instanceof SuctionTubeBlockEntity t)) {
            throw helper.assertionException(Component.literal("no SuctionTubeBlockEntity at " + TUBE + " (found " + be + ")"));
        }
        return t;
    }

    private static Container container(GameTestHelper helper, BlockPos pos) {
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        if (!(be instanceof Container c)) {
            throw helper.assertionException(Component.literal("no container at " + pos + " (found " + be + ")"));
        }
        return c;
    }
}
