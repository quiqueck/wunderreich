package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.gui.whisperer.WhispererScreen;

import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;

@Environment(EnvType.CLIENT)
public final class WunderreichScreens {
    public static final MenuType<WhispererMenu> WHISPERER = ScreenHandlerRegistry.registerSimple(Wunderreich.ID(
            "whisperer"), WhispererMenu::new);

    public static void registerScreens() {
        ScreenRegistry.register(WHISPERER, WhispererScreen::new);
    }

}
