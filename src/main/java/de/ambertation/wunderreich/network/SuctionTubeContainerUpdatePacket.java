package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ClientBoundMessage;
import de.ambertation.wunderlib.network.NetworkRegistry;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record SuctionTubeContainerUpdatePacket(Map<Direction, ItemStack> connections) {
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Direction, ItemStack>> CONNECTIONS_CODEC =
            ByteBufCodecs.map(HashMap::new, Direction.STREAM_CODEC, ItemStack.STREAM_CODEC);

    private static final StreamCodec<RegistryFriendlyByteBuf, SuctionTubeContainerUpdatePacket> CODEC =
            CONNECTIONS_CODEC.map(SuctionTubeContainerUpdatePacket::new, SuctionTubeContainerUpdatePacket::connections);

    public static final ClientBoundMessage<SuctionTubeContainerUpdatePacket> KEY = NetworkRegistry.registerClientBound(
            Wunderreich.ID("suction_container"),
            CODEC
    );

    public static void send(ServerPlayer serverPlayer, Map<Direction, ItemStack> connections) {
        NetworkRegistry.sendToClient(serverPlayer, KEY, new SuctionTubeContainerUpdatePacket(connections));
    }
}
