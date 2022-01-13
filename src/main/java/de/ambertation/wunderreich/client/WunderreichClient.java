package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.interfaces.BlockEntityProvider;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
    public static net.minecraft.client.resources.model.Material BOX_OF_EIR_LOCATION = chestMaterial("box_of_eir");

    private static net.minecraft.client.resources.model.Material chestMaterial(String string) {
        return new net.minecraft.client.resources.model.Material(Sheets.CHEST_SHEET,
                new ResourceLocation(Wunderreich.MOD_ID, "entity/chest/" + string));
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
    }
}
