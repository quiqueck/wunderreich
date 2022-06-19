package de.ambertation.wunderreich.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.api.EnvType;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import de.ambertation.wunderreich.Wunderreich;

public abstract class ServerBoundPacketHandler<D> {
    protected ResourceLocation CHANNEL;

    public static <T extends ServerBoundPacketHandler> T register(String channel, T packetHandler) {
        packetHandler.CHANNEL = Wunderreich.ID(channel);
        org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            org.quiltmc.qsl.networking.api.ServerPlayNetworking.registerReceiver(
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
    }

    public void sendToServer(D content) {
        if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
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

    protected abstract D deserializeOnServer(
            FriendlyByteBuf buf,
            ServerPlayer player,
            org.quiltmc.qsl.networking.api.PacketSender responseSender
    );

    protected abstract void processOnGameThread(MinecraftServer server, ServerPlayer player, D content);

    protected void onRegister() {
    }
}
