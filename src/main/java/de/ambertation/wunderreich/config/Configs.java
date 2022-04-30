package de.ambertation.wunderreich.config;

public class Configs {
    public static final MainConfig MAIN = new MainConfig();
    public static final BlockConfig BLOCK_CONFIG = new BlockConfig();
    public static final ItemConfig ITEM_CONFIG = new ItemConfig();
    public static final RecipeConfig RECIPE_CONFIG = new RecipeConfig();
    public static final DefaultGameRules DEFAULT_RULES = new DefaultGameRules();

    public static void saveConfigs() {
        MAIN.runMigrations();

        MAIN.save();
        BLOCK_CONFIG.save();
        ITEM_CONFIG.save();
        RECIPE_CONFIG.save();
        DEFAULT_RULES.save();
    }
}
