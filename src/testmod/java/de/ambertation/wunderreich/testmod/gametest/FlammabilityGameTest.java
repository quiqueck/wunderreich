package de.ambertation.wunderreich.testmod.gametest;

import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

/**
 * Regression for "Tags not bound" on server start with Adorn: Adorn registers flammable block
 * <em>tags</em> in Fabric's {@link FlammableBlockRegistry}, and Wunderreich queried that registry for
 * every block it registered - before tags are loaded, so the server crashed during mod init.
 * {@code FlammableTagBeforeBlocksMixin} registers {@link #FLAMMABLE_PROBE_TAG} the same way right
 * before Wunderreich's blocks, so without the fix this suite never even boots. The test itself checks
 * that Wunderreich's own flammable blocks still burn.
 */
public class FlammabilityGameTest {
    public static final TagKey<Block> FLAMMABLE_PROBE_TAG = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("wunderreich-testmod", "flammable_probe")
    );

    @GameTest
    public void woolSlabsStayFlammableWhenAnotherModRegisteredAFlammableTag(GameTestHelper helper) {
        final Block slab = WunderreichSlabBlocks.WHITE_WOOL_SLAB;
        final FlammableBlockRegistry.Entry entry = FlammableBlockRegistry.getDefaultInstance().get(slab);
        if (entry.getBurnChance() == 0 || entry.getSpreadChance() == 0) {
            helper.fail(Component.literal("white_wool_slab is not flammable (burn " + entry.getBurnChance()
                    + ", spread " + entry.getSpreadChance() + ")"));
        }
        helper.succeed();
    }
}
