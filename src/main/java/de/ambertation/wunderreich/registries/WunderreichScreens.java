package de.ambertation.wunderreich.registries;

import net.minecraft.client.gui.screens.MenuScreens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderreich.gui.whisperer.WhispererScreen;

@Environment(EnvType.CLIENT)
public final class WunderreichScreens {
    public static void registerScreens() {
        MenuScreens.register(WunderreichMenuTypes.WHISPERER, WhispererScreen::new);
    }
}
