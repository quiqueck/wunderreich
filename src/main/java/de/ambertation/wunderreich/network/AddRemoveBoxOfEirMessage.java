package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.blocks.BoxOfEirBlock;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock.LiveBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;


public class AddRemoveBoxOfEirMessage extends ServerBoundPacketHandler<AddRemoveBoxOfEirMessage.Content> {
    public static final AddRemoveBoxOfEirMessage INSTANCE = ServerBoundPacketHandler.register("box_of_eir",
            new AddRemoveBoxOfEirMessage());

    protected AddRemoveBoxOfEirMessage() {
    }

    protected static record Content(boolean didAdd, BlockPos pos, ServerLevel level) {
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

    private static void addedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);
        System.out.println("Adding " + pos + " " + BoxOfEirBlock.liveBlocks.contains(lb));
        BoxOfEirBlock.liveBlocks.add(lb);
        BoxOfEirBlock.updateNeighbours(level, pos);
    }

    private static void removedBox(ServerLevel level, BlockPos pos) {
        final LiveBlock lb = new LiveBlock(pos, level);
        System.out.println("Removing " + pos + " " + BoxOfEirBlock.liveBlocks.contains(lb));
        BoxOfEirBlock.liveBlocks.remove(lb);
        BoxOfEirBlock.updateNeighbours(level, pos);
    }

//    public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "box_of_eir");
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
