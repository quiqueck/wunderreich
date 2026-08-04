package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.NetworkRegistry;
import de.ambertation.wunderlib.network.ServerBoundMessage;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.utils.LiveBlockManager.LiveBlock;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record AddRemoveWunderKisteMessage(boolean didAdd, BlockPos pos) {
    public static final ServerBoundMessage<AddRemoveWunderKisteMessage> KEY = NetworkRegistry.registerServerBound(
            Wunderreich.ID("wunder_kiste"),
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AddRemoveWunderKisteMessage::didAdd,
                    BlockPos.STREAM_CODEC, AddRemoveWunderKisteMessage::pos,
                    AddRemoveWunderKisteMessage::new
            ),
            (msg, ctx) -> {
                final ServerLevel level = ctx.player().level();
                if (msg.didAdd) addedBox(level, msg.pos);
                else removedBox(level, msg.pos);
            }
    );

    static {
        WunderKisteBlock.getLiveBlockManager().onChangeAt(WunderKisteBlock::updateNeighbours);
    }

    public static void addedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);
        final BlockState state = level.getBlockState(pos);
        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        WunderKisteDomain domain = WunderKisteServerExtension.getDomain(state);
        boolean result = WunderKisteBlock.getLiveBlockManager().add(lb);

        Wunderreich.LOGGER.info("Adding WunderKiste at " + pos + " (wasManaged: " + wasManaged + ", domain: " + domain + ", didAdd:" + result + ")");
    }

    public static void removedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);

        boolean wasManaged = WunderKisteBlock.getLiveBlockManager().contains(lb);
        boolean result = WunderKisteBlock.getLiveBlockManager().remove(lb);

        Wunderreich.LOGGER.info("Removing WunderKiste at " + pos + " (wasManaged: " + wasManaged + ", didRemove:" + result + ")");
    }

    public static void send(boolean didAdd, BlockPos pos) {
        NetworkRegistry.sendToServer(KEY, new AddRemoveWunderKisteMessage(didAdd, pos));
    }
}
