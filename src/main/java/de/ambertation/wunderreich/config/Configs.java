package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import ru.bclib.config.PathConfig;

public class Configs {
    public static final MainConfig MAIN = new MainConfig();
    public static final PathConfig BLOCK_CONFIG = new PathConfig(Wunderreich.MOD_ID, "blocks");
    public static final PathConfig ITEM_CONFIG = new PathConfig(Wunderreich.MOD_ID, "items");
    public static final PathConfig RECIPE_CONFIG = new PathConfig(Wunderreich.MOD_ID, "receipes");

    public static void saveConfigs() {
        MAIN.saveChanges();
        BLOCK_CONFIG.saveChanges();
        ITEM_CONFIG.saveChanges();
        RECIPE_CONFIG.saveChanges();
    }
}
