package de.ambertation.wunderreich.testmod.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

/**
 * Minimal smoke test for the 1.20.1 testmod: Wunderreich registered blocks on a dedicated server, and one of
 * them can actually be placed and read back in a level.
 */
public class BlockRegistrationGameTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void blocksAreRegisteredAndPlaceable(GameTestHelper helper) {
        final ResourceLocation probeId = BuiltInRegistries.BLOCK.keySet()
                                                                .stream()
                                                                .filter(id -> id.getNamespace().equals("wunderreich"))
                                                                .sorted()
                                                                .findFirst()
                                                                .orElse(null);
        helper.assertTrue(probeId != null, "expected Wunderreich to register at least one block");

        final Block probe = BuiltInRegistries.BLOCK.get(probeId);
        final BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, probe);
        helper.assertBlockPresent(probe, pos);
        helper.succeed();
    }
}
