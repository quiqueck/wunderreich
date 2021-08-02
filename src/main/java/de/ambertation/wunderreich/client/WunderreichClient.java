package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public class WunderreichClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(Wunderreich.BLOCK_ENTITY_BOX_OF_EIR, ChestRenderer::new);
	}
}
