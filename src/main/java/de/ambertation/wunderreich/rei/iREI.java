package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.Wunderreich;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public interface iREI {
    CategoryIdentifier<ImprinterDisplay> IMPRINTER = CategoryIdentifier.of(Wunderreich.MOD_ID, "plugins/" + ImprinterReceip.Type.ID);
}
