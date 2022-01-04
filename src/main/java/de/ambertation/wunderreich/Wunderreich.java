package de.ambertation.wunderreich;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.modmenu.MainScreen;
import de.ambertation.wunderreich.network.AddRemoveBoxOfEirMessage;
import de.ambertation.wunderreich.network.CycleTradesMessage;
import de.ambertation.wunderreich.network.SelectWhisperMessage;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichReceipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.api.WorldDataAPI;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.integration.modmenu.ModMenu;
import ru.bclib.util.Logger;

import java.util.List;
import java.util.Optional;

public class Wunderreich implements ModInitializer {
	public static final String MOD_ID = "wunderreich";
	public static final Logger LOGGER = new Logger(MOD_ID);
	public static String VERSION = "0.0.0";

	public static ResourceLocation makeID(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
	
	@Override
	public void onInitialize() {
		Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(Wunderreich.MOD_ID);
		if (optional.isPresent()) {
			ModContainer modContainer = optional.get();
			VERSION = modContainer.getMetadata().getVersion().toString();
		}
		
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		WunderreichBlockEntities.register();
		WunderreichBlocks.register();
		WunderreichItems.register();
		WunderreichReceipes.register();

		CycleTradesMessage.register();
		AddRemoveBoxOfEirMessage.register();

		WorldDataAPI.registerModCache(MOD_ID);
		DataExchangeAPI.registerMod(MOD_ID);
		Configs.saveConfigs();

		ModMenu.addModMenuScreen(MOD_ID, MainScreen::new);

		DataExchangeAPI.registerDescriptors(List.of(SelectWhisperMessage.DESCRIPTOR));
	}
}
