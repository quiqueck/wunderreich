package de.ambertation.wunderreich.config;

class TestConfig extends ConfigFile {
    public BooleanValue val1 = new BooleanValue("all.testA", "val1", false);
    public BooleanValue val2 = new BooleanValue("all.testA", "val2", true).and(val1);
    public IntValue val3 = new IntValue("ints", "val3", 42);

    public TestConfig() {
        super("test");
    }
}

public class WunderreichConfigs {
    public static final TestConfig TEST = new TestConfig();
    public static final MainConfig MAIN = new MainConfig();
    public static final BlockConfig BLOCK_CONFIG = new BlockConfig();
    public static final ItemConfig ITEM_CONFIG = new ItemConfig();
    public static final RecipeConfig RECIPE_CONFIG = new RecipeConfig();

    public static void saveConfigs() {
        MAIN.save();
        BLOCK_CONFIG.save();
        ITEM_CONFIG.save();
        RECIPE_CONFIG.save();
        TEST.save();
    }
}
