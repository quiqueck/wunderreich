package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.recipes.AgingRecipe;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

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
        registry.add(new AgingCategory());

        // The Whisper Imprinter block is the working station for this category.
        registry.addWorkstations(ServerPlugin.IMPRINTER, EntryIngredients.of(WunderreichBlocks.WHISPER_IMPRINTER));
        // ... and the Chronarium for the aging category.
        registry.addWorkstations(ServerPlugin.AGING, EntryIngredients.of(WunderreichBlocks.CHRONARIUM));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // One display per imprinter recipe (imprint input + blank whisperer -> trained whisperer).
        for (ImprinterRecipe recipe : ImprinterRecipe.getUISortedRecipes(Minecraft.getInstance().level)) {
            try {
                registry.add(ImprinterDisplay.of(recipe));
            } catch (Exception e) {
                Wunderreich.LOGGER.error("Failed to build REI display for recipe " + recipe.id, e);
            }
        }

        // Aging recipes never show up in the vanilla recipe book, so this is the only place a
        // player can find them. The list is read from the recipes the server synced to us, which
        // is what makes this work on a dedicated server as well.
        for (RecipeHolder<AgingRecipe> holder : AgingRecipe.getUISortedRecipes(Minecraft.getInstance().level)) {
            try {
                registry.add(AgingDisplay.of(holder));
            } catch (Exception e) {
                Wunderreich.LOGGER.error("Failed to build REI display for recipe " + holder.id(), e);
            }
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        TrainedVillagerWhisperer.addAllVariants(stacks, Minecraft.getInstance().level);
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
                        Identifier recipeId = imprinterDisplay.getDisplayLocation().orElse(null);
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


