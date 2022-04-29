package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Registry;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
    public static net.minecraft.client.resources.model.Material WUNDER_KISTE_LOCATION = chestMaterial("wunder_kiste");
    public static net.minecraft.client.resources.model.Material WUNDER_KISTE_MONOCHROME_LOCATION = chestMaterial("wunder_kiste_bw");

    private static net.minecraft.client.resources.model.Material chestMaterial(String string) {
        return new net.minecraft.client.resources.model.Material(Sheets.CHEST_SHEET,
                                                                 Wunderreich.ID("entity/chest/" + string));
    }

    @Override
    public void onInitializeClient() {
        WunderreichParticles.register();
        WunderreichScreens.registerScreens();

        Registry.BLOCK.forEach(block -> {
            if (block instanceof ChangeRenderLayer view) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, view.getRenderType());
            }

            if (block instanceof BlockEntityProvider view) {
                BlockEntityRendererRegistry.register(view.getBlockEntityType(),
                                                     view.getBlockEntityRenderProvider());
            }
        });


        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (tintIndex == 0) return view != null && pos != null
                    ? BiomeColors.getAverageGrassColor(view, pos)
                    : GrassColor.get(0.5D, 1.0D);

            return 0xffffffff;
        }, WunderreichBlocks.GRASS_SLAB);

        ColorProviderRegistry.ITEM.register((item, tintIndex) -> GrassColor.get(0.5D, 1.0D),
                                            WunderreichBlocks.GRASS_SLAB);
    }
}
