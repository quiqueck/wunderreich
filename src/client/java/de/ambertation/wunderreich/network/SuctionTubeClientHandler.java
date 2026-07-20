package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ClientBoundHandler;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class SuctionTubeClientHandler implements ClientBoundHandler<SuctionTubeContainerUpdatePacket> {
    @Override
    public void processOnClient(SuctionTubeContainerUpdatePacket payload, PacketSender responseSender) {

    }

    @Override
    public void processOnGameThread(SuctionTubeContainerUpdatePacket payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            Wunderreich.LOGGER.warn("Received SuctionTubeContainerUpdatePacket but player is null.");
            return;
        }

        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu instanceof SuctionTubeMenu suctionTubeMenu) {
            suctionTubeMenu.updateContainerConnections(payload.connections);
        }
    }
}
