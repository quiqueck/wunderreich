package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import ru.bclib.config.PathConfig;

public class Configs {
    public static final MainConfig MAIN = new MainConfig();

    public static void saveConfigs() {
        MAIN.saveChanges();
    }
}
