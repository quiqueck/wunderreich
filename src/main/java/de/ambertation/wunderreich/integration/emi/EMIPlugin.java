package de.ambertation.wunderreich.integration.emi;


import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EMIPlugin implements dev.emi.emi.api.EmiPlugin {
    public static final ResourceLocation WIDGETS = Wunderreich.ID(
            "textures/gui/widgets.png"
    );
    public static final EmiStack IMPRINTER_WORKSTATION = EmiStack.of(WunderreichBlocks.WHISPER_IMPRINTER);

    public static final EmiRecipeCategory IMPRINTER_CATEGORY = new EmiRecipeCategory(
            Wunderreich.ID("imprinter"),
            IMPRINTER_WORKSTATION,
            getSprite(0, 0)
    );

    public static EmiTexture getSprite(int u, int v) {
        return new EmiTexture(WIDGETS, u, v, 16, 16, 16, 16, 16, 16);
    }

    public static <C extends Container, T extends Recipe<C>, E extends EmiRecipe> void addAllRecipes(
            EmiRegistry emiRegistry,
            RecipeManager manager,
            Logger logger,
            RecipeType<T> recipeType,
            Function<T, E> createRecipe
    ) {
        addAllRecipes(
                emiRegistry,
                manager,
                logger,
                recipeType,
                (_ignored) -> null,
                (recipe, _ignored) -> createRecipe.apply(recipe)
        );
    }

    public static <C extends Container, T extends Recipe<C>, E extends EmiRecipe, V> void addAllRecipes(
            EmiRegistry emiRegistry,
            RecipeManager manager,
            Logger logger,
            RecipeType<T> recipeType,
            Function<T, List<V>> variantSupplier,
            BiFunction<T, V, E> createRecipe
    ) {
        final List<T> recipes = manager
                .getAllRecipesFor(recipeType)
                .stream()
                .sorted(Comparator.comparing(a -> a.getResultItem(Minecraft.getInstance().level.registryAccess())
                                                   .getDisplayName()
                                                   .getString()))
                .toList();
        for (T recipe : recipes) {
            List<V> variants = variantSupplier.apply(recipe);
            if (variants == null) {
                emiRegistry.addRecipe(createRecipe.apply(recipe, null));
            } else {
                for (V variantData : variants) {
                    try {
                        emiRegistry.addRecipe(createRecipe.apply(recipe, variantData));
                    } catch (Exception e) {
                        logger.error("Exception when parsing vanilla recipe " + recipe.getId(), e);
                    }
                }
            }
        }
    }

    @Override
    public void register(EmiRegistry emiRegistry) {
        final RecipeManager manager = emiRegistry.getRecipeManager();
        emiRegistry.addCategory(IMPRINTER_CATEGORY);
        emiRegistry.addWorkstation(IMPRINTER_CATEGORY, IMPRINTER_WORKSTATION);

        EMIImprinterRecipe.addAllRecipes(emiRegistry, manager);
    }
}
