package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.InputManager;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.GrassColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

import com.google.common.collect.Maps;
import org.lwjgl.glfw.GLFW;

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

    private static KeyMapping transformKey;

    @Override
    public void onInitializeClient() {
        WunderreichParticles.register();
        WunderreichScreens.registerScreens();

        CreativeTabs.ensureStaticallyLoaded();

        BuiltInRegistries.BLOCK.forEach(block -> {
            if (block instanceof ChangeRenderLayer view) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, view.getRenderType());
            }

            if (block instanceof BlockEntityProvider view) {
                BlockEntityRendererRegistry.register(
                        view.getBlockEntityType(),
                        view.getBlockEntityRenderProvider()
                );
            }
        });

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.GRASS_SLAB)) {
            ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
                if (tintIndex == 0) return view != null && pos != null
                        ? BiomeColors.getAverageGrassColor(view, pos)
                        : GrassColor.get(0.5D, 1.0D);

                return 0xffffffff;
            }, WunderreichSlabBlocks.GRASS_SLAB);

            ColorProviderRegistry.ITEM.register(
                    (item, tintIndex) -> GrassColor.get(0.5D, 1.0D),
                    WunderreichSlabBlocks.GRASS_SLAB
            );
        }

        if (Configs.MAIN.allowConstructionTools.get()) {
            transformKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.wunderreich.transform",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_PERIOD,
                    "category.wunderreich"
            ));

            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (transformKey.consumeClick()) {
                    InputManager.INSTANCE.startTransformMode();
                }
            });
        }
    }
}
