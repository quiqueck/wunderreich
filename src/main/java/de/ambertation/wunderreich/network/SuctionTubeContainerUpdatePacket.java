package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ClientBoundNetworkPayload;
import de.ambertation.wunderlib.network.ClientBoundPacketHandler;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.util.EnumMap;
import java.util.Map;

public class SuctionTubeContainerUpdatePacket extends ClientBoundNetworkPayload<SuctionTubeContainerUpdatePacket> {
    public static final ClientBoundPacketHandler<SuctionTubeContainerUpdatePacket> HANDLER = new ClientBoundPacketHandler<>(
            Wunderreich.ID("suction_container"),
            SuctionTubeContainerUpdatePacket::new
    );


    public final Map<Direction, ItemStack> connections;

    protected SuctionTubeContainerUpdatePacket(RegistryFriendlyByteBuf buf) {
        super(HANDLER);

        int size = buf.readInt();
        this.connections = new EnumMap<>(Direction.class);
        for (int i = 0; i < size; i++) {
            Direction direction = buf.readEnum(Direction.class);
            this.connections.put(direction, ItemStack.STREAM_CODEC.decode(buf));
        }
    }

    protected SuctionTubeContainerUpdatePacket(Map<Direction, ItemStack> connections) {
        super(HANDLER);
        this.connections = connections;
    }

    public static void send(ServerPlayer serverPlayer, Map<Direction, ItemStack> connections) {
        ClientBoundPacketHandler.sendToClient(serverPlayer, new SuctionTubeContainerUpdatePacket(connections));
    }


    @Override
    protected void prepareOnServer(ServerPlayer player) {

    }


    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.connections.size());
        for (var entry : this.connections.entrySet()) {
            buf.writeEnum(entry.getKey());
            ItemStack.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void processOnClient(PacketSender responseSender) {

    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void processOnGameThread(Minecraft client) {
        if (client.player == null) {
            Wunderreich.LOGGER.warn("Received SuctionTubeContainerUpdatePacket but player is null.");
            return;
        }

        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu instanceof SuctionTubeMenu suctionTubeMenu) {
            suctionTubeMenu.updateContainerConnections(connections);
        }
    }
}
