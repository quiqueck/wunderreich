package de.ambertation.wunderreich.gui.modmenu;

import com.terraformersmc.modmenu.util.ModMenuApiMarker;
import ru.bclib.integration.ModMenuIntegration;

public class EntryPoint extends ModMenuIntegration {
    public static final ModMenuApiMarker entrypointObject = createEntrypoint(new EntryPoint());

    public EntryPoint() {
        super(MainScreen::new);
    }
}