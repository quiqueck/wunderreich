package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.Wunderreich;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;

public class ServerPlugin implements REIServerPlugin, iREI {
    public static CategoryIdentifier<ImprinterDisplay> IMPRINTER = CategoryIdentifier.of(Wunderreich.MOD_ID, ImprinterRecipe.Type.ID);

}
