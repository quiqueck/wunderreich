package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public abstract class ServerBoundPacketHandler<D> {
    protected ResourceLocation CHANNEL;

    public static <T extends ServerBoundPacketHandler> T register(String channel, T packetHandler) {
        packetHandler.CHANNEL = Wunderreich.ID(channel);
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(
                    handler,
                    packetHandler.CHANNEL,
                    (_server, _player, _handler, _buf, _responseSender) -> {
                        packetHandler.receiveOnServer(
                                _server,
                                _player,
                                _handler,
                                _buf,
                                _responseSender
                        );
                    }
            );
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayNetworking.unregisterReceiver(handler, packetHandler.CHANNEL);
        });

        return packetHandler;
    }

    public static void register() {
        AddRemoveWunderKisteMessage.INSTANCE.onRegister();
        CycleTradesMessage.INSTANCE.onRegister();
        SelectWhisperMessage.INSTANCE.onRegister();
        ChangedSDFMessage.INSTANCE.onRegister();
        ChangedTargetBlockMessage.INSTANCE.onRegister();
        UpdateSDFTransformMessage.INSTANCE.onRegister();
    }

    public void sendToServer(D content) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            _sendToServer(content);
        } else {
            //
        }
    }

    private void _sendToServer(D content) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        serializeOnClient(buf, content);
        ClientPlayNetworking.send(CHANNEL, buf);
    }

    void receiveOnServer(
            MinecraftServer server,
            ServerPlayer player,
            ServerGamePacketListenerImpl handler,
            FriendlyByteBuf buf,
            PacketSender responseSender
    ) {
        D content = deserializeOnServer(buf, player, responseSender);
        server.execute(() -> processOnGameThread(server, player, content));
    }

    protected abstract void serializeOnClient(FriendlyByteBuf buf, D content);

    protected abstract D deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender);

    protected abstract void processOnGameThread(MinecraftServer server, ServerPlayer player, D content);

    protected void onRegister() {
    }
}
