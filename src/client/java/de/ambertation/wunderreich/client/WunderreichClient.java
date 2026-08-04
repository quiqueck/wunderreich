package de.ambertation.wunderreich.client;

import de.ambertation.wunderlib.network.ClientNetworkRegistry;
import de.ambertation.wunderlib.network.ExecutionPhase;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.renderer.WunderkisteRenderer;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.network.SuctionTubeContainerUpdatePacket;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class WunderreichClient implements ClientModInitializer {
    /**
     * {@code ChangeRenderLayer} lives in common code and can therefore only expose the plain
     * {@code RenderLayer} enum; this is the (client-only) place that turns it back into the real
     * {@link ChunkSectionLayer} the Fabric render layer map needs.
     */
    private static ChunkSectionLayer toChunkSectionLayer(ChangeRenderLayer.RenderLayer layer) {
        return switch (layer) {
            case CUTOUT -> ChunkSectionLayer.CUTOUT;
            case TRANSLUCENT -> ChunkSectionLayer.TRANSLUCENT;
        };
    }

    @Override
    public void onInitializeClient() {
        WunderreichParticleProviders.register();
        WunderreichScreens.registerScreens();

        CreativeTabs.register();

        ClientNetworkRegistry.addClientHandler(
                SuctionTubeContainerUpdatePacket.KEY,
                ExecutionPhase.GAME_THREAD,
                (payload, ctx) -> {
                    if (ctx.player() == null) {
                        Wunderreich.LOGGER.warn("Received SuctionTubeContainerUpdatePacket but player is null.");
                        return;
                    }

                    AbstractContainerMenu menu = ctx.player().containerMenu;
                    if (menu instanceof SuctionTubeMenu suctionTubeMenu) {
                        suctionTubeMenu.updateContainerConnections(payload.connections());
                    }
                }
        );

        ImprinterRecipe.CLIENT_RECIPE_MANAGER_SUPPLIER = () -> {
            var mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                var recipes = mc.getConnection().recipes();
                if (recipes instanceof RecipeManager rm) {
                    return rm;
                }
            }
            return null;
        };

        // ChangeRenderLayer is common code (so datagen's BlockModelProvider can read it too), but the
        // actual render-layer registration still has to happen here since this MC version's block
        // models don't yet drive their render layer purely from "render_type" at runtime.
        BuiltInRegistries.BLOCK.forEach(block -> {
            if (block instanceof ChangeRenderLayer view) {
                BlockRenderLayerMap.putBlocks(toChunkSectionLayer(view.getRenderType()), block);
            }
        });

        BlockEntityRendererRegistry.register(WunderreichBlockEntities.BLOCK_ENTITY_WUNDER_KISTE, WunderkisteRenderer::new);

        /*
         * Color Provider Registration for Grass Slab Block and Item
         *
         * BLOCK COLOR PROVIDER:
         * - Registered using ColorProviderRegistry.BLOCK.register() as usual
         * - Applies grass biome tinting to tintindex 0 faces in the block model
         *
         * ITEM COLOR PROVIDER (Minecraft 1.21.4+):
         * - Item colors are now defined in item model definitions using the new tint system
         * - The grass_slab item model (in models/item/layered_slab.json) uses:
         *   - "type": "grass" for automatic grass color tinting
         * - This replaces the old ColorProviderRegistry.ITEM.register() approach
         *
         * The block model (models/block/layered_slab.json) has tintindex: 0 on:
         * - Top face: "top" texture (grass_block_top)
         * - Side overlays: "overlay" texture (grass_block_side_overlay)
         */

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.GRASS_SLAB)) {
            ColorProviderRegistry.BLOCK.register(
                    (state, view, pos, tintIndex) -> {
                        if (tintIndex == 0) return view != null && pos != null
                                ? BiomeColors.getAverageGrassColor(view, pos)
                                : GrassColor.get(0.5D, 1.0D);

                        return 0xffffffff;
                    }, WunderreichSlabBlocks.GRASS_SLAB
            );
        }
    }
}
