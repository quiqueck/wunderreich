package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.gui.construction.RulerScreen;
import de.ambertation.wunderreich.gui.whisperer.WhispererScreen;

import net.minecraft.client.gui.screens.MenuScreens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class WunderreichScreens {
    public static void registerScreens() {
        MenuScreens.register(WunderreichMenuTypes.WHISPERER, WhispererScreen::new);
        MenuScreens.register(WunderreichMenuTypes.RULER, RulerScreen::new);
    }
}
