package de.ambertation.wunderreich.network;

/**
 * Each message registers itself with {@code NetworkRegistry} from its own {@code KEY}'s static initializer;
 * this just forces those classes to load.
 */
public abstract class ServerBoundNetworkHandlers {
    public static void register() {
        ensureLoaded(AddRemoveWunderKisteMessage.KEY);
        ensureLoaded(CycleTradesMessage.KEY);
        ensureLoaded(SelectWhisperMessage.KEY);
    }

    private static void ensureLoaded(Object key) {
    }
}
