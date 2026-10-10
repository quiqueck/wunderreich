package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Recomputing the "is any box of this network open" state with a named Wunderkiste whose network was
 * never looked up before.
 * <p>
 * Regression: the open state is reset for every <em>known</em> network and then read back per box. A
 * named network's ID is only registered when something first asks for it, which right after a server
 * start happens inside that loop - the read returned {@code null} and the unboxing NPE crashed the
 * server ("Exception ticking world") on every start of a world with a named Wunderkiste that was
 * opened before (the lid recheck is a persisted scheduled tick).
 */
public class WunderKisteNamedOpenStateGameTest {
    private static final BlockPos KISTE = new BlockPos(2, 2, 2);

    @GameTest
    public void updatingOpenStateWithANeverSeenNamedNetworkDoesNotCrash(GameTestHelper helper) {
        helper.setBlock(KISTE, WunderreichBlocks.WUNDER_KISTE);
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(KISTE));
        if (!(be instanceof WunderKisteBlockEntity kiste)) {
            throw helper.assertionException(Component.literal("no WunderKisteBlockEntity at " + KISTE + " (found " + be + ")"));
        }
        // a name no other code has asked for yet, so its network ID is not registered
        kiste.setCustomName(Component.literal("never-seen-" + helper.getLevel().getGameTime() + "-" + System.nanoTime()));

        try {
            // triggered through another (plain, colour) network, the way any box's lid recheck does it
            WunderKisteBlock.updateAllBoxes(
                    WunderKisteBlock.getContainer(WunderreichBlocks.WUNDER_KISTE.defaultBlockState(), null, helper.getLevel()),
                    true,
                    false
            );
        } catch (RuntimeException e) {
            throw helper.assertionException(Component.literal("updating the open state crashed: " + e));
        }
        helper.succeed();
    }
}
