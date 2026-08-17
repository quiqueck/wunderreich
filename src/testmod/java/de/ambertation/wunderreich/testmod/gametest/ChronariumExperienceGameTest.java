package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.gui.chronarium.ChronariumMenu;
import de.ambertation.wunderreich.recipes.AgingRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The experience half of the Chronarium: who gets paid for a finished result, and when.
 *
 * <h3>Why this needs a GameTest and not a console session</h3>
 * The award does not happen when a recipe finishes. It happens when a <b>player</b> takes the result
 * out of {@code ChronariumMenu}'s output slot - the block entity only books a counter
 * ({@code RecipesUsed}) until then. Reproducing that needs a real {@link ServerPlayer} with a live
 * menu clicking the result slot, which no amount of {@code /setblock} and {@code /item replace} from
 * a server console can produce. That is what makes this the one part of the Chronarium that has never
 * been verified anywhere else.
 *
 * <h3>Why the debt is seeded instead of earned</h3>
 * {@code setRecipeUsed} is the public {@code RecipeCraftingHolder} hook the block entity's own
 * {@code finish()} calls once per completed result, so calling it directly books exactly the debt ten
 * finished results would - without spending 10 x 200 ticks of test server time getting there. The
 * aging path that calls it is covered by {@code ChronariumAgingGameTest}.
 *
 * <h3>Why exactly ten completions</h3>
 * {@code createExperience} multiplies first and rounds once, settling the leftover fraction with a
 * single random roll (vanilla's furnace formula, verbatim). At 0.10 xp per slab, ten completions come
 * to exactly 1.0 and the roll never happens; nine would come to 0.9 and the expected value would be a
 * coin flip. {@link ChronariumTestSupport#expectedExperience} enforces that rather than trusting the
 * arithmetic to stay true if the experience curve is retuned.
 */
public class ChronariumExperienceGameTest {
    private static final BlockPos CHRONARIUM = new BlockPos(3, 3, 3);
    private static final BlockPos HOPPER_BELOW = new BlockPos(3, 2, 3);
    private static final BlockPos PLAYER = new BlockPos(1, 3, 1);

    /** Bounded to this test's own structure - tests share one level and run concurrently. */
    private static final double ORB_SEARCH_RADIUS = 6.0;

    private static final int COMPLETIONS = 10;

    /**
     * A player taking the finished result out of the menu is paid for every result the machine banked
     * since the last time somebody did, and the debt is cleared afterwards.
     * <p>
     * The second take is what proves the clearing: it is the identical click on an identical stack, and
     * it has to be worth nothing. Without it, a machine that paid out but never reset its counter would
     * pass just as well - and would then pay out again for every subsequent item forever.
     * <p>
     * The orbs are counted immediately after the click, in the same tick, on purpose: they spawn at the
     * player's feet, so on the very next tick the player has already absorbed them and there is nothing
     * left to count. Their <em>values</em> are summed rather than the orbs counted, because
     * {@code ExperienceOrb.award} both splits large awards over several orbs and merges small ones into
     * an orb that is already lying there.
     */
    @GameTest
    public void takingTheOutputFromTheMenuAwardsTheBankedExperience(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final RecipeHolder<AgingRecipe> recipe = ChronariumTestSupport.recipeFor(
                helper,
                new ItemStack(Items.COBBLESTONE_SLAB),
                new ItemStack(Items.VINE)
        );
        final int expected = ChronariumTestSupport.expectedExperience(helper, recipe, COMPLETIONS);

        for (int i = 0; i < COMPLETIONS; i++) {
            chronarium.setRecipeUsed(recipe);
        }
        chronarium.setItem(ChronariumBlockEntity.OUTPUT_SLOT, new ItemStack(Items.MOSSY_COBBLESTONE_SLAB, 4));

        final ServerPlayer player = ChronariumTestSupport.menuPlayer(helper, PLAYER);
        final AbstractContainerMenu menu = openChronariumMenu(helper, player, chronarium);

        final List<String> failures = new ArrayList<>();

        if (ChronariumTestSupport.experienceNear(helper, PLAYER, ORB_SEARCH_RADIUS) != 0) {
            failures.add("there were already experience orbs around the player before anything was taken");
        }

        // Shift-click the result slot, i.e. what a player actually does with a finished machine.
        menu.clicked(ChronariumMenu.OUTPUT_SLOT, 0, ContainerInput.QUICK_MOVE, player);

        final int afterFirstTake = ChronariumTestSupport.experienceNear(helper, PLAYER, ORB_SEARCH_RADIUS);
        if (afterFirstTake != expected) {
            failures.add("taking " + COMPLETIONS + " results' worth of output from the menu dropped "
                    + afterFirstTake + " experience, expected " + expected
                    + " (" + COMPLETIONS + " x " + recipe.value().experience() + " from " + recipe.id() + ")");
        }
        if (!chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT).isEmpty()) {
            failures.add("the output slot still holds " + ChronariumTestSupport.describe(
                    chronarium.getItem(ChronariumBlockEntity.OUTPUT_SLOT)
            ) + " after a shift-click");
        }
        if (!player.getInventory().contains(new ItemStack(Items.MOSSY_COBBLESTONE_SLAB))) {
            failures.add("the finished slabs did not end up in the player's inventory");
        }

        // Same click, same item, on a machine that owes nothing any more.
        chronarium.setItem(ChronariumBlockEntity.OUTPUT_SLOT, new ItemStack(Items.MOSSY_COBBLESTONE_SLAB, 4));
        menu.clicked(ChronariumMenu.OUTPUT_SLOT, 0, ContainerInput.QUICK_MOVE, player);

        final int afterSecondTake = ChronariumTestSupport.experienceNear(helper, PLAYER, ORB_SEARCH_RADIUS);
        if (afterSecondTake != afterFirstTake) {
            failures.add("a second take paid out another " + (afterSecondTake - afterFirstTake)
                    + " experience - RecipesUsed was not cleared, so the machine pays for the same"
                    + " results over and over");
        }

        ChronariumTestSupport.failIfAny(helper, "Chronarium experience regression", failures);
        helper.succeed();
    }

    /**
     * Vanilla's furnace quirk, deliberately preserved: automating the output forfeits the experience.
     * <p>
     * A hopper talks to the {@code Container} directly and never touches the menu's result slot, which
     * is the only place the award lives. So a hopper draining the output awards nothing - and, just as
     * importantly, does <b>not</b> clear the debt: the phase-two take by a real player has to still be
     * worth the full {@value #COMPLETIONS} results. A machine that cleared {@code RecipesUsed} on any
     * removal would pass the "awards nothing" half and silently destroy the banked experience.
     */
    @GameTest(maxTicks = 200)
    public void aHopperDrainingTheOutputAwardsNothingAndKeepsTheDebt(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final HopperBlockEntity hopper = ChronariumTestSupport.placeHopper(helper, HOPPER_BELOW, Direction.DOWN);

        final RecipeHolder<AgingRecipe> recipe = ChronariumTestSupport.recipeFor(
                helper,
                new ItemStack(Items.COBBLESTONE_SLAB),
                new ItemStack(Items.VINE)
        );
        final int expected = ChronariumTestSupport.expectedExperience(helper, recipe, COMPLETIONS);

        for (int i = 0; i < COMPLETIONS; i++) {
            chronarium.setRecipeUsed(recipe);
        }
        chronarium.setItem(ChronariumBlockEntity.OUTPUT_SLOT, new ItemStack(Items.MOSSY_COBBLESTONE_SLAB, 2));

        helper.startSequence()
              .thenIdle(60)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  final ItemStack drained = hopper.getItem(0);

                  if (!drained.is(Items.MOSSY_COBBLESTONE_SLAB) || drained.getCount() != 2) {
                      failures.add("the hopper below should have drained both finished slabs (otherwise"
                              + " this test proves nothing about what draining awards), it holds "
                              + ChronariumTestSupport.describe(drained));
                  }
                  if (ChronariumTestSupport.experienceNear(helper, CHRONARIUM, ORB_SEARCH_RADIUS) != 0) {
                      failures.add("a hopper draining the output dropped experience - only a player"
                              + " taking from the menu is ever paid");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium hopper-experience regression", failures);
              })
              // The debt has to have survived the hopper, so a player showing up later still gets paid.
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();

                  chronarium.setItem(
                          ChronariumBlockEntity.OUTPUT_SLOT,
                          new ItemStack(Items.MOSSY_COBBLESTONE_SLAB, 1)
                  );
                  final ServerPlayer player = ChronariumTestSupport.menuPlayer(helper, PLAYER);
                  final AbstractContainerMenu menu = openChronariumMenu(helper, player, chronarium);
                  menu.clicked(ChronariumMenu.OUTPUT_SLOT, 0, ContainerInput.QUICK_MOVE, player);

                  final int awarded = ChronariumTestSupport.experienceNear(helper, PLAYER, ORB_SEARCH_RADIUS);
                  if (awarded != expected) {
                      failures.add("after a hopper drained two results, a player taking the next one was"
                              + " paid " + awarded + " experience instead of the full " + expected
                              + " the machine still owed - draining must not clear RecipesUsed");
                  }

                  ChronariumTestSupport.failIfAny(helper, "Chronarium hopper-experience regression", failures);
              })
              .thenSucceed();
    }

    /** Menu index of the player's first hotbar slot: 3 machine slots + 27 inventory slots. */
    private static final int FIRST_HOTBAR_SLOT = ChronariumMenu.CONTAINER_SLOT_COUNT + 27;

    // ---------------------------------------------------------------- helpers

    private static AbstractContainerMenu openChronariumMenu(
            GameTestHelper helper,
            ServerPlayer player,
            ChronariumBlockEntity chronarium
    ) {
        player.openMenu(chronarium);
        final AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof ChronariumMenu)) {
            throw helper.assertionException(Component.literal(
                    "opening the chronarium gave the player a " + menu + " instead of a ChronariumMenu"
            ));
        }
        return menu;
    }

    /**
     * The other half of a player's interaction with the machine: putting things <em>in</em>.
     * <p>
     * Everything else in this suite seeds the slots directly with {@code setItem}, which bypasses the
     * menu entirely - so {@code ChronariumMenu#quickMoveStack} and the slots' {@code mayPlace} were
     * never exercised from the player's side. A mistake there is invisible until somebody shift-clicks
     * cobblestone and watches it land in the catalyst slot.
     * <p>
     * The menu's rule is: from the player inventory, prefer the input slot and only fall back to the
     * catalyst slot when the input cannot take (any more of) that item. Both branches are checked
     * here, in that order, because the fallback is only reachable once the input is occupied.
     */
    @GameTest
    public void aPlayerCanLoadTheMachineThroughTheMenu(GameTestHelper helper) {
        final ChronariumBlockEntity chronarium = ChronariumTestSupport.placeChronarium(helper, CHRONARIUM);
        final ServerPlayer player = ChronariumTestSupport.menuPlayer(helper, PLAYER);
        final AbstractContainerMenu menu = openChronariumMenu(helper, player, chronarium);

        final List<String> failures = new ArrayList<>();

        // The take-only output slot must refuse anything a player tries to push into it.
        if (menu.getSlot(ChronariumMenu.OUTPUT_SLOT).mayPlace(new ItemStack(Items.COBBLESTONE_SLAB))) {
            failures.add("the output slot accepted an item from the player - it is take only");
        }

        // 1. Source material goes to the input slot.
        player.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE_SLAB, 8));
        menu.clicked(FIRST_HOTBAR_SLOT, 0, ContainerInput.QUICK_MOVE, player);

        final ItemStack input = chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT);
        if (!input.is(Items.COBBLESTONE_SLAB) || input.getCount() != 8) {
            failures.add("shift-clicking 8 cobblestone slabs should have filled the input slot, found "
                    + ChronariumTestSupport.describe(input));
        }
        if (!chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT).isEmpty()) {
            failures.add("the slabs also reached the catalyst slot");
        }

        // 2. With the input occupied by a different item, the catalyst is the fallback.
        player.getInventory().setItem(0, new ItemStack(Items.VINE, 3));
        menu.clicked(FIRST_HOTBAR_SLOT, 0, ContainerInput.QUICK_MOVE, player);

        final ItemStack catalyst = chronarium.getItem(ChronariumBlockEntity.CATALYST_SLOT);
        if (!catalyst.is(Items.VINE) || catalyst.getCount() != 3) {
            failures.add("shift-clicking vines with the input already full should have filled the"
                    + " catalyst slot, found " + ChronariumTestSupport.describe(catalyst));
        }
        if (!chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT).is(Items.COBBLESTONE_SLAB)) {
            failures.add("the vines displaced the input slot's contents");
        }

        // 3. Shift-clicking a machine slot sends it back to the player.
        menu.clicked(ChronariumMenu.INPUT_SLOT, 0, ContainerInput.QUICK_MOVE, player);
        if (!chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT).isEmpty()) {
            failures.add("shift-clicking the input slot did not return it to the player, still holding "
                    + ChronariumTestSupport.describe(chronarium.getItem(ChronariumBlockEntity.INPUT_SLOT)));
        }
        if (!player.getInventory().contains(new ItemStack(Items.COBBLESTONE_SLAB))) {
            failures.add("the slabs did not come back into the player's inventory");
        }

        ChronariumTestSupport.failIfAny(helper, "Chronarium menu loading regression", failures);
        helper.succeed();
    }
}
