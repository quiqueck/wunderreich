package de.ambertation.wunderreich;

import de.ambertation.wunderreich.network.CycleTradesMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class Wunderreich implements ModInitializer {
	public static final String MOD_ID = "wunderreich";
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		CycleTradesMessage.register();
	}
}
