package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.InputManager;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.BiomeColors;
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

import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
    private static KeyMapping transformKey;

    @Override
    public void onInitializeClient() {
        WunderreichParticles.register();
        WunderreichScreens.registerScreens();

        CreativeTabs.register();

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
