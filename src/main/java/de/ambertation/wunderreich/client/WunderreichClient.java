package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

import com.google.common.collect.Maps;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
    private static final Map<String, SpriteId> WUNDERKISTE_MATERIALS = Maps.newHashMap();
    public static SpriteId WUNDER_KISTE_LOCATION = getWunderkisteColor("wunder_kiste");
    public static SpriteId WUNDER_KISTE_TOP_LOCATION = chestMaterial(
            "wunder_kiste_top");

    public static SpriteId WUNDER_KISTE_MONOCHROME_TOP_LOCATION = chestMaterial(
            "wunder_kiste_bw_top");

    private static SpriteId chestMaterial(String string) {
        return new SpriteId(Sheets.CHEST_SHEET, Wunderreich.ID("entity/chest/" + string));
    }

    public static SpriteId getWunderkisteColor(String name) {
        return WUNDERKISTE_MATERIALS.computeIfAbsent(name, WunderreichClient::chestMaterial);
    }

    @Override
    public void onInitializeClient() {
        WunderreichParticles.register();
        WunderreichScreens.registerScreens();

        CreativeTabs.register();

        // Note: block render layers are now driven by the block model JSON
        // ("render_type") instead of the removed Fabric BlockRenderLayerMap.
        BuiltInRegistries.BLOCK.forEach(block -> {
            if (block instanceof BlockEntityProvider view) {
                BlockEntityRendererRegistry.register(
                        view.getBlockEntityType(),
                        view.getBlockEntityRenderProvider()
                );
            }
        });

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
