package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.blockentities.renderer.WunderkisteRenderer;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.network.SuctionTubeClientHandler;
import de.ambertation.wunderreich.network.SuctionTubeContainerUpdatePacket;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class WunderreichClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WunderreichParticleProviders.register();
        WunderreichScreens.registerScreens();

        CreativeTabs.register();

        SuctionTubeContainerUpdatePacket.HANDLER.setClientHandler(new SuctionTubeClientHandler());

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

        // Note: block render layers are now driven by the block model JSON
        // ("render_type") instead of the removed Fabric BlockRenderLayerMap.
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
            // The old ColorProviderRegistry.BLOCK is gone. The tint is now collected
            // into an IntList indexed by tintindex; index 0 carries the grass color.
            BlockColorRegistry.register(
                    (state, view, pos, out) -> {
                        int color = view != null && pos != null
                                ? BiomeColors.getAverageGrassColor(view, pos)
                                : GrassColor.get(0.5D, 1.0D);
                        out.add(color);
                    }, WunderreichSlabBlocks.GRASS_SLAB
            );
        }
    }
}
