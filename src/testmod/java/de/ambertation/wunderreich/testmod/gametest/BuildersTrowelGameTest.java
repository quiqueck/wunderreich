package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.CellularTrowelPattern;
import de.ambertation.wunderreich.items.TrowelPattern;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Regressions: the builder's trowel had an attack speed modifier of -5, which on top of the player's
 * base attack speed of 4 showed up as "-1 Attack Speed" in the tooltip (and clamps to 0 in game).
 * The diamond trowel's block pattern used a noise seed from {@code Math.random()}, so the pattern
 * changed on every server start instead of being stable per world. Its patches were also mostly one
 * or two blocks big (about as mixed as random picks); they should be a few blocks across.
 * <p>
 * The diamond trowel now uses {@link CellularTrowelPattern}: the band pattern ({@link TrowelPattern})
 * always puts the block types in the same order, so bigger patches came out as rings and stripes.
 * The iron trowel now uses the band pattern (and the new wooden trowel places random blocks), so the
 * band pattern's tests now cover the iron trowel.
 */
public class BuildersTrowelGameTest {
    @GameTest
    public void trowelsHaveAPositiveAttackSpeed(GameTestHelper helper) {
        final List<String> failures = new ArrayList<>();
        for (Item trowel : trowels()) {
            if (trowel == null) continue;
            final ItemAttributeModifiers modifiers = new ItemStack(trowel).getOrDefault(
                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            final double speed = modifiers.compute(
                    Attributes.ATTACK_SPEED, Attributes.DEFAULT_ATTACK_SPEED, EquipmentSlot.MAINHAND);
            if (speed <= 0) {
                failures.add(trowel + " has an attack speed of " + speed + " in the main hand");
            }
        }
        ChronariumTestSupport.failIfAny(helper, "builder's trowel attack speed", failures);
        helper.succeed();
    }

    @GameTest
    public void trowelTiersAndPatterns(GameTestHelper helper) {
        final List<String> failures = new ArrayList<>();
        final List<Item> trowels = trowels();
        if (trowels.size() != 3) {
            failures.add("expected the wooden, iron and diamond trowel, got " + trowels);
        } else {
            for (int i = 1; i < trowels.size(); i++) {
                final int lower = new ItemStack(trowels.get(i - 1)).getMaxDamage();
                final int higher = new ItemStack(trowels.get(i)).getMaxDamage();
                if (lower >= higher) {
                    failures.add(trowels.get(i - 1) + " lasts " + lower + " uses, " + trowels.get(i) + " only " + higher);
                }
            }
            final BuildersTrowel.Pattern[] patterns = {
                    BuildersTrowel.Pattern.RANDOM, BuildersTrowel.Pattern.BANDS, BuildersTrowel.Pattern.CELLS
            };
            for (int i = 0; i < patterns.length; i++) {
                final BuildersTrowel.Pattern actual = ((BuildersTrowel) trowels.get(i)).pattern();
                if (actual != patterns[i]) {
                    failures.add(trowels.get(i) + " places " + actual + ", expected " + patterns[i]);
                }
            }
        }
        ChronariumTestSupport.failIfAny(helper, "builder's trowel tiers", failures);
        helper.succeed();
    }

    @GameTest
    public void trowelTooltipsNameTheirPattern(GameTestHelper helper) {
        final List<String> failures = new ArrayList<>();
        for (Item item : trowels()) {
            final BuildersTrowel trowel = (BuildersTrowel) item;
            final List<Component> lines = new ArrayList<>();
            trowel.appendHoverText(
                    new ItemStack(trowel), Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, lines::add,
                    TooltipFlag.Default.NORMAL
            );
            final String key = trowel.pattern().translationKey();
            final boolean found = lines.stream().anyMatch(line ->
                    line.getContents() instanceof TranslatableContents t && t.getKey().equals(key));
            if (!found) failures.add(trowel + " has no tooltip line " + key + ": " + lines);
        }
        // the server has no client language, so read the shipped lang files directly
        for (String lang : new String[]{"en_us", "de_de"}) {
            final String path = "/assets/wunderreich/lang/" + lang + ".json";
            try (InputStream in = BuildersTrowelGameTest.class.getResourceAsStream(path)) {
                if (in == null) {
                    failures.add(path + " is missing");
                    continue;
                }
                final JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                                                  .getAsJsonObject();
                for (BuildersTrowel.Pattern pattern : BuildersTrowel.Pattern.values()) {
                    if (!json.has(pattern.translationKey())) {
                        failures.add(lang + " does not translate " + pattern.translationKey());
                    }
                }
            } catch (IOException e) {
                failures.add(path + ": " + e);
            }
        }
        ChronariumTestSupport.failIfAny(helper, "builder's trowel tooltips", failures);
        helper.succeed();
    }

    @GameTest
    public void woodenTrowelIsCraftedFromAnyPlanks(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final List<String> failures = new ArrayList<>();
        // mixed planks: the recipe takes the wooden tool material tag, like vanilla's wooden tools
        final ItemStack oak = new ItemStack(Items.OAK_PLANKS);
        final ItemStack spruce = new ItemStack(Items.SPRUCE_PLANKS);
        final ItemStack cherry = new ItemStack(Items.CHERRY_PLANKS);
        final ItemStack stick = new ItemStack(Items.STICK);
        final ItemStack none = ItemStack.EMPTY;
        final CraftingInput input = CraftingInput.of(3, 3, List.of(
                oak, spruce, cherry,
                none, oak, none,
                none, stick, none
        ));
        final ItemStack result = level.getServer()
                                      .getRecipeManager()
                                      .getRecipeFor(RecipeType.CRAFTING, input, level)
                                      .map(recipe -> recipe.value().assemble(input))
                                      .orElse(ItemStack.EMPTY);
        if (!result.is(WunderreichItems.WOODEN_BUILDERS_TROWEL)) {
            failures.add("planks over a stick crafted " + ChronariumTestSupport.describe(result));
        }
        ChronariumTestSupport.failIfAny(helper, "wooden trowel recipe", failures);
        helper.succeed();
    }

    @GameTest
    public void ironTrowelPatternIsStablePerWorldSeed(GameTestHelper helper) {
        final RandomList<String> hotbar = new RandomList<>(9);
        hotbar.add("stone_bricks", 64);
        hotbar.add("cracked_stone_bricks", 32);
        hotbar.add("mossy_stone_bricks", 16);
        hotbar.add("andesite", 8);

        final long worldSeed = 1234567890123L;
        // two instances, as after a server restart
        final TrowelPattern first = TrowelPattern.forWorldSeed(worldSeed);
        final TrowelPattern afterRestart = TrowelPattern.forWorldSeed(worldSeed);
        final TrowelPattern otherWorld = TrowelPattern.forWorldSeed(worldSeed + 1);

        final List<String> failures = new ArrayList<>();
        int differentInOtherWorld = 0;
        int positions = 0;
        for (int x = -16; x < 16; x++) {
            for (int y = 60; y < 76; y++) {
                final BlockPos pos = new BlockPos(x * 7, y, 1000 - x);
                positions++;
                final String a = first.pick(hotbar, pos);
                final String b = afterRestart.pick(hotbar, pos);
                if (!a.equals(b) && failures.size() < 5) {
                    failures.add("same world seed picked " + a + " and " + b + " at " + pos.toShortString());
                }
                if (!a.equals(otherWorld.pick(hotbar, pos))) differentInOtherWorld++;
            }
        }
        // a different world should produce a clearly different pattern, not just a handful of changes
        if (differentInOtherWorld < positions / 10) {
            failures.add("another world seed changed only " + differentInOtherWorld + " of " + positions + " picks");
        }
        ChronariumTestSupport.failIfAny(helper, "iron trowel pattern", failures);
        helper.succeed();
    }

    @GameTest
    public void ironTrowelPatchesAreAFewBlocksAcross(GameTestHelper helper) {
        final TrowelPattern pattern = TrowelPattern.forWorldSeed(1234567890123L);
        final List<String> failures = new ArrayList<>();
        for (int types : new int[]{2, 4, 9}) {
            final RandomList<Integer> hotbar = new RandomList<>(9);
            for (int i = 0; i < types; i++) hotbar.add(i, 64);
            // 24x24 walls (x/y) and floors (x/z) at a few places, like a player would build them
            for (boolean floor : new boolean[]{false, true}) {
                double meanSum = 0;
                final int samples = 8;
                for (int s = 0; s < samples; s++) {
                    final int ox = s * 211 - 800, oy = 64 + s * 9, oz = 500 - s * 157;
                    final int[][] grid = new int[24][24];
                    for (int a = 0; a < 24; a++) {
                        for (int b = 0; b < 24; b++) {
                            final BlockPos pos = floor
                                    ? new BlockPos(ox + a, oy, oz + b)
                                    : new BlockPos(ox + a, oy + b, oz);
                            grid[a][b] = pattern.pick(hotbar, pos);
                        }
                    }
                    meanSum += meanRegionSize(grid);
                }
                final double mean = meanSum / samples;
                // the old pattern had ~1.9 blocks per patch with four types (random picks: ~1.6);
                // floors come out a bit bigger than walls (the vertical frequency is higher)
                final double max = floor ? 14 : 10;
                if (mean < 4 || mean > max) {
                    failures.add(String.format(
                            "%d types on a %s: mean patch size %.2f blocks, expected 4..%.0f",
                            types, floor ? "floor" : "wall", mean, max
                    ));
                }
            }
        }
        ChronariumTestSupport.failIfAny(helper, "iron trowel patch size", failures);
        helper.succeed();
    }

    @GameTest
    public void diamondTrowelPatternIsStablePerWorldSeed(GameTestHelper helper) {
        final RandomList<String> hotbar = new RandomList<>(9);
        hotbar.add("stone_bricks", 64);
        hotbar.add("cracked_stone_bricks", 32);
        hotbar.add("mossy_stone_bricks", 16);
        hotbar.add("andesite", 8);

        final long worldSeed = 1234567890123L;
        // two instances, as after a server restart
        final CellularTrowelPattern first = CellularTrowelPattern.forWorldSeed(worldSeed);
        final CellularTrowelPattern afterRestart = CellularTrowelPattern.forWorldSeed(worldSeed);
        final CellularTrowelPattern otherWorld = CellularTrowelPattern.forWorldSeed(worldSeed + 1);

        final List<String> failures = new ArrayList<>();
        int differentInOtherWorld = 0;
        int positions = 0;
        for (int x = -16; x < 16; x++) {
            for (int y = 60; y < 76; y++) {
                final BlockPos pos = new BlockPos(x * 7, y, 1000 - x);
                positions++;
                final String a = first.pick(hotbar, pos);
                final String b = afterRestart.pick(hotbar, pos);
                if (!a.equals(b) && failures.size() < 5) {
                    failures.add("same world seed picked " + a + " and " + b + " at " + pos.toShortString());
                }
                if (!a.equals(otherWorld.pick(hotbar, pos))) differentInOtherWorld++;
            }
        }
        if (differentInOtherWorld < positions / 10) {
            failures.add("another world seed changed only " + differentInOtherWorld + " of " + positions + " picks");
        }
        ChronariumTestSupport.failIfAny(helper, "diamond trowel pattern", failures);
        helper.succeed();
    }

    @GameTest
    public void diamondTrowelPatchesAreAFewBlocksAcross(GameTestHelper helper) {
        final CellularTrowelPattern pattern = CellularTrowelPattern.forWorldSeed(1234567890123L);
        final List<String> failures = new ArrayList<>();
        for (int types : new int[]{2, 4, 9}) {
            final RandomList<Integer> hotbar = equalHotbar(types);
            for (boolean floor : new boolean[]{false, true}) {
                final double mean = meanRegionSize(floor, pattern::pick, hotbar);
                // about 6 blocks for 4 and 9 types (random picks: ~1.6); two types always form
                // winding, connected areas, so they come out bigger (~12)
                final double min = types == 2 ? 6 : 4.5;
                final double max = types == 2 ? 16 : 9;
                if (mean < min || mean > max) {
                    failures.add(String.format(
                            "%d types on a %s: mean patch size %.2f blocks, expected %.1f..%.0f",
                            types, floor ? "floor" : "wall", mean, min, max
                    ));
                }
            }
        }
        ChronariumTestSupport.failIfAny(helper, "diamond trowel patch size", failures);
        helper.succeed();
    }

    /**
     * In the band pattern a block type only ever touches the types next to it in the hotbar (the
     * bands follow each other in hotbar order), which is what draws the rings. Cells pick their type
     * independently, so every pair of types meets about equally often.
     */
    @GameTest
    public void diamondTrowelPatchesHaveNoRings(GameTestHelper helper) {
        final long worldSeed = 1234567890123L;
        final CellularTrowelPattern cells = CellularTrowelPattern.forWorldSeed(worldSeed);
        final TrowelPattern bands = TrowelPattern.forWorldSeed(worldSeed);
        final List<String> failures = new ArrayList<>();
        for (int types : new int[]{4, 9}) {
            final RandomList<Integer> hotbar = equalHotbar(types);
            // share of the type pairs that are not next to each other in the hotbar (4 types: 2 of 6)
            final double expected = (double) (types * (types - 1) / 2 - types) / (types * (types - 1) / 2);
            for (boolean floor : new boolean[]{false, true}) {
                final double cellShare = farContactShare(types, floor, cells::pick, hotbar);
                final double bandShare = farContactShare(types, floor, bands::pick, hotbar);
                final String where = String.format("%d types on a %s", types, floor ? "floor" : "wall");
                if (cellShare < expected * 0.75) {
                    failures.add(String.format(
                            "%s: %.1f%% of the borders are between types that are not hotbar neighbours, expected about %.1f%%",
                            where, cellShare * 100, expected * 100
                    ));
                }
                // the metric has to see the rings of the band pattern, or it proves nothing
                if (bandShare > expected * 0.4) {
                    failures.add(String.format(
                            "%s: the band pattern scores %.1f%%, the metric no longer tells rings apart",
                            where, bandShare * 100
                    ));
                }
            }
        }
        ChronariumTestSupport.failIfAny(helper, "diamond trowel rings", failures);
        helper.succeed();
    }

    @GameTest
    public void diamondTrowelKeepsStackWeights(GameTestHelper helper) {
        final CellularTrowelPattern pattern = CellularTrowelPattern.forWorldSeed(1234567890123L);
        final RandomList<Integer> hotbar = new RandomList<>(9);
        final int[] weights = {64, 32, 16, 8};
        for (int i = 0; i < weights.length; i++) hotbar.add(i, weights[i]);

        // 5 blocks apart, so nearly every sample lands in a cell of its own
        final int[] counts = new int[weights.length];
        int samples = 0;
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 20; z++) {
                    counts[pattern.pick(hotbar, new BlockPos(x * 5 - 50, y * 5 - 30, z * 5 + 300))]++;
                    samples++;
                }
            }
        }
        final List<String> failures = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            final double share = (double) counts[i] / samples;
            final double expected = weights[i] / 120.0;
            if (Math.abs(share - expected) > 0.025) {
                failures.add(String.format(
                        "a stack of %d got %.1f%% of the blocks, expected %.1f%%",
                        weights[i], share * 100, expected * 100
                ));
            }
        }
        ChronariumTestSupport.failIfAny(helper, "diamond trowel stack weights", failures);
        helper.succeed();
    }

    /** The trowels that are enabled, from the lowest tier up. */
    private static List<Item> trowels() {
        final List<Item> trowels = new ArrayList<>(3);
        for (Item trowel : new Item[]{
                WunderreichItems.WOODEN_BUILDERS_TROWEL,
                WunderreichItems.BUILDERS_TROWEL,
                WunderreichItems.DIAMOND_BUILDERS_TROWEL
        }) {
            if (trowel != null) trowels.add(trowel);
        }
        return trowels;
    }

    private static RandomList<Integer> equalHotbar(int types) {
        final RandomList<Integer> hotbar = new RandomList<>(9);
        for (int i = 0; i < types; i++) hotbar.add(i, 64);
        return hotbar;
    }

    /** 24x24 walls (x/y) or floors (x/z) at a few places, like a player would build them. */
    private static List<int[][]> builds(
            boolean floor,
            BiFunction<RandomList<Integer>, BlockPos, Integer> pick,
            RandomList<Integer> hotbar
    ) {
        final List<int[][]> grids = new ArrayList<>();
        for (int s = 0; s < 8; s++) {
            final int ox = s * 211 - 800, oy = 64 + s * 9, oz = 500 - s * 157;
            final int[][] grid = new int[24][24];
            for (int a = 0; a < 24; a++) {
                for (int b = 0; b < 24; b++) {
                    final BlockPos pos = floor
                            ? new BlockPos(ox + a, oy, oz + b)
                            : new BlockPos(ox + a, oy + b, oz);
                    grid[a][b] = pick.apply(hotbar, pos);
                }
            }
            grids.add(grid);
        }
        return grids;
    }

    private static double meanRegionSize(
            boolean floor,
            BiFunction<RandomList<Integer>, BlockPos, Integer> pick,
            RandomList<Integer> hotbar
    ) {
        final List<int[][]> grids = builds(floor, pick, hotbar);
        double sum = 0;
        for (int[][] grid : grids) sum += meanRegionSize(grid);
        return sum / grids.size();
    }

    /** Of all borders between two different types, the share between types not next to each other in the hotbar. */
    private static double farContactShare(
            int types,
            boolean floor,
            BiFunction<RandomList<Integer>, BlockPos, Integer> pick,
            RandomList<Integer> hotbar
    ) {
        int borders = 0, far = 0;
        for (int[][] grid : builds(floor, pick, hotbar)) {
            for (int a = 0; a < grid.length; a++) {
                for (int b = 0; b < grid[0].length; b++) {
                    for (int[] d : new int[][]{{1, 0}, {0, 1}}) {
                        final int na = a + d[0], nb = b + d[1];
                        if (na >= grid.length || nb >= grid[0].length) continue;
                        final int diff = Math.abs(grid[a][b] - grid[na][nb]);
                        if (diff == 0) continue;
                        borders++;
                        if (diff != 1 && diff != types - 1) far++;
                    }
                }
            }
        }
        return borders == 0 ? 0 : (double) far / borders;
    }

    /** Mean size of the 4-connected same-value regions. */
    private static double meanRegionSize(int[][] grid) {
        final int w = grid.length, h = grid[0].length;
        final boolean[][] seen = new boolean[w][h];
        int regions = 0;
        final ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (int a = 0; a < w; a++) {
            for (int b = 0; b < h; b++) {
                if (seen[a][b]) continue;
                regions++;
                seen[a][b] = true;
                queue.add(new int[]{a, b});
                while (!queue.isEmpty()) {
                    final int[] p = queue.poll();
                    for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        final int na = p[0] + d[0], nb = p[1] + d[1];
                        if (na < 0 || nb < 0 || na >= w || nb >= h || seen[na][nb]) continue;
                        if (grid[na][nb] != grid[p[0]][p[1]]) continue;
                        seen[na][nb] = true;
                        queue.add(new int[]{na, nb});
                    }
                }
            }
        }
        return (double) (w * h) / regions;
    }
}
