package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.blocks.Chronarium;
import de.ambertation.wunderreich.recipes.AgingRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Placement and the aging cycle of the {@link Chronarium}.
 *
 * <h3>What is pinned here</h3>
 * <ul>
 *     <li>{@link Chronarium#FACING} comes from the placing player's facing, mirrored (the model's
 *     glass window is on the north face, so the block has to look <em>at</em> the player).</li>
 *     <li>One completed result consumes exactly one input item and leaves the catalyst stack
 *     untouched - not shrunk, not damaged, not emptied. The water-bucket case is separate
 *     ({@link #theWaterBucketCatalystSurvivesOneCompletion}) because a bucket is the one catalyst that a
 *     naive "consume the ingredient" implementation would silently turn into an empty bucket.</li>
 *     <li>{@link Chronarium#WORKING} - the only thing the outside of the block says about what is
 *     going on inside it - is true while a recipe is advancing and false again once it stops.</li>
 * </ul>
 *
 * <h3>Recipes used</h3>
 * Both recipes are looked up through {@link AgingRecipe#find} rather than by id, and their declared
 * {@code time()}/{@code experience()} are asserted against the values {@code WunderreichRecipes}
 * derives, so a change to either the naming scheme or the experience curve fails loudly here instead
 * of quietly turning these tests into no-ops.
 */
public class ChronariumAgingGameTest {
    private static final BlockPos CHRONARIUM = new BlockPos(3, 3, 3);

    /** Somewhere out of the way for the mock player used by the placement test. */
    private static final BlockPos PLACER = new BlockPos(4, 5, 6);

    /** The four blocks the placement test clicks on, one per horizontal direction. */
    private static final BlockPos[] SUPPORTS = {
            new BlockPos(1, 2, 1),
            new BlockPos(3, 2, 1),
            new BlockPos(5, 2, 1),
            new BlockPos(7, 2, 1)
    };

    private static final Direction[] HORIZONTALS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    /** cobblestone_slab + vine -> mossy_cobblestone_slab, the shortest aging recipe we ship. */
    private static final int SLAB_TIME = 200;
    private static final float SLAB_XP = 0.10f;

    /**
     * copper_block + water_bucket -> exposed_copper, the first (and now shortest) step of the
     * weathering chain - {@code copperTimes[0]} in {@code WunderreichRecipes}.
     * <p>
     * The copper items are reached through {@code Items.COPPER_BLOCK.weathering()} because 26.2 packs
     * a whole family into one {@code WeatheringCopperCollection} instead of exposing one constant per
     * weather state - the same accessor {@code WunderreichRecipes} walks to build the chain.
     */
    private static final Item UNAFFECTED_COPPER = Items.COPPER_BLOCK.weathering().unaffected();
    private static final Item EXPOSED_COPPER = Items.COPPER_BLOCK.weathering().exposed();
    private static final int COPPER_TIME = 100;
    private static final float COPPER_XP = 0.06f;

    /**
     * Places a Chronarium the way a player does - by using the block item against the top face of a
     * block - once for each horizontal direction the player can be facing, and checks the resulting
     * {@link Chronarium#FACING}.
     * <p>
     * This goes through {@code ItemStack#useOn} rather than calling
     * {@code Chronarium#getStateForPlacement} directly, so the whole {@code BlockItem} placement path
     * is covered: a block that never reached {@code getStateForPlacement} at all (because it failed
     * {@code canPlace}, say) would fail here rather than pass on a hand-built context.
     * <p>
     * A freshly placed Chronarium is also asserted to be idle - {@link Chronarium#WORKING} raises the
     * block's light level, so a block that came out of the item working would light up an empty room.
     */
    @GameTest
    public void manualPlacementFacesThePlayer(GameTestHelper helper) {
        final Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        final List<String> failures = new ArrayList<>();

        for (int i = 0; i < HORIZONTALS.length; i++) {
            final Direction facing = HORIZONTALS[i];
            final BlockPos support = SUPPORTS[i];
            final BlockPos target = support.above();

            helper.setBlock(support, Blocks.STONE);

            // Park the player away from the build site, looking along `facing`. UseOnContext reads the
            // horizontal direction straight off the player's yRot.
            final BlockPos placerAbs = helper.absolutePos(PLACER);
            player.snapTo(
                    placerAbs.getX() + 0.5, placerAbs.getY(), placerAbs.getZ() + 0.5,
                    facing.toYRot(), 0.0f
            );
            if (player.getDirection() != facing) {
                failures.add("could not point the mock player " + facing
                        + " (getDirection() reports " + player.getDirection() + ")");
                continue;
            }

            final BlockPos supportAbs = helper.absolutePos(support);
            final BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(supportAbs).add(0.0, 0.5, 0.0),
                    Direction.UP,
                    supportAbs,
                    false
            );
            new ItemStack(WunderreichBlocks.CHRONARIUM)
                    .useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

            final BlockState placed = helper.getBlockState(target);
            if (!placed.is(WunderreichBlocks.CHRONARIUM)) {
                failures.add("using the chronarium item on top of " + support
                        + " placed " + placed + " instead of a chronarium");
                continue;
            }

            final Direction expected = facing.getOpposite();
            final Direction actual = placed.getValue(Chronarium.FACING);
            if (actual != expected) {
                failures.add("a player facing " + facing + " should place a chronarium facing "
                        + expected + " (towards them), got " + actual);
            }
            if (placed.getValue(Chronarium.WORKING)) {
                failures.add("a freshly placed chronarium is already WORKING - it has nothing in it");
            }
        }

        ChronariumTestSupport.failIfAny(helper, "Chronarium placement regression", failures);
        helper.succeed();
    }

    /**
     * The full aging cycle, run twice back to back on one stack of two inputs.
     * <p>
     * Two inputs rather than one on purpose: the first completion shows that exactly <em>one</em> item
     * is consumed (the stack has to go 2 -> 1, not 2 -> 0), and the second shows what happens when the
     * machine runs dry, which is the only way to observe {@link Chronarium#WORKING} going back to
     * false.
     * <p>
     * The catalyst is compared with {@link ItemStack#matches} against a copy taken before the run, so
     * a change to its count, damage or any data component fails - not just a change to its item.
     */
    // Two completions back to back, so the budget has to cover 2 x SLAB_TIME with room to spare.
    @GameTest(maxTicks = 3 * SLAB_TIME)
    public void agingConsumesOneInputPerResultAndNeverTouchesTheCatalyst(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);

        final RecipeHolder<AgingRecipe> recipe = ChronariumTestSupport.recipeFor(
                helper,
                new ItemStack(Items.COBBLESTONE_SLAB),
                new ItemStack(Items.VINE)
        );
        assertRecipeShape(helper, recipe, SLAB_TIME, SLAB_XP);

        chronarium.setItem(ChronariumBlockEntity.INPUT_SLOT, new ItemStack(Items.COBBLESTONE_SLAB, 2));
        chronarium.setItem(ChronariumBlockEntity.CATALYST_SLOT, new ItemStack(Items.VINE));
        final ItemStack catalystBefore = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).copy();

        helper.startSequence()
              // ---- first completion
              .thenWaitUntil(() -> {
                  if (chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT).isEmpty()) {
                      throw helper.assertionException(Component.literal("still aging the first slab"));
                  }
              })
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack output = chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT);
                  final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);

                  if (!output.is(Items.MOSSY_COBBLESTONE_SLAB) || output.getCount() != 1) {
                      failures.add("the first completion produced " + ChronariumTestSupport.describe(output)
                              + " instead of 1x mossy_cobblestone_slab");
                  }
                  if (input.getCount() != 1) {
                      failures.add("one completion must consume exactly one input item: the stack went"
                              + " from 2x cobblestone_slab to " + ChronariumTestSupport.describe(input));
                  }
                  if (!helper.getBlockState(CHRONARIUM).getValue(Chronarium.WORKING)) {
                      failures.add("WORKING is false while the machine is still aging the second slab");
                  }
                  checkCatalyst(failures, chronarium, catalystBefore, "after the first completion");

                  ChronariumTestSupport.failIfAny(helper, "Chronarium aging regression", failures);
              })
              // ---- second completion empties the input and stops the machine
              .thenWaitUntil(() -> {
                  if (!chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT).isEmpty()) {
                      throw helper.assertionException(Component.literal("still aging the second slab"));
                  }
              })
              // WORKING only flips on the tick after the machine finds nothing left to do.
              .thenIdle(3)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack output = chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT);

                  if (!output.is(Items.MOSSY_COBBLESTONE_SLAB) || output.getCount() != 2) {
                      failures.add("two inputs should have produced 2x mossy_cobblestone_slab, got "
                              + ChronariumTestSupport.describe(output));
                  }
                  if (helper.getBlockState(CHRONARIUM).getValue(Chronarium.WORKING)) {
                      failures.add("WORKING is still true after the input ran out - the block would keep"
                              + " showing the vortex and emitting light " + Chronarium.LIGHT_WORKING);
                  }
                  checkCatalyst(failures, chronarium, catalystBefore, "after both completions");

                  ChronariumTestSupport.failIfAny(helper, "Chronarium aging regression", failures);
              })
              .thenSucceed();
    }

    /**
     * The catalyst invariant with the item that actually makes it interesting.
     * <p>
     * A water bucket is the catalyst of every copper weathering step, and it is the case where "the
     * catalyst is never consumed" and "the catalyst is never <em>changed</em>" come apart: a machine
     * that treated the catalyst as a normal ingredient would hand back an empty bucket (vanilla's own
     * furnace does exactly that for its wet-sponge byproduct), which looks like a feature rather than
     * a bug until the second recipe fails to start.
     */
    @GameTest(maxTicks = 3 * COPPER_TIME)
    public void theWaterBucketCatalystSurvivesOneCompletion(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);

        final RecipeHolder<AgingRecipe> recipe = ChronariumTestSupport.recipeFor(
                helper,
                new ItemStack(UNAFFECTED_COPPER),
                new ItemStack(Items.WATER_BUCKET)
        );
        assertRecipeShape(helper, recipe, COPPER_TIME, COPPER_XP);

        chronarium.setItem(ChronariumBlockEntity.INPUT_SLOT, new ItemStack(UNAFFECTED_COPPER));
        chronarium.setItem(ChronariumBlockEntity.CATALYST_SLOT, new ItemStack(Items.WATER_BUCKET));
        final ItemStack catalystBefore = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).copy();

        helper.startSequence()
              .thenWaitUntil(() -> {
                  if (chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT).isEmpty()) {
                      throw helper.assertionException(Component.literal("still weathering the copper"));
                  }
              })
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack output = chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT);
                  final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);
                  final ItemStack catalyst = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);

                  if (!output.is(EXPOSED_COPPER) || output.getCount() != 1) {
                      failures.add("weathering a copper block produced "
                              + ChronariumTestSupport.describe(output) + " instead of 1x exposed_copper");
                  }
                  if (!input.isEmpty()) {
                      failures.add("the single input should have been consumed, found "
                              + ChronariumTestSupport.describe(input));
                  }
                  if (!catalyst.is(Items.WATER_BUCKET)) {
                      failures.add("the water bucket catalyst came out as "
                              + ChronariumTestSupport.describe(catalyst)
                              + " - a catalyst is never consumed, emptied or swapped for its remainder");
                  }
                  checkCatalyst(failures, chronarium, catalystBefore, "after weathering one copper block");

                  ChronariumTestSupport.failIfAny(helper, "Chronarium catalyst regression", failures);
              })
              .thenSucceed();
    }

    // ---------------------------------------------------------------- helpers

    private static void assertRecipeShape(
            GameTestHelper helper,
            RecipeHolder<AgingRecipe> recipe,
            int expectedTime,
            float expectedExperience
    ) {
        final List<String> failures = new ArrayList<>();
        if (recipe.value().time() != expectedTime) {
            failures.add(recipe.id() + " takes " + recipe.value().time() + " ticks, this test was"
                    + " written against " + expectedTime);
        }
        if (recipe.value().experience() != expectedExperience) {
            failures.add(recipe.id() + " is worth " + recipe.value().experience() + " xp, this test was"
                    + " written against " + expectedExperience);
        }
        ChronariumTestSupport.failIfAny(helper, "aging recipe shape changed", failures);
    }

    private static void checkCatalyst(
            List<String> failures,
            ChronariumBlockEntity chronarium,
            ItemStack before,
            String when
    ) {
        final ItemStack now = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);
        if (!ItemStack.matches(before, now)) {
            failures.add("the catalyst changed " + when + ": was " + ChronariumTestSupport.describe(before)
                    + ", is " + ChronariumTestSupport.describe(now)
                    + " (compared with ItemStack.matches, so count and components count too)");
        }
    }
}
