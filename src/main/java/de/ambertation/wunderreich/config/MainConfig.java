package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.registries.WunderreichItems;

public class MainConfig extends ConfigFile {
    public final static String FEATURE_CATEGORY = "features";

    public final BooleanValue doNotDespawnWithNameTag = new BooleanValue(FEATURE_CATEGORY,
            "doNotDespawnWithNameTag",
            true);

    public final BooleanValue allowTradesCycling = new BooleanValue(FEATURE_CATEGORY, "allowTradesCycling", true);

    public final BooleanValue allowLibrarianSelection = new BooleanValue(
            FEATURE_CATEGORY,
            "allowLibrarianSelection",
            true
    ).and(allowTradesCycling);

    public final BooleanValue cyclingNeedsWhisperer = new BooleanValue(
            FEATURE_CATEGORY,
            "cyclingNeedsWhisperer",
            true
    ).and(allowTradesCycling);

    public final BooleanValue allowBuilderTools = new BooleanValue(FEATURE_CATEGORY,
            "allowBuilderTools",
            true);


    public MainConfig() {
        super("main");
    }

    public boolean allowLibrarianSelection() {
        return allowLibrarianSelection.get()
                && WunderreichConfigs.ITEM_CONFIG.valueOf(WunderreichItems.BLANK_WHISPERER).get()
                && WunderreichConfigs.ITEM_CONFIG.valueOf(WunderreichItems.WHISPERER).get();
    }
}
