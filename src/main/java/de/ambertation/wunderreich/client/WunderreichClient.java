package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Registry;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
    private static final Map<String, Material> WUNDERKISTE_MATERIALS = Maps.newHashMap();
    public static Material WUNDER_KISTE_LOCATION = getWunderkisteColor("wunder_kiste");
    public static Material WUNDER_KISTE_TOP_LOCATION = chestMaterial(
            "wunder_kiste_top");

    public static Material WUNDER_KISTE_MONOCHROME_TOP_LOCATION = chestMaterial(
            "wunder_kiste_bw_top");

    private static Material chestMaterial(String string) {
        return new Material(Sheets.CHEST_SHEET, Wunderreich.ID("entity/chest/" + string));
    }

    public static Material getWunderkisteColor(String name) {
        return WUNDERKISTE_MATERIALS.computeIfAbsent(name, WunderreichClient::chestMaterial);
    }

    public static void getAllWunderkisteMaterials(Consumer<Material> consumer) {
        //this ensures that all static fields are loaded before we register the materials
        WunderKisteDomain.WHITE.getMaterial();
        for (Map.Entry<String, Material> entry : WUNDERKISTE_MATERIALS.entrySet()) {
            consumer.accept(entry.getValue());
        }
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
