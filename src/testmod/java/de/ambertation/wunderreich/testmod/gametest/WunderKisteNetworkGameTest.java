package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

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
 * <p>
 * 1.21.x has no game rule registry; the rule is looked up by its legacy id
 * ({@code wunderkisteAllowNetworks}) instead - {@link GameRules.Key} compares by id.
 */
public class WunderKisteNetworkGameTest {
    private static final GameRules.Key<GameRules.BooleanValue> ALLOW_NETWORKS =
            new GameRules.Key<>("wunderkisteAllowNetworks", GameRules.Category.MISC);

    @GameTest
    public void withNetworksDisabledEveryBoxGetsTheDefaultInventory(GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final GameRules.BooleanValue rule = server.getGameRules().getRule(ALLOW_NETWORKS);
        if (rule == null) {
            throw helper.assertionException(Component.literal("game rule wunderkisteAllowNetworks not found"));
        }

        final boolean before = rule.get();
        final List<String> failures = new ArrayList<>();
        final WunderKisteServerExtension ext = new WunderKisteServerExtension();
        try {
            rule.set(false, server);
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
            rule.set(true, server);
            final WunderKisteContainer white = ext.getContainer(server.registryAccess(), WunderKisteDomain.WHITE.domainID);
            final WunderKisteContainer lime = ext.getContainer(server.registryAccess(), WunderKisteDomain.LIME.domainID);
            if (!WunderKisteDomain.WHITE.domainID.equals(white.domain)) {
                failures.add("after re-enabling networks the white box shows the " + white.domain + " inventory");
            }
            if (!WunderKisteDomain.LIME.domainID.equals(lime.domain) || lime == white) {
                failures.add("after re-enabling networks the lime box does not get its own inventory (" + lime.domain + ")");
            }
        } finally {
            rule.set(before, server);
        }

        ChronariumTestSupport.failIfAny(helper, "Wunderkiste allow_networks=false regression", failures);
        helper.succeed();
    }
}
