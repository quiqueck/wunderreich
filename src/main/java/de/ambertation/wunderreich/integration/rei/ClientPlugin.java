package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.ArrayList;
import java.util.List;

public class ClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ImprinterCategory());

        // The Whisper Imprinter block is the working station for this category.
        registry.addWorkstations(ServerPlugin.IMPRINTER, EntryIngredients.of(WunderreichBlocks.WHISPER_IMPRINTER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // One display per imprinter recipe (imprint input + blank whisperer -> trained whisperer).
        for (ImprinterRecipe recipe : ImprinterRecipe.getUISortedRecipes()) {
            try {
                registry.add(ImprinterDisplay.of(recipe));
            } catch (Exception e) {
                Wunderreich.LOGGER.error("Failed to build REI display for recipe " + recipe.id, e);
            }
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        TrainedVillagerWhisperer.addAllVariants(stacks);
        for (ItemStack stack : stacks) {
            registry.addEntry(EntryStacks.of(stack));
        }
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new TransferHandler() {
            @Override
            public ApplicabilityResult checkApplicable(Context context) {
                if (context.getMenu() instanceof WhispererMenu && context.getDisplay() instanceof ImprinterDisplay) {
                    return ApplicabilityResult.createApplicable();
                }
                return ApplicabilityResult.createNotApplicable();
            }

            @Override
            public Result handle(Context context) {
                if (context.getMenu() instanceof WhispererMenu menu && context.getDisplay() instanceof ImprinterDisplay imprinterDisplay) {
                    if (context.isActuallyCrafting()) {
                        ResourceLocation recipeId = imprinterDisplay.getDisplayLocation().orElse(null);
                        ImprinterRecipe recipe = menu.getRuleByID(recipeId);
                        if (recipe != null) {
                            menu.tryMoveItems(recipe);
                        }
                    }
                    return Result.createSuccessful().blocksFurtherHandling(true);
                }
                return Result.createNotApplicable();
            }
        });
    }
}


