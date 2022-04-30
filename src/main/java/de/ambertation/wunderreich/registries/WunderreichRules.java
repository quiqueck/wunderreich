package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.utils.WunderreichGameRules;
import de.ambertation.wunderreich.utils.WunderreichGameRules.BooleanRule;
import de.ambertation.wunderreich.utils.WunderreichGameRules.IntRule;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.storage.ServerLevelData;

public class WunderreichRules {
    private static final BooleanRule NO_NAMETAGGED_DESPAWN =
            new BooleanRule(Category.SPAWNING, Configs.DEFAULT_RULES.doNotDespawnWithNameTag);

    public static void onLevelLoad(ServerLevel l, ServerLevelData serverLevelData) {
        Wunderreich.LOGGER.info("Loading ServerLevel: " + l);
        WunderreichGameRules.setCurrentRules(serverLevelData.getGameRules());
    }

    public static void register() {
        Whispers.register();
        Wunderkiste.register();
    }

    public static boolean doNotDespawnWithNameTag() {
        return NO_NAMETAGGED_DESPAWN.get();
    }

    public static class Whispers {
        private static final IntRule DURABILITY =
                new IntRule(Category.MISC, Configs.DEFAULT_RULES.whisperDurability, 0, Integer.MAX_VALUE);

        private static final IntRule TRAINED_DURABILITY =
                new IntRule(Category.MISC, Configs.DEFAULT_RULES.whisperTrainedDurability, 0, Integer.MAX_VALUE);

        private static final IntRule MIN_XP_MULT =
                new IntRule(Category.DROPS, Configs.DEFAULT_RULES.whisperMinXPMultiplier, 0, Integer.MAX_VALUE);

        private static final IntRule MAX_XP_MULT =
                new IntRule(Category.DROPS, Configs.DEFAULT_RULES.whisperMaxXPMultiplier, 0, Integer.MAX_VALUE);


        private static final BooleanRule LIBRARIAN_SELECTION =
                new BooleanRule(Category.MOBS, Configs.DEFAULT_RULES.allowLibrarianSelection);

        private static final BooleanRule TRADES_CYCLING =
                new BooleanRule(Category.MOBS, Configs.DEFAULT_RULES.allowTradesCycling);

        private static final BooleanRule CYCLE_NEEDS_WHISPERER =
                new BooleanRule(Category.MOBS, Configs.DEFAULT_RULES.cyclingNeedsWhisperer);


        public static int durability() {
            return DURABILITY.get();
        }

        public static int trainedDurability() {
            return TRAINED_DURABILITY.get();
        }

        public static float minXPMultiplier() {
            return MIN_XP_MULT.get() / 100.0f;
        }

        public static float maxXPMultiplier() {
            return MAX_XP_MULT.get() / 100.0f;
        }

        public static boolean allowLibrarianSelection() {
            return LIBRARIAN_SELECTION.get() && Configs.ITEM_CONFIG.valueOf(WunderreichItems.BLANK_WHISPERER)
                    && Configs.ITEM_CONFIG.valueOf(WunderreichItems.WHISPERER);
        }

        public static boolean allowTradesCycling() {
            return TRADES_CYCLING.get();
        }

        public static boolean cyclingNeedsWhisperer() {
            return CYCLE_NEEDS_WHISPERER.get() && (Configs.ITEM_CONFIG.valueOf(WunderreichItems.BLANK_WHISPERER) || Configs.ITEM_CONFIG.valueOf(
                    WunderreichItems.WHISPERER));
        }


        static void register() {

        }
    }

    public static class Wunderkiste {
        private static final BooleanRule REDSTONE_POWER =
                new BooleanRule(Category.UPDATES, Configs.DEFAULT_RULES.wunderkisteRedstonePowerWhenOpened);

        private static final BooleanRule ANALOG_REDSTONE =
                new BooleanRule(Category.UPDATES, Configs.DEFAULT_RULES.wunderkisteAnalogRedstoneOutput);

        private static final BooleanRule SHOW_COLOR_WUNDERKISTE =
                new BooleanRule(Category.MISC, Configs.DEFAULT_RULES.wunderkisteShowColored);

        private static final BooleanRule ALLOW_WUNDERKISTE_DOMAINS =
                new BooleanRule(Category.MISC, Configs.DEFAULT_RULES.wunderkisteAllowDomains);

        private static final BooleanRule CAN_COLOR_WUNDERKISTE =
                new BooleanRule(Category.PLAYER, Configs.DEFAULT_RULES.wunderkisteCanColor);

        private static final IntRule COLOR_COST_WUNDERKISTE =
                new IntRule(Category.MISC, Configs.DEFAULT_RULES.wunderkisteChangeDomainCost, 0, 64);

        public static boolean redstonePowerWhenOpened() {
            return REDSTONE_POWER.get();
        }

        public static boolean analogRedstoneOutput() {
            return ANALOG_REDSTONE.get();
        }

        public static boolean showColors() {
            return SHOW_COLOR_WUNDERKISTE.get();
        }

        public static boolean canColor() {
            return CAN_COLOR_WUNDERKISTE.get();
        }

        public static boolean haveMultiple() {
            return ALLOW_WUNDERKISTE_DOMAINS.get();
        }

        public static int recolorCost() {
            return COLOR_COST_WUNDERKISTE.get();
        }

        public static boolean colorsOrDomains() {
            return showColors() || canColor() || haveMultiple();
        }

        public static boolean isRedstoneEnabled() {
            return redstonePowerWhenOpened() || analogRedstoneOutput();
        }

        static void register() {

        }
    }
}
