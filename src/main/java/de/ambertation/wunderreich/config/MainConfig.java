package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.registries.WunderreichItems;

public class MainConfig extends ConfigFile {
    public final static String FEATURE_CATEGORY = "features";
    public final static String WUNDERKISTE_CATEGORY = "wunderkiste";
    public final static String WHIPSER_CATEGORY = "whispers";

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

    public final BooleanValue addSlabs = new BooleanValue(FEATURE_CATEGORY,
                                                          "addSlabs",
                                                          true);


    public final BooleanValue wunderkisteRedstoneSignal = new BooleanValue(WUNDERKISTE_CATEGORY,
                                                                           "enableRedstoneSignal",
                                                                           true);

    public final BooleanValue wunderkisteRedstoneAnalog = new BooleanValue(WUNDERKISTE_CATEGORY,
                                                                           "enableAnalogRedstoneOutput",
                                                                           true);

    public final BooleanValue wunderkisteAllowMultiple = new BooleanValue(WUNDERKISTE_CATEGORY,
                                                                          "wunderkisteAllowMultiple",
                                                                          true);

    public final IntValue whisperDurability = new IntValue(WHIPSER_CATEGORY, "durability", 20);
    public final IntValue whisperTrainedDurability = new IntValue(WHIPSER_CATEGORY, "trainedDurability", 40);
    public final FloatValue whisperMinXPMultiplier = new FloatValue(WHIPSER_CATEGORY, "minXPMultiplier", 0.75f);
    public final FloatValue whisperMaxXPMultiplier = new FloatValue(WHIPSER_CATEGORY, "maxXPMultiplier", 1.0f);

    public MainConfig() {
        super("main");
        wunderkisteRedstoneSignal.hideInUI();
        wunderkisteRedstoneAnalog.hideInUI();
        wunderkisteAllowMultiple.hideInUI();
    }

    public boolean allowLibrarianSelection() {
        return allowLibrarianSelection.get()
                && Configs.ITEM_CONFIG.valueOf(WunderreichItems.BLANK_WHISPERER)
                && Configs.ITEM_CONFIG.valueOf(WunderreichItems.WHISPERER);
    }

    public boolean wunderkisteIsRedstoneEnabled() {
        return wunderkisteRedstoneSignal.get() || wunderkisteRedstoneAnalog.get();
    }
}
