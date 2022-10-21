package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.ConfigFile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.Category;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class WunderreichGameRules {
    private static net.minecraft.world.level.GameRules currentRules;

    public static void setCurrentRules(net.minecraft.world.level.GameRules rules) {
        if (currentRules != rules) {
            Wunderreich.LOGGER.info("Load new Set of Server Rules");
            currentRules = rules;
        }
    }

    private static abstract class Base<T extends net.minecraft.world.level.GameRules.Value<T>, V extends ConfigFile.Value<R>, R> {
        public final net.minecraft.world.level.GameRules.Type<T> type;
        public final net.minecraft.world.level.GameRules.Key<T> key;
        public final V config;

        protected Base(Category category, net.minecraft.world.level.GameRules.Type<T> type, V config) {
            this(buildName(config), category, type, config);
        }

        protected Base(String name, Category category, net.minecraft.world.level.GameRules.Type<T> type, V config) {
            Wunderreich.LOGGER.info("Adding GameRule '" + name + "' (default: " + config.get() + ")");
            this.type = type;
            this.config = config;

            this.key = GameRuleRegistry.register(name, category, type);
        }

        private static String upperCaseDots(String str) {
            final String regex = "(\\.\\w)";

            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(str);

            return matcher.replaceAll(m -> m.group(0).substring(1).toUpperCase());
        }

        private static <VV extends ConfigFile.Value> String buildName(VV config) {
            return upperCaseDots(config.token.path()) + config.token.key().substring(0, 1).toUpperCase() + config.token
                    .key()
                    .substring(1);
        }

        protected abstract R get(@NotNull net.minecraft.world.level.GameRules rules);

        @NotNull
        public R get() {
            if (currentRules == null) return config.get();
            return get(currentRules);
        }
    }

    public static class BooleanRule extends Base<net.minecraft.world.level.GameRules.BooleanValue, ConfigFile.BooleanValue, Boolean> {
        public BooleanRule(Category category, ConfigFile.BooleanValue config) {
            super(category, GameRuleFactory.createBooleanRule(config.get()), config);
        }

        public BooleanRule(
                String name,
                Category category,
                ConfigFile.BooleanValue config
        ) {
            super(name, category, GameRuleFactory.createBooleanRule(config.get()), config);
        }


        @Override
        protected Boolean get(net.minecraft.world.level.@NotNull GameRules rules) {
            return rules.getBoolean(key);
        }
    }

    public static class IntRule extends Base<net.minecraft.world.level.GameRules.IntegerValue, ConfigFile.IntValue, Integer> {
        public IntRule(Category category, ConfigFile.IntValue config) {
            this(category, config, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntRule(
                Category category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> callback
        ) {
            super(category, GameRuleFactory.createIntRule(config.get(), minValue, maxValue, callback), config);
        }

        public IntRule(
                Category category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue
        ) {
            super(category, GameRuleFactory.createIntRule(config.get(), minValue, maxValue), config);
        }

        public IntRule(
                String name,
                Category category,
                ConfigFile.IntValue config
        ) {
            this(name, category, config, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntRule(
                String name,
                Category category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue
        ) {
            super(name, category, GameRuleFactory.createIntRule(config.get(), minValue, maxValue), config);
        }

        @Override
        protected Integer get(net.minecraft.world.level.@NotNull GameRules rules) {
            return rules.getInt(key);
        }
    }
}
