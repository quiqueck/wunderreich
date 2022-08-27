package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;

public abstract class ServerBoundPacketHandler<D> extends de.ambertation.lib.network.ServerBoundPacketHandler<D> {
    public static <D, T extends de.ambertation.lib.network.ServerBoundPacketHandler<D>> T register(
            String channel,
            T packetHandler
    ) {
        return de.ambertation.lib.network.ServerBoundPacketHandler.register(Wunderreich.ID(channel), packetHandler);
    }

    public static void register() {
        AddRemoveWunderKisteMessage.INSTANCE.onRegister();
        CycleTradesMessage.INSTANCE.onRegister();
        SelectWhisperMessage.INSTANCE.onRegister();
        ChangedSDFMessage.INSTANCE.onRegister();
        ChangedTargetBlockMessage.INSTANCE.onRegister();
    }
}
