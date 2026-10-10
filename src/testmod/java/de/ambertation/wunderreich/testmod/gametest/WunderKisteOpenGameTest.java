package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

/**
 * Opening and closing a Wunderkiste through its menu.
 * <p>
 * Regression guard from the 26.3 port: there {@code Container#startOpen/stopOpen} take a
 * {@code ContainerUser}, so the old {@code (Player)} overloads were never called by the chest menu.
 * The box never counted as open: no redstone signal, no lid animation or sound, and the network was
 * not saved when the menu closed. 1.21.6's {@code Container} still takes a {@code Player}, so it was
 * not affected; the test keeps it that way.
 * <p>
 * Uses the purple network so no other test's box shares the "is any box of this network open" state.
 */
public class WunderKisteOpenGameTest {
    private static final BlockPos KISTE = new BlockPos(2, 2, 2);
    private static final BlockPos LAMP = new BlockPos(3, 2, 2);
    private static final BlockPos PLAYER = new BlockPos(2, 2, 4);
    private static final WunderKisteDomain DOMAIN = WunderKisteDomain.PURPLE;

    @GameTest(maxTicks = 100)
    public void openingAWunderkisteCountsAsOpenPowersRedstoneAndSavesOnClose(GameTestHelper helper) {
        helper.setBlock(KISTE, WunderreichBlocks.WUNDER_KISTE.defaultBlockState().setValue(WunderKisteBlock.DOMAIN, DOMAIN));
        helper.setBlock(LAMP, Blocks.REDSTONE_LAMP);
        final WunderKisteBlockEntity kiste = kiste(helper);
        final ServerPlayer player = ChronariumTestSupport.menuPlayer(helper, PLAYER);
        final ItemStack marker = new ItemStack(Items.NAME_TAG, 7);
        marker.set(DataComponents.CUSTOM_NAME, Component.literal("open-test-" + helper.getLevel().getGameTime()));

        helper.startSequence()
              .thenExecute(() -> helper.useBlock(KISTE, player))
              .thenIdle(2)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  if (((ActiveChestStorage) player).getActiveWunderKiste() != kiste) {
                      failures.add("using the box did not make it the player's active Wunderkiste (menu not opened?)");
                  }
                  if (!kiste.isOpen()) {
                      failures.add("the box does not count as open while its menu is shown - startOpen was not called");
                  }
                  if (!helper.getBlockState(LAMP).getValue(RedstoneLampBlock.LIT)) {
                      failures.add("a redstone lamp next to the open box is not lit");
                  }
                  ChronariumTestSupport.failIfAny(helper, "Wunderkiste open regression", failures);

                  // change the network while open; closing has to write it to the level data
                  final WunderKisteContainer container = WunderKisteBlock.getContainer(kiste.getBlockState(), kiste, helper.getLevel());
                  container.setItem(0, marker.copy());
              })
              .thenExecute(player::closeContainer)
              .thenIdle(10)
              .thenExecute(() -> {
                  final List<String> failures = new ArrayList<>();
                  if (kiste.isOpen()) {
                      failures.add("the box still counts as open after the menu was closed - stopOpen was not called");
                  }
                  if (((ActiveChestStorage) player).getActiveWunderKiste() != null) {
                      failures.add("the player's active Wunderkiste was not cleared on close");
                  }
                  if (helper.getBlockState(LAMP).getValue(RedstoneLampBlock.LIT)) {
                      failures.add("the redstone lamp is still lit after the box was closed");
                  }

                  final ListTag saved = LevelData.getInstance()
                                                 .getWunderkisteInventory(DOMAIN.domainID)
                                                 .getList("items")
                                                 .orElse(new ListTag());
                  final boolean found = saved.stream().anyMatch(t -> t.toString().contains(
                          marker.get(DataComponents.CUSTOM_NAME).getString()));
                  if (!found) {
                      failures.add("closing the menu did not save the network inventory (saved: " + saved + ")");
                  }
                  ChronariumTestSupport.failIfAny(helper, "Wunderkiste close regression", failures);

                  WunderKisteBlock.getContainer(kiste.getBlockState(), kiste, helper.getLevel()).setItem(0, ItemStack.EMPTY);
                  // 1.21.x has no runBeforeTestEnd hook, so the mock player is removed by hand (see menuPlayer)
                  ChronariumTestSupport.removePlayer(helper, player);
              })
              .thenSucceed();
    }

    private static WunderKisteBlockEntity kiste(GameTestHelper helper) {
        final BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(KISTE));
        if (!(be instanceof WunderKisteBlockEntity k)) {
            throw helper.assertionException(Component.literal("no WunderKisteBlockEntity at " + KISTE + " (found " + be + ")"));
        }
        return k;
    }
}
