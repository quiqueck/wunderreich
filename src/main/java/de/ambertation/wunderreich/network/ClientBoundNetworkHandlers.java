package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ClientBoundPacketHandler;

public class ClientBoundNetworkHandlers {
    public static void register() {
        ClientBoundPacketHandler.register(SuctionTubeContainerUpdatePacket.HANDLER);
    }
}
