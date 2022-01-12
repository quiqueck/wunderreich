package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import ru.bclib.config.PathConfig;

public class WunderreichConfigs {
    public static final MainConfig MAIN = new MainConfig();
    public static final BlockConfig BLOCK_CONFIG = new BlockConfig();
    public static final ItemConfig ITEM_CONFIG = new ItemConfig();
    public static final PathConfig RECIPE_CONFIG = new PathConfig(Wunderreich.MOD_ID, "receipes");

    public static void saveConfigs() {
        MAIN.saveChanges();
        BLOCK_CONFIG.saveChanges();
        ITEM_CONFIG.saveChanges();
        RECIPE_CONFIG.saveChanges();
    }
}
