package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.network.AddRemoveWunderKisteMessage;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.LiveBlockManager;
import de.ambertation.wunderreich.utils.LiveBlockManager.LiveBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * The server-side list of Wunderkisten, which force-loads the chunks around every entry.
 * <p>
 * Regressions: the client's add/remove message was applied without any check, so a client could
 * force-load arbitrary chunks (or deregister real boxes), and entries for positions that no longer
 * hold a Wunderkiste were never cleaned up and kept their chunks loaded forever.
 */
public class WunderKisteTrackingGameTest {
    private static final BlockPos KISTE = new BlockPos(2, 2, 2);
    private static final BlockPos STONE = new BlockPos(4, 2, 2);
    private static final BlockPos PLAYER = new BlockPos(2, 2, 5);

    @GameTest
    public void clientRequestsAreValidatedOnTheServer(GameTestHelper helper) {
        final LiveBlockManager<LiveBlock> manager = WunderKisteBlock.getLiveBlockManager();
        helper.setBlock(KISTE, WunderreichBlocks.WUNDER_KISTE);
        helper.setBlock(STONE, Blocks.STONE);
        final ServerPlayer player = ChronariumTestSupport.menuPlayer(helper, PLAYER);
        final BlockPos kiste = helper.absolutePos(KISTE);
        final BlockPos stone = helper.absolutePos(STONE);
        final BlockPos farAway = kiste.offset(100_000, 0, 100_000);
        final List<String> failures = new ArrayList<>();

        if (!manager.contains(live(helper, kiste))) {
            failures.add("placing a Wunderkiste did not register it on the server");
        }

        // adding a position that holds no Wunderkiste
        if (new AddRemoveWunderKisteMessage(true, stone).handleFromClient(player) || manager.contains(live(helper, stone))) {
            failures.add("a client could register a stone block as a Wunderkiste");
        }

        // adding a far away, unloaded position must neither register it nor load its chunk
        if (new AddRemoveWunderKisteMessage(true, farAway).handleFromClient(player) || manager.contains(live(helper, farAway))) {
            failures.add("a client could register a far away position as a Wunderkiste");
        }
        if (helper.getLevel().isLoaded(farAway)) {
            failures.add("handling a request for a far away position loaded its chunk");
        }

        // removing a box that is still there
        if (new AddRemoveWunderKisteMessage(false, kiste).handleFromClient(player) || !manager.contains(live(helper, kiste))) {
            failures.add("a client could deregister a Wunderkiste that is still in the world");
        }

        // the legacy path still works: a real, untracked box near the player gets added
        manager.remove(live(helper, kiste));
        if (!new AddRemoveWunderKisteMessage(true, kiste).handleFromClient(player) || !manager.contains(live(helper, kiste))) {
            failures.add("a valid client request for an untracked Wunderkiste was rejected");
        }

        // 1.21.x has no runBeforeTestEnd hook, so the mock player is removed by hand (see menuPlayer)
        ChronariumTestSupport.removePlayer(helper, player);
        ChronariumTestSupport.failIfAny(helper, "Wunderkiste client request validation", failures);
        helper.succeed();
    }

    @GameTest
    public void entriesWithoutAWunderkisteArePruned(GameTestHelper helper) {
        final LiveBlockManager<LiveBlock> manager = WunderKisteBlock.getLiveBlockManager();
        helper.setBlock(KISTE, WunderreichBlocks.WUNDER_KISTE);
        helper.setBlock(STONE, Blocks.STONE);
        final LiveBlock kiste = live(helper, helper.absolutePos(KISTE));
        final LiveBlock stale = live(helper, helper.absolutePos(STONE));
        final List<String> failures = new ArrayList<>();

        // what older versions left behind: an entry for a block that is not a Wunderkiste
        manager.add(stale);
        AddRemoveWunderKisteMessage.pruneStaleBoxes(helper.getLevel().getServer());

        if (manager.contains(stale)) {
            failures.add("an entry for a stone block survived pruning and keeps its chunk loaded");
        }
        if (!manager.contains(kiste)) {
            failures.add("pruning removed the entry of a real Wunderkiste");
        }

        ChronariumTestSupport.failIfAny(helper, "Wunderkiste stale entry pruning", failures);
        helper.succeed();
    }

    private static LiveBlock live(GameTestHelper helper, BlockPos absolutePos) {
        return new LiveBlock(absolutePos, helper.getLevel());
    }
}
