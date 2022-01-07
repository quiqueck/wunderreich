package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.particles.EirParticle;
import de.ambertation.wunderreich.particles.SimpleParticleType;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import de.ambertation.wunderreich.registries.WunderreichScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class WunderreichClient implements ClientModInitializer {
	public static net.minecraft.client.resources.model.Material BOX_OF_EIR_LOCATION = chestMaterial("box_of_eir");
	
	@Override
	public void onInitializeClient() {
		WunderreichParticles.register();
		BlockEntityRendererRegistry.INSTANCE.register(WunderreichBlockEntities.BLOCK_ENTITY_BOX_OF_EIR, ChestRenderer::new);

		WunderreichScreens.registerScreens();
	}
	
	private static net.minecraft.client.resources.model.Material chestMaterial(String string) {
		return new net.minecraft.client.resources.model.Material(Sheets.CHEST_SHEET, new ResourceLocation(Wunderreich.MOD_ID, "entity/chest/" + string));
	}
}
