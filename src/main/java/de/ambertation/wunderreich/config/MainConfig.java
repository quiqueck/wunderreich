package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;

public class MainConfig extends ConfigFile {
    public final static String FEATURE_CATEGORY = "features";
    public final static String DISPLAY_CATEGORY = "display";

    public final BooleanValue enableWhispers = new BooleanValue(
            FEATURE_CATEGORY,
            "enableWhispers",
            true
    );
    public final BooleanValue addBlankWhispere = new BooleanValue(
            FEATURE_CATEGORY,
            "addBlankWhisperer",
            true
    ).and(enableWhispers);

    public final BooleanValue addImprintedWhispers = new BooleanValue(
            FEATURE_CATEGORY,
            "addImprintedWhispers",
            true
    ).and(enableWhispers);

    public final BooleanValue allowBuilderTools = new BooleanValue(
            FEATURE_CATEGORY,
            "allowBuilderTools",
            true
    );

    public final BooleanValue allowConstructionTools = new BooleanValue(
            FEATURE_CATEGORY,
            "allowConstructionTools",
            true
    ).and(allowBuilderTools);

    public final BooleanValue addSlabs = new BooleanValue(
            FEATURE_CATEGORY,
            "addSlabs",
            true
    );

    public final BooleanValue addStairs = new BooleanValue(
            FEATURE_CATEGORY,
            "addStairs",
            true
    );

    public final BooleanValue addWalls = new BooleanValue(
            FEATURE_CATEGORY,
            "addWalls",
            true
    );

    public final BooleanValue multiTexturedWunderkiste = new BooleanValue(
            DISPLAY_CATEGORY,
            "multiTexturedWunderkiste",
            true
    );


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
        super("main");
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
