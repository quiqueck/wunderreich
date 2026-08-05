package de.ambertation.wunderreich.utils;

import de.ambertation.wunderlib.configs.ConfigFile;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class WunderreichGameRules {
    private static GameRules currentRules;

    public static void setCurrentRules(GameRules rules) {
        if (currentRules != rules) {
            Wunderreich.LOGGER.info("Load new Set of Server Rules");
            currentRules = rules;
        }
    }

    private static abstract class Base<R, V extends ConfigFile.Value<R, V>> {
        public final GameRule<R> key;
        public final V config;

        protected Base(String name, GameRule<R> key, V config) {
            Wunderreich.LOGGER.info("Adding GameRule '" + name + "' (default: " + config.get() + ")");
            this.key = key;
            this.config = config;
        }

        static String upperCaseDots(String str) {
            final String regex = "(\\.\\w)";

            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(str);

            return matcher.replaceAll(m -> m.group(0).substring(1).toUpperCase());
        }

        static <VV extends ConfigFile.Value> String buildName(VV config) {
            return upperCaseDots(config.token.path()) + config.token.key().substring(0, 1).toUpperCase() + config.token
                    .key()
                    .substring(1);
        }

        // Game rules are now registered as namespaced Identifiers (snake_case path) instead of the
        // old plain camelCase fabric rule name. We derive a stable identifier from the legacy name.
        static Identifier idFor(String name) {
            String path = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            return Wunderreich.ID(path);
        }

        @NotNull
        public R get() {
            if (currentRules == null) return config.get();
            return currentRules.get(key);
        }
    }

    public static class BooleanRule extends Base<Boolean, ConfigFile.BooleanValue> {
        public BooleanRule(GameRuleCategory category, ConfigFile.BooleanValue config) {
            this(buildName(config), category, config);
        }

        public BooleanRule(
                String name,
                GameRuleCategory category,
                ConfigFile.BooleanValue config
        ) {
            super(
                    name,
                    GameRuleBuilder.forBoolean(config.get()).category(category).buildAndRegister(idFor(name)),
                    config
            );
        }
    }

    public static class IntRule extends Base<Integer, ConfigFile.IntValue> {
        public IntRule(GameRuleCategory category, ConfigFile.IntValue config) {
            this(category, config, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntRule(
                GameRuleCategory category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue,
                BiConsumer<MinecraftServer, Integer> callback
        ) {
            this(buildName(config), category, config, minValue, maxValue, callback);
        }

        public IntRule(
                GameRuleCategory category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue
        ) {
            this(buildName(config), category, config, minValue, maxValue, null);
        }

        public IntRule(
                String name,
                GameRuleCategory category,
                ConfigFile.IntValue config
        ) {
            this(name, category, config, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
        }

        public IntRule(
                String name,
                GameRuleCategory category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue
        ) {
            this(name, category, config, minValue, maxValue, null);
        }

        public IntRule(
                String name,
                GameRuleCategory category,
                ConfigFile.IntValue config,
                int minValue,
                int maxValue,
                BiConsumer<MinecraftServer, Integer> callback
        ) {
            super(
                    name,
                    GameRuleBuilder
                            .forInteger(config.get())
                            .range(minValue, maxValue)
                            .category(category)
                            .buildAndRegister(idFor(name)),
                    config
            );
            if (callback != null) {
                GameRuleEvents.changeCallback(key).register((value, server) -> callback.accept(server, value));
            }
        }
    }
}
