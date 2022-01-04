package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.gui.whisperer.WhispererScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.inventory.MenuType;

@Environment(EnvType.CLIENT)
public final class WunderreichScreens {
    public static final MenuType<WhispererMenu> WHISPERER = ScreenHandlerRegistry.registerSimple(Wunderreich.makeID("whisperer"), WhispererMenu::new);

    public static void registerScreens() {
        ScreenRegistry.register(WHISPERER, WhispererScreen::new);
    }

}
