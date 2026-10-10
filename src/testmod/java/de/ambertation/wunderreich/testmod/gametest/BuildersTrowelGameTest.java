package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.items.TrowelPattern;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Regressions: the builder's trowel had an attack speed modifier of -5, which on top of the player's
 * base attack speed of 4 showed up as "-1 Attack Speed" in the tooltip (and clamps to 0 in game).
 * The diamond trowel's block pattern used a noise seed from {@code Math.random()}, so the pattern
 * changed on every server start instead of being stable per world. Its patches were also mostly one
 * or two blocks big (about as mixed as random picks); they should be a few blocks across.
 */
public class BuildersTrowelGameTest {
    @GameTest
    public void trowelsHaveAPositiveAttackSpeed(GameTestHelper helper) {
        final List<String> failures = new ArrayList<>();
        for (Item trowel : List.of(WunderreichItems.BUILDERS_TROWEL, WunderreichItems.DIAMOND_BUILDERS_TROWEL)) {
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
    public void diamondTrowelPatternIsStablePerWorldSeed(GameTestHelper helper) {
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
        ChronariumTestSupport.failIfAny(helper, "diamond trowel pattern", failures);
        helper.succeed();
    }

    @GameTest
    public void diamondTrowelPatchesAreAFewBlocksAcross(GameTestHelper helper) {
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
        ChronariumTestSupport.failIfAny(helper, "diamond trowel patch size", failures);
        helper.succeed();
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
