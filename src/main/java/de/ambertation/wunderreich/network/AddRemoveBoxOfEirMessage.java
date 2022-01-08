package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock.LiveBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class AddRemoveBoxOfEirMessage {
    public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "box_of_eir");

    public static void register() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, CHANNEL, (_server, _player, _handler, _buf, _responseSender) -> {
                boolean didAdd = _buf.readBoolean();
                BlockPos pos = _buf.readBlockPos();
                ServerLevel level = _player.getLevel();

                if (didAdd) addedBox(level, pos);
                else removedBox(level, pos);
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayNetworking.unregisterReceiver(handler, CHANNEL);
        });
    }

    public static void send(boolean didAdd, BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(didAdd);
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(CHANNEL, buf);
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
}
