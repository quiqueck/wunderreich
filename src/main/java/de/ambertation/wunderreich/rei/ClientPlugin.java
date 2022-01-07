package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.registries.WunderreichItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientPlugin implements REIClientPlugin, iREI {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ImprinterCategory());

        registry.addWorkstations(IMPRINTER, EntryStacks.of(WunderreichItems.WHISPERER));

        registry.removePlusButton(IMPRINTER);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(ImprinterReceip.class, ImprinterReceip.TYPE, ImprinterDisplay::of);
    }
}
