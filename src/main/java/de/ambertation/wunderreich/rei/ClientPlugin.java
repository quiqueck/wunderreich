package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.registries.WunderreichItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ClientPlugin implements REIClientPlugin, iREI {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ImprinterCategory());

        registry.addWorkstations(ServerPlugin.IMPRINTER, EntryStacks.of(WunderreichItems.WHISPERER));

        registry.removePlusButton(ServerPlugin.IMPRINTER);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(ImprinterRecipe.class, ImprinterRecipe.Type.INSTANCE, ImprinterDisplay::of);
    }
}
