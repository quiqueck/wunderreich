package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The Chronarium's automation contract, exercised with real hoppers.
 *
 * <h3>The rig, and why the negative tests come first</h3>
 * A hopper test is easy to get wrong in the direction of a false pass: a hopper that never manages to
 * move anything (wrong facing, wrong position, never ticked, cooldown never expired) satisfies every
 * "must not move X" assertion perfectly. So each face is covered by a pair - a negative test that
 * shows the wrong slot is unreachable, and a positive one that shows the rig can move items across
 * that same face at all. Neither half means anything alone.
 * <p>
 * The negatives are set up so the only thing standing between the hopper and a transfer is the face
 * mapping: the slot the face <em>does</em> expose is pre-filled to its stack limit with a different
 * item, so a hopper that could reach any other slot would immediately do so.
 *
 * <h3>Timing</h3>
 * A hopper moves one item per 8-tick cooldown, so every phase idles 60 ticks - comfortably more than
 * the 5 transfers any of these tests needs, and short enough that the 200-tick aging recipe used as a
 * backdrop never completes and muddies the counts.
 */
public class ChronariumHopperGameTest {
    private static final BlockPos CHRONARIUM = new BlockPos(3, 3, 3);
    private static final BlockPos HOPPER_ABOVE = new BlockPos(3, 4, 3);
    private static final BlockPos HOPPER_BELOW = new BlockPos(3, 2, 3);
    private static final BlockPos HOPPER_SIDE = new BlockPos(4, 3, 3);

    /** The direction the side hopper has to point to feed the Chronarium at {@link #CHRONARIUM}. */
    private static final Direction SIDE_HOPPER_FACING = Direction.WEST;

    private static final int SETTLE_TICKS = 60;

    /**
     * The face mapping itself, read straight off the {@code WorldlyContainer} methods every hopper
     * consults. The hopper tests below prove the mapping has the effect it claims; this one pins the
     * mapping, and is the test that says which slot each face is <em>supposed</em> to expose.
     */
    @GameTest
    public void worldlyContainerFacesExposeOnlyTheDocumentedSlots(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final List<String> failures = new ArrayList<>();
        final ItemStack probe = new ItemStack(Items.COBBLESTONE_SLAB);

        assertFaceSlots(failures, chronarium, Direction.DOWN, ChronariumBlockEntity.OUTPUT_SLOT);
        assertFaceSlots(failures, chronarium, Direction.UP, ChronariumBlockEntity.INPUT_SLOT);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            assertFaceSlots(failures, chronarium, side, ChronariumBlockEntity.CATALYST_SLOT);
        }

        for (Direction dir : Direction.values()) {
            if (!chronarium.canTakeItemThroughFace(ChronariumBlockEntity.OUTPUT_SLOT, probe, dir)) {
                failures.add("the output slot cannot be extracted through " + dir);
            }
            if (chronarium.canTakeItemThroughFace(ChronariumBlockEntity.INPUT_SLOT, probe, dir)) {
                failures.add("the input slot can be extracted through " + dir);
            }
            if (chronarium.canTakeItemThroughFace(ChronariumBlockEntity.CATALYST_SLOT, probe, dir)) {
                failures.add("the catalyst slot can be extracted through " + dir
                        + " - automation must never be able to pull the catalyst back out");
            }

            if (!chronarium.canPlaceItemThroughFace(ChronariumBlockEntity.INPUT_SLOT, probe, dir)) {
                failures.add("nothing can be inserted into the input slot through " + dir);
            }
            if (!chronarium.canPlaceItemThroughFace(ChronariumBlockEntity.CATALYST_SLOT, probe, dir)) {
                failures.add("nothing can be inserted into the catalyst slot through " + dir);
            }
            if (chronarium.canPlaceItemThroughFace(ChronariumBlockEntity.OUTPUT_SLOT, probe, dir)) {
                failures.add("items can be inserted into the output slot through " + dir);
            }
        }

        ChronariumTestSupport.failIfAny(helper, "Chronarium WorldlyContainer regression", failures);
        helper.succeed();
    }

    /**
     * Negative case for the top face: a hopper above must not be able to reach the catalyst slot.
     * <p>
     * The input slot - the only slot {@code UP} exposes - is filled to 64 cobblestone slabs, so the
     * vine in the hopper has nowhere legal to go. If it moves at all, it reached a slot the top face
     * is not supposed to expose.
     */
    @GameTest(maxTicks = 200)
    public void aHopperAboveCannotReachTheCatalystSlot(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_ABOVE, Direction.DOWN);

        chronarium.setItem(ChronariumBlockEntity.INPUT_SLOT, new ItemStack(Items.COBBLESTONE_SLAB, 64));
        hopper.setItem(0, new ItemStack(Items.VINE, 1));

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack catalyst = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);
                  final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);

                  if (!catalyst.isEmpty()) {
                      failures.add("a hopper on top pushed " + ChronariumTestSupport.describe(catalyst)
                              + " into the catalyst slot - the UP face only exposes the input slot");
                  }
                  if (!input.is(Items.COBBLESTONE_SLAB) || input.getCount() != 64) {
                      failures.add("the pre-filled input slot changed to "
                              + ChronariumTestSupport.describe(input));
                  }
                  if (!hopper.getItem(0).is(Items.VINE)) {
                      failures.add("the vine left the hopper (now "
                              + ChronariumTestSupport.describe(hopper.getItem(0))
                              + ") even though every slot it could legally reach was full");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium top-face regression", failures);
              })
              .thenSucceed();
    }

    /**
     * Positive counterpart to {@link #aHopperAboveCannotReachTheCatalystSlot}: the same rig, with the
     * input slot free, does move the items in - so the negative above is a real restriction and not a
     * hopper that was never going to work.
     */
    @GameTest(maxTicks = 200)
    public void aHopperAboveFeedsTheInputSlot(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_ABOVE, Direction.DOWN);

        hopper.setItem(0, new ItemStack(Items.COBBLESTONE_SLAB, 5));

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);

                  if (!input.is(Items.COBBLESTONE_SLAB) || input.getCount() != 5) {
                      failures.add("a hopper on top should have moved all 5 cobblestone slabs into the"
                              + " input slot, found " + ChronariumTestSupport.describe(input));
                  }
                  if (!chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).isEmpty()) {
                      failures.add("the catalyst slot was filled from above");
                  }
                  if (!hopper.isEmpty()) {
                      failures.add("the hopper still holds "
                              + ChronariumTestSupport.describe(hopper.getItem(0)));
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium top-face regression", failures);
              })
              .thenSucceed();
    }

    /**
     * Negative case for the side faces: a hopper at the side must not be able to reach the input slot.
     * <p>
     * The catalyst slot - the only slot a side exposes - is filled to 64 vines, so the cobblestone slab
     * in the hopper has nowhere legal to go. Had it reached the input slot instead, the machine would
     * also have started an aging cycle, which the assertions below would catch either way.
     */
    @GameTest(maxTicks = 200)
    public void aHopperAtTheSideCannotReachTheInputSlot(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_SIDE, SIDE_HOPPER_FACING);

        chronarium.setItem(ChronariumBlockEntity.CATALYST_SLOT, new ItemStack(Items.VINE, 64));
        hopper.setItem(0, new ItemStack(Items.COBBLESTONE_SLAB, 1));

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);
                  final ItemStack catalyst = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);

                  if (!input.isEmpty()) {
                      failures.add("a hopper at the side pushed " + ChronariumTestSupport.describe(input)
                              + " into the input slot - a side face only exposes the catalyst slot");
                  }
                  if (!catalyst.is(Items.VINE) || catalyst.getCount() != 64) {
                      failures.add("the pre-filled catalyst slot changed to "
                              + ChronariumTestSupport.describe(catalyst));
                  }
                  if (!hopper.getItem(0).is(Items.COBBLESTONE_SLAB)) {
                      failures.add("the cobblestone slab left the hopper (now "
                              + ChronariumTestSupport.describe(hopper.getItem(0))
                              + ") even though every slot it could legally reach was full");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium side-face regression", failures);
              })
              .thenSucceed();
    }

    /**
     * Positive counterpart to {@link #aHopperAtTheSideCannotReachTheInputSlot}: with the catalyst slot
     * free, the same side hopper does fill it.
     */
    @GameTest(maxTicks = 200)
    public void aHopperAtTheSideFeedsTheCatalystSlot(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_SIDE, SIDE_HOPPER_FACING);

        hopper.setItem(0, new ItemStack(Items.VINE, 3));

        helper.startSequence()
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack catalyst = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);

                  if (!catalyst.is(Items.VINE) || catalyst.getCount() != 3) {
                      failures.add("a hopper at the side should have moved all 3 vines into the catalyst"
                              + " slot, found " + ChronariumTestSupport.describe(catalyst));
                  }
                  if (!chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT).isEmpty()) {
                      failures.add("the input slot was filled from the side");
                  }
                  if (!hopper.isEmpty()) {
                      failures.add("the hopper still holds "
                              + ChronariumTestSupport.describe(hopper.getItem(0)));
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium side-face regression", failures);
              })
              .thenSucceed();
    }

    /**
     * Both halves of the bottom face in one run, because the negative half is only meaningful next to
     * the positive one on the identical rig.
     * <p>
     * Phase one: a running machine (aging a slab with a vine catalyst) with an empty output slot. The
     * hopper underneath must come away with nothing at all - before {@code getSlotsForFace} existed it
     * pulled the catalyst out from under a running recipe, which stalled the machine silently.
     * <p>
     * Phase two: the same hopper, same tick budget, with two finished items in the output slot - and
     * now it drains them. The 200-tick recipe running in the background never completes inside the
     * ~120 ticks this test uses, so every item the hopper ends up holding is one this test put there.
     */
    @GameTest(maxTicks = 300)
    public void aHopperBelowTakesTheOutputAndNothingElse(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_BELOW, Direction.DOWN);

        chronarium.setItem(ChronariumBlockEntity.INPUT_SLOT, new ItemStack(Items.COBBLESTONE_SLAB, 1));
        chronarium.setItem(ChronariumBlockEntity.CATALYST_SLOT, new ItemStack(Items.VINE, 1));

        helper.startSequence()
              // ---- negative: nothing but the output may leave the machine
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();

                  if (!hopper.isEmpty()) {
                      failures.add("a hopper below drained " + ChronariumTestSupport.describe(hopper.getItem(0))
                              + " out of a running machine with an empty output slot"
                              + " - only the output slot may ever be extracted");
                  }
                  if (chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT).isEmpty()) {
                      failures.add("the input slot was emptied from below");
                  }
                  if (chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).isEmpty()) {
                      failures.add("the catalyst was pulled out from under a running recipe");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium bottom-face regression", failures);
              })
              // ---- positive: the same hopper does take a finished result
              .thenExecute(() -> chronarium.setItem(
                      ChronariumBlockEntity.OUTPUT_SLOT,
                      new ItemStack(Items.MOSSY_COBBLESTONE_SLAB, 2)
              ))
              .thenIdle(SETTLE_TICKS)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack drained = hopper.getItem(0);

                  if (!drained.is(Items.MOSSY_COBBLESTONE_SLAB) || drained.getCount() != 2) {
                      failures.add("a hopper below should have drained both finished slabs, it holds "
                              + ChronariumTestSupport.describe(drained));
                  }
                  if (!chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT).isEmpty()) {
                      failures.add("the output slot still holds "
                              + ChronariumTestSupport.describe(chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT)));
                  }
                  if (chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).isEmpty()) {
                      failures.add("the catalyst disappeared while the output was being drained");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium bottom-face regression", failures);
              })
              .thenSucceed();
    }

    // ---------------------------------------------------------------- helpers

    private static void assertFaceSlots(
            List<String> failures,
            ChronariumBlockEntity chronarium,
            Direction face,
            int expectedSlot
    ) {
        final int[] slots = chronarium.getSlotsForFace(face);
        if (slots.length != 1 || slots[0] != expectedSlot) {
            failures.add("the " + face + " face exposes " + Arrays.toString(slots)
                    + ", expected exactly [" + expectedSlot + "]");
        }
    }
}
