package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.PoiWrapper;

public class WunderreichPoi {
    public static final PoiWrapper WUNDERKISTE = PoiWrapper.register(
            Wunderreich.ID("wunderkiste"),
            PoiWrapper.getBlockStates(WunderreichBlocks.WUNDER_KISTE),
            0, 1
    );

    public static void register() {

    }
}
