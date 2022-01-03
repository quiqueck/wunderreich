package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import ru.bclib.config.PathConfig;

public class Configs {
    public static final MainConfig MAIN = new MainConfig();
    public static final PathConfig BLOCK_CONFIG = new PathConfig(Wunderreich.MOD_ID, "blocks");

    public static void saveConfigs() {
        MAIN.saveChanges();
    }
}
