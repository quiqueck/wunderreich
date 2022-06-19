package de.ambertation.wunderreich.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import org.quiltmc.qsl.networking.api.PacketSender;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.utils.LiveBlockManager.LiveBlock;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;


public class AddRemoveWunderKisteMessage extends ServerBoundPacketHandler<AddRemoveWunderKisteMessage.Content> {
    public static final AddRemoveWunderKisteMessage INSTANCE = ServerBoundPacketHandler.register(
            "wunder_kiste",
            new AddRemoveWunderKisteMessage()
    );

    static {
        WunderKisteBlock.getLiveBlockManager().onChangeAt(WunderKisteBlock::updateNeighbours);
    }


    protected AddRemoveWunderKisteMessage() {
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

    public void send(boolean didAdd, BlockPos pos) {
        this.sendToServer(new Content(didAdd, pos, null));
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {
        buf.writeBoolean(content.didAdd);
        buf.writeBlockPos(content.pos);
    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        boolean didAdd = buf.readBoolean();
        BlockPos pos = buf.readBlockPos();
        ServerLevel level = player.getLevel();
        return new Content(didAdd, pos, level);

    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        if (content.didAdd) addedBox(content.level, content.pos);
        else removedBox(content.level, content.pos);
    }

    protected record Content(boolean didAdd, BlockPos pos, ServerLevel level) {
    }

//    public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "wunder_kiste");
//
//    public static void register() {
//        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
//            ServerPlayNetworking.registerReceiver(handler,
//                    CHANNEL,
//                    (_server, _player, _handler, _buf, _responseSender) -> {
//                        boolean didAdd = _buf.readBoolean();
//                        BlockPos pos = _buf.readBlockPos();
//                        ServerLevel level = _player.getLevel();
//
//                        if (didAdd) addedBox(level, pos);
//                        else removedBox(level, pos);
//                    });
//        });
//
//        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
//            ServerPlayNetworking.unregisterReceiver(handler, CHANNEL);
//        });
//    }
//
//    public static void send(boolean didAdd, BlockPos pos) {
//        FriendlyByteBuf buf = PacketByteBufs.create();
//        buf.writeBoolean(didAdd);
//        buf.writeBlockPos(pos);
//        ClientPlayNetworking.send(CHANNEL, buf);
//    }
}
