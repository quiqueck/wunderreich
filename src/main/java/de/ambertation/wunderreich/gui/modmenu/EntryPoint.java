package de.ambertation.wunderreich.gui.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class EntryPoint implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new MainScreen(screen);
    }
}
