package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Which inventory a box gets when the {@code wunderkiste_allow_networks} rule is off.
 * <p>
 * Regression: the shared default key was filled with the inventory of whichever box asked first, so
 * with networks disabled a lime box touched first after start-up made "the" inventory lime's, and
 * the white network's items were hidden until a restart.
 * <p>
 * The rule is global, and tests run concurrently on the same server, so the rule is flipped and
 * restored inside one synchronous block - no other test can tick in between. A fresh
 * {@link WunderKisteServerExtension} is used so the server's real container cache is not touched.
 */
public class WunderKisteNetworkGameTest {
    @GameTest
    @SuppressWarnings("unchecked")
    public void withNetworksDisabledEveryBoxGetsTheDefaultInventory(GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final GameRule<Boolean> rule = (GameRule<Boolean>) BuiltInRegistries.GAME_RULE.getValue(
                Wunderreich.ID("wunderkiste_allow_networks"));
        if (rule == null) {
            throw helper.assertionException(Component.literal("game rule wunderreich:wunderkiste_allow_networks not found"));
        }

        final GameRules rules = server.getGameRules();
        final boolean before = rules.get(rule);
        final List<String> failures = new ArrayList<>();
        final WunderKisteServerExtension ext = new WunderKisteServerExtension();
        try {
            rules.set(rule, false, server);
            // a non-default network asks first ...
            final WunderKisteContainer fromLime = ext.getContainer(server.registryAccess(), WunderKisteDomain.LIME.domainID);
            final WunderKisteContainer fromWhite = ext.getContainer(server.registryAccess(), WunderKisteDomain.WHITE.domainID);

            if (fromLime != fromWhite) {
                failures.add("with networks disabled a lime and a white box got different inventories");
            }
            if (!WunderKisteBlock.DEFAULT_DOMAIN.domainID.equals(fromLime.domain)) {
                failures.add("with networks disabled the shared inventory is the " + fromLime.domain
                        + " network (the first box to ask), expected the default " + WunderKisteBlock.DEFAULT_DOMAIN.domainID);
            }

            // ... and turning networks back on gives each network its own inventory again
            rules.set(rule, true, server);
            final WunderKisteContainer white = ext.getContainer(server.registryAccess(), WunderKisteDomain.WHITE.domainID);
            final WunderKisteContainer lime = ext.getContainer(server.registryAccess(), WunderKisteDomain.LIME.domainID);
            if (!WunderKisteDomain.WHITE.domainID.equals(white.domain)) {
                failures.add("after re-enabling networks the white box shows the " + white.domain + " inventory");
            }
            if (!WunderKisteDomain.LIME.domainID.equals(lime.domain) || lime == white) {
                failures.add("after re-enabling networks the lime box does not get its own inventory (" + lime.domain + ")");
            }
        } finally {
            rules.set(rule, before, server);
        }

        ChronariumTestSupport.failIfAny(helper, "Wunderkiste allow_networks=false regression", failures);
        helper.succeed();
    }
}
