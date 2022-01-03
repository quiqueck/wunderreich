package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync;
import ru.bclib.config.ConfigUI;
import ru.bclib.config.NamedPathConfig;

public class MainConfig extends NamedPathConfig {
    public final static String FEATURE_CATEGORY = "features";

    public static final ConfigToken<Boolean> DO_NOT_DESPANW_WITH_NAMETAG = ConfigToken.Boolean(true, "doNotDespawnWithNametag", FEATURE_CATEGORY);
    public static final ConfigToken<Boolean> ALLOW_TRADES_CYCLING = ConfigToken.Boolean(true, "allowTradesCycling", FEATURE_CATEGORY);
    public static final ConfigToken<Boolean> ALLOW_LIBRARIAN_SELECTION = ConfigToken.Boolean(true, "allowLibrarianSelection", FEATURE_CATEGORY);

    public MainConfig() {
        super(Wunderreich.MOD_ID, "main", true);
    }
}
