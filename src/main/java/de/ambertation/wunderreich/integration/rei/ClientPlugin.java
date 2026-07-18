package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ImprinterCategory());

        // The Whisper Imprinter block is the working station for this category.
        registry.addWorkstations(ServerPlugin.IMPRINTER, EntryStacks.of(WunderreichBlocks.WHISPER_IMPRINTER));

        // There is no plus/craftable button for imprinter recipes.
        registry.removePlusButton(ServerPlugin.IMPRINTER);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // One display per imprinter recipe (imprint input + blank whisperer -> trained whisperer).
        for (ImprinterRecipe recipe : ImprinterRecipe.getUISortedRecipes()) {
            registry.add(ImprinterDisplay.of(recipe));
        }
    }
}
