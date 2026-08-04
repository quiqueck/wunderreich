package de.ambertation.wunderreich.network;

/**
 * Each message registers itself with {@code NetworkRegistry} from its own {@code KEY}'s static initializer;
 * this just forces those classes to load.
 */
public class ClientBoundNetworkHandlers {
    public static void register() {
        ensureLoaded(SuctionTubeContainerUpdatePacket.KEY);
    }

    private static void ensureLoaded(Object key) {
    }
}
