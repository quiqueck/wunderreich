package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.recipes.AgingRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Shared rig for the three Chronarium GameTest classes.
 * <p>
 * <b>Everything here is scoped to the structure of the calling test.</b> Fabric's GameTest runner
 * executes many tests concurrently in one dedicated server and one shared level, so any lookup that
 * goes through the level (entities in particular) has to be bounded by
 * {@link GameTestHelper#absolutePos(BlockPos)} rather than by a global search or a static counter.
 * {@link #experienceNear} exists for exactly that reason.
 */
final class ChronariumTestSupport {
    private ChronariumTestSupport() {
    }

    // ---------------------------------------------------------------- blocks

    /**
     * Places a Chronarium in its default state (facing north, not working) and returns its block
     * entity. Use this whenever the test is about what the machine <em>does</em>; the placement path
     * itself is exercised separately by {@code ChronariumAgingGameTest#manualPlacementFacesThePlayer}.
     */
    static ChronariumBlockEntity placeChronarium(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, WunderreichBlocks.CHRONARIUM);
        return chronarium(helper, pos);
    }

    static ChronariumBlockEntity chronarium(GameTestHelper helper, BlockPos pos) {
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        if (!(be instanceof ChronariumBlockEntity c)) {
            throw helper.assertionException(Component.literal(
                    "no ChronariumBlockEntity at " + pos + " (found " + be + ")"
            ));
        }
        return c;
    }

    /**
     * A hopper pointing at {@code facing}. The facing is converted with
     * {@link GameTestHelper#getAbsoluteDirection(Direction)} because {@code setBlock} writes the state
     * verbatim while positions are rotated with the structure - mixing the two silently builds a rig
     * that points the wrong way as soon as a test is given a rotation.
     */
    static HopperBlockEntity placeHopper(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(
                pos,
                Blocks.HOPPER.defaultBlockState()
                             .setValue(HopperBlock.FACING, helper.getAbsoluteDirection(facing))
        );
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        if (!(be instanceof HopperBlockEntity hopper)) {
            throw helper.assertionException(Component.literal(
                    "no HopperBlockEntity at " + pos + " (found " + be + ")"
            ));
        }
        return hopper;
    }

    // ---------------------------------------------------------------- recipes

    /**
     * The aging recipe for the two given ingredients, resolved the same way the block entity resolves
     * it. Deliberately not a hardcoded recipe id: the ids are derived from the block names in
     * {@code WunderreichRecipes}, and a test that spells one out would start passing vacuously the day
     * the naming scheme changes.
     */
    static RecipeHolder<AgingRecipe> recipeFor(GameTestHelper helper, ItemStack input, ItemStack catalyst) {
        final Optional<RecipeHolder<AgingRecipe>> found = AgingRecipe.find(helper.getLevel(), input, catalyst);
        if (found.isEmpty()) {
            throw helper.assertionException(Component.literal(
                    "no wunderreich:aging recipe for input=" + input + " catalyst=" + catalyst
                            + " - the whole Chronarium test suite depends on this recipe existing"
            ));
        }
        return found.get();
    }

    /**
     * How much experience {@code completions} results of {@code recipe} are worth, and a hard failure
     * when that number is not deterministic.
     * <p>
     * {@code ChronariumBlockEntity#createExperience} (vanilla's furnace formula) floors
     * {@code completions * experience} and then settles the remaining fraction with a single random
     * roll. A test that picks a completion count leaving a fraction therefore has a coin flip baked
     * into its expected value, so this refuses to produce one instead of producing a flaky test.
     * <p>
     * An expectation of zero is refused for a different reason: "no orbs were found" is exactly what a
     * completely broken award path also produces, so a recipe that happened to be worth no experience
     * would turn the experience tests into tests of nothing at all.
     */
    static int expectedExperience(GameTestHelper helper, RecipeHolder<AgingRecipe> recipe, int completions) {
        final float perItem = recipe.value().experience();
        final float total = completions * perItem;
        if (Mth.frac(total) != 0.0f) {
            throw helper.assertionException(Component.literal(
                    "the test asks for " + completions + " completions of " + recipe.id() + " at "
                            + perItem + " xp each, which is " + total + " - not a whole number, so the"
                            + " award is decided by a random roll. Pick a completion count that lands"
                            + " on an integer."
            ));
        }

        final int expected = Mth.floor(total);
        if (expected <= 0) {
            throw helper.assertionException(Component.literal(
                    completions + " completions of " + recipe.id() + " are worth " + total
                            + " experience. A test expecting zero orbs cannot tell a working award path"
                            + " from a broken one - raise the completion count, or pick a recipe that is"
                            + " worth something."
            ));
        }
        return expected;
    }

    // ---------------------------------------------------------------- players

    /**
     * A {@code PlayerList}-registered player standing at {@code relativePos} inside the test structure.
     * <p>
     * Built on {@code makeMockServerPlayerInLevel} because that is the only mock the helper offers
     * with a live {@code connection}, which {@code ServerPlayer#openMenu} and
     * {@code ServerPlayer#awardRecipes} both need - {@code makeMockServerPlayer(GameType)} leaves it
     * null and NPEs on the first packet. The trade-off is that it is hard-coded to creative (its
     * anonymous subclass overrides {@code gameMode()}), which is irrelevant here: nothing about taking
     * an item out of a menu or receiving experience orbs behaves differently in creative.
     * <p>
     * It is moved with {@code teleportTo} rather than {@code snapTo} - only {@code teleportTo} moves
     * the player's chunk-loading ticket, and it spawns at world spawn, nowhere near the structure.
     * <p>
     * The player is removed again before the test ends, so it does not linger in the shared test level
     * and get caught by another test's entity search.
     */
    @SuppressWarnings("removal")
    static ServerPlayer menuPlayer(GameTestHelper helper, BlockPos relativePos) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final BlockPos abs = helper.absolutePos(relativePos);
        player.teleportTo(
                helper.getLevel(),
                abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5,
                Set.of(),
                0.0f, 0.0f,
                false
        );
        helper.runBeforeTestEnd(() -> helper.getLevel().getServer().getPlayerList().remove(player));
        return player;
    }

    // ---------------------------------------------------------------- experience orbs

    /**
     * The total experience sitting in orbs within {@code radius} of {@code relativePos}, summed over
     * their values rather than counted - {@code ExperienceOrb.award} both splits a large award over
     * several orbs and merges a small one into an orb that is already there, so the orb <i>count</i>
     * says nothing.
     * <p>
     * Bounded to this test's structure on purpose; see the class javadoc.
     */
    static int experienceNear(GameTestHelper helper, BlockPos relativePos, double radius) {
        int total = 0;
        for (ExperienceOrb orb : helper.getEntities(EntityTypes.EXPERIENCE_ORB, relativePos, radius)) {
            total += orb.getValue();
        }
        return total;
    }

    // ---------------------------------------------------------------- assertions

    /**
     * Reports every collected problem in one exception instead of only the first, so a broken rig does
     * not have to be fixed one test run at a time.
     */
    static void failIfAny(GameTestHelper helper, String headline, List<String> failures) {
        if (!failures.isEmpty()) {
            throw helper.assertionException(Component.literal(
                    headline + ":\n - " + String.join("\n - ", failures)
            ));
        }
    }

    static String describe(ItemStack stack) {
        return stack.isEmpty() ? "<empty>" : (stack.getCount() + "x " + stack.getItem());
    }
}
