package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.NetworkRegistry;
import de.ambertation.wunderlib.network.ServerBoundMessage;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.utils.LiveBlockManager;
import de.ambertation.wunderreich.utils.LiveBlockManager.LiveBlock;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public record AddRemoveWunderKisteMessage(boolean didAdd, BlockPos pos) {
    public static final ServerBoundMessage<AddRemoveWunderKisteMessage> KEY = NetworkRegistry.registerServerBound(
            Wunderreich.ID("wunder_kiste"),
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AddRemoveWunderKisteMessage::didAdd,
                    BlockPos.STREAM_CODEC, AddRemoveWunderKisteMessage::pos,
                    AddRemoveWunderKisteMessage::new
            ),
            (msg, ctx) -> msg.handleFromClient(ctx.player())
    );

    static {
        WunderKisteBlock.getLiveBlockManager().onChangeAt(WunderKisteBlock::updateNeighbours);
    }

    /**
     * Handles the (legacy) client request. The server tracks boxes itself when they are placed or
     * broken; the client message only exists to pick up boxes in old worlds that were placed before
     * that tracking existed. It is never trusted: every tracked box force-loads chunks, so a request
     * is only honoured for a position in an already loaded chunk near the sender that actually holds
     * (add) or no longer holds (remove) a Wunderkiste.
     *
     * @return whether the request was accepted
     */
    public boolean handleFromClient(ServerPlayer player) {
        final ServerLevel level = player.level();
        final BlockPos target = pos.immutable();
        if (!level.isLoaded(target)) return false;

        final int maxChunkDistance = level.getServer().getPlayerList().getViewDistance() + 1;
        if (player.chunkPosition().getChessboardDistance(ChunkPos.containing(target)) > maxChunkDistance) {
            Wunderreich.LOGGER.debug("Ignoring Wunderkiste request from " + player.getName().getString() + " for far away " + target);
            return false;
        }

        if (didAdd != isWunderKisteAt(level, target)) {
            Wunderreich.LOGGER.debug("Ignoring invalid Wunderkiste request from " + player.getName().getString() + " (add: " + didAdd + ", " + target + ")");
            return false;
        }

        if (didAdd) addedBox(level, target);
        else removedBox(level, target);
        return true;
    }

    public static boolean isWunderKisteAt(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof WunderKisteBlock;
    }

    public static void addedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos.immutable(), level);
        final BlockState state = level.getBlockState(pos);
        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        WunderKisteDomain domain = WunderKisteServerExtension.getDomain(state);
        boolean result = WunderKisteBlock.getLiveBlockManager().add(lb);

        if (result) {
            Wunderreich.LOGGER.info("Adding WunderKiste at " + pos + " (domain: " + domain + ")");
        } else {
            Wunderreich.LOGGER.debug("WunderKiste at " + pos + " already managed (wasManaged: " + wasManaged + ", domain: " + domain + ")");
        }
    }

    public static void removedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos.immutable(), level);

        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        boolean result = WunderKisteBlock.getLiveBlockManager().remove(lb);

        if (result) {
            Wunderreich.LOGGER.info("Removing WunderKiste at " + pos);
        } else {
            Wunderreich.LOGGER.debug("WunderKiste at " + pos + " was not managed (wasManaged: " + wasManaged + ")");
        }
    }

    /**
     * Drops tracked positions that no longer hold a Wunderkiste (e.g. entries left behind by older
     * versions), so they stop force-loading their chunks. Only positions in loaded chunks are checked;
     * since tracked positions keep their own chunks loaded, every entry gets checked eventually.
     *
     * @return the number of removed entries
     */
    public static int pruneStaleBoxes(MinecraftServer server) {
        final LiveBlockManager<LiveBlock> manager = WunderKisteBlock.getLiveBlockManager();
        final List<LiveBlock> stale = new ArrayList<>();
        manager.forEach(lb -> {
            if (lb.getLevel() instanceof ServerLevel level
                    && level.getServer() == server
                    && level.isLoaded(lb.pos)
                    && !isWunderKisteAt(level, lb.pos)) {
                stale.add(lb);
            }
        });

        for (LiveBlock lb : stale) {
            Wunderreich.LOGGER.info("Removing stale WunderKiste entry at " + lb + ": there is no Wunderkiste anymore");
            manager.remove(lb);
        }
        return stale.size();
    }

    public static void send(boolean didAdd, BlockPos pos) {
        NetworkRegistry.sendToServer(KEY, new AddRemoveWunderKisteMessage(didAdd, pos.immutable()));
    }
}
