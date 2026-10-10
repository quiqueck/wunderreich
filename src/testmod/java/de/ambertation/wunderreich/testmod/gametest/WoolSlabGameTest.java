package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Regression: {@code light_gray_wool_slab} was registered as a concrete slab - pickaxe tag instead of
 * wool, stone material and sound. It has to behave exactly like its wool siblings.
 */
public class WoolSlabGameTest {
    @GameTest
    public void theLightGrayWoolSlabIsAWoolSlab(GameTestHelper helper) {
        final Block lightGray = WunderreichSlabBlocks.LIGHT_GRAY_WOOL_SLAB;
        final Block reference = WunderreichSlabBlocks.GRAY_WOOL_SLAB;
        final BlockState state = lightGray.defaultBlockState();
        final BlockState refState = reference.defaultBlockState();
        final List<String> failures = new ArrayList<>();

        if (lightGray.getClass() != reference.getClass()) {
            failures.add("light_gray_wool_slab is a " + lightGray.getClass().getSimpleName()
                    + ", the other wool slabs are " + reference.getClass().getSimpleName());
        }
        if (!state.is(BlockTags.WOOL)) {
            failures.add("light_gray_wool_slab is not in #minecraft:wool");
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            failures.add("light_gray_wool_slab is in #minecraft:mineable/pickaxe");
        }
        if (state.getSoundType() != refState.getSoundType()) {
            failures.add("light_gray_wool_slab does not use the wool sound type");
        }
        if (state.getDestroySpeed(helper.getLevel(), helper.absolutePos(BlockPos.ZERO))
                != refState.getDestroySpeed(helper.getLevel(), helper.absolutePos(BlockPos.ZERO))) {
            failures.add("light_gray_wool_slab has a different hardness than the other wool slabs");
        }

        ChronariumTestSupport.failIfAny(helper, "light_gray_wool_slab regression", failures);
        helper.succeed();
    }
}
