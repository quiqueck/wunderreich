package de.ambertation.wunderreich.config;

import de.ambertation.wunderlib.configs.ConfigFile;
import de.ambertation.wunderreich.Wunderreich;

public class MainConfig extends ConfigFile {
    public final static Group TOOLS_GROUP = new Group(Wunderreich.MOD_ID, "tools", 0);
    public final static Group BLOCKS_GROUP = new Group(Wunderreich.MOD_ID, "blocks", 1);
    public final static Group COSMETICS_GROUP = new Group(Wunderreich.MOD_ID, "cosmetics", 2);
    public final static String FEATURE_CATEGORY = "features";
    public final static String DISPLAY_CATEGORY = "display";

    public final BooleanValue enableWhispers = new BooleanValue(
            FEATURE_CATEGORY,
            "enableWhispers",
            true
    ).setGroup(TOOLS_GROUP);
    public final BooleanValue addBlankWhispere = new BooleanValue(
            FEATURE_CATEGORY,
            "addBlankWhisperer",
            true
    ).and(enableWhispers).setGroup(TOOLS_GROUP);
    ;

    public final BooleanValue addImprintedWhispers = new BooleanValue(
            FEATURE_CATEGORY,
            "addImprintedWhispers",
            true
    ).and(enableWhispers).setGroup(TOOLS_GROUP);
    ;

    public final BooleanValue multiTexturedWunderkiste = new BooleanValue(
            DISPLAY_CATEGORY,
            "multiTexturedWunderkiste",
            true
    ).setGroup(COSMETICS_GROUP);

    public final BooleanValue allowBuilderTools = new BooleanValue(
            FEATURE_CATEGORY,
            "allowBuilderTools",
            true
    ).setGroup(TOOLS_GROUP);

    public final BooleanValue allowConstructionTools = new BooleanValue(
            FEATURE_CATEGORY,
            "allowExperimentalConstructionTools",
            false
    ).and(allowBuilderTools).setGroup(TOOLS_GROUP);

    public final BooleanValue addSlabs = new BooleanValue(
            FEATURE_CATEGORY,
            "addSlabs",
            true
    ).setGroup(BLOCKS_GROUP);

    public final BooleanValue addStairs = new BooleanValue(
            FEATURE_CATEGORY,
            "addStairs",
            true
    ).setGroup(BLOCKS_GROUP);

    public final BooleanValue addWalls = new BooleanValue(
            FEATURE_CATEGORY,
            "addWalls",
            true
    ).setGroup(BLOCKS_GROUP);


    @Deprecated(forRemoval = true)
    public final BooleanValue deprecated_doNotDespawnWithNameTag = new BooleanValue(FEATURE_CATEGORY,
            "doNotDespawnWithNameTag",
            true, true
    );

    @Deprecated(forRemoval = true)
    public final BooleanValue deprecated_allowTradesCycling = new BooleanValue(FEATURE_CATEGORY,
            "allowTradesCycling",
            true, true
    );

    @Deprecated(forRemoval = true)
    public final BooleanValue deprecated_allowLibrarianSelection = new BooleanValue(
            FEATURE_CATEGORY,
            "allowLibrarianSelection",
            true,
            true
    ).and(deprecated_allowTradesCycling);

    @Deprecated(forRemoval = true)
    public final BooleanValue deprecated_cyclingNeedsWhisperer = new BooleanValue(
            FEATURE_CATEGORY,
            "cyclingNeedsWhisperer",
            true,
            true
    ).and(deprecated_allowTradesCycling);

    public MainConfig() {
        super(Wunderreich.VERSION_PROVIDER, "main");
    }


    public void runMigrations() {
        if (this.lastModifiedVersion().isLessThan("1.0.5")) {
            Wunderreich.LOGGER.info("Running 1.0.5 migration for main.json...");

            enableWhispers.set(deprecated_allowLibrarianSelection.get() || deprecated_cyclingNeedsWhisperer.get());
            addBlankWhispere.set(deprecated_allowLibrarianSelection.get() || deprecated_cyclingNeedsWhisperer.get());
            addImprintedWhispers.set(deprecated_allowLibrarianSelection.get());

            deprecated_doNotDespawnWithNameTag.migrate(Configs.DEFAULT_RULES.doNotDespawnWithNameTag);
            deprecated_allowTradesCycling.migrate(Configs.DEFAULT_RULES.allowTradesCycling);
            deprecated_allowLibrarianSelection.migrate(Configs.DEFAULT_RULES.allowLibrarianSelection);
            deprecated_cyclingNeedsWhisperer.migrate(Configs.DEFAULT_RULES.cyclingNeedsWhisperer);
        }
    }
}
