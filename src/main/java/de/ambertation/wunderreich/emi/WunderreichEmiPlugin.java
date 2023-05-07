package de.ambertation.wunderreich.emi;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WunderreichEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory IMPRINTER_CATEGORY =
            new EmiRecipeCategory(new ResourceLocation(Wunderreich.MOD_ID, ImprinterRecipe.Type.ID.getPath()),
                    EmiStack.of(WunderreichBlocks.WHISPER_IMPRINTER)) {
                @Override
                public Component getName() {
                    return Component.translatable("block.wunderreich.whisper_imprinter");
                }
            };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(IMPRINTER_CATEGORY);
        registry.addWorkstation(IMPRINTER_CATEGORY, EmiStack.of(WunderreichBlocks.WHISPER_IMPRINTER));

        registry.getRecipeManager()
                .getAllRecipesFor(ImprinterRecipe.Type.INSTANCE)
                .stream()
                .map(ImprinterEmiRecipe::new)
                .forEach(registry::addRecipe);

        registry.setDefaultComparison(EmiStack.of(WunderreichItems.WHISPERER), c -> c.copy().nbt(true).build());
    }
}
