package de.ambertation.wunderreich.integration.emi;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class EMIImprinterRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    public EMIImprinterRecipe(ImprinterRecipe recipe) {
        this.id = recipe.getId();
        this.input = recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList();
        this.output = List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
    }

    static void addAllRecipes(EmiRegistry emiRegistry, RecipeManager manager) {
        EMIPlugin.addAllRecipes(
                emiRegistry, manager, Wunderreich.LOGGER,
                ImprinterRecipe.Type.INSTANCE, EMIImprinterRecipe::new
        );
    }


    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.IMPRINTER_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 104;
    }

    @Override
    public int getDisplayHeight() {
        return 26;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        // Add an arrow texture to indicate processing
        widgetHolder.addTexture(EmiTexture.EMPTY_ARROW, 46, 5);

        // Adds an input slot on the left
        widgetHolder.addSlot(input.get(0), 0, 4);
        widgetHolder.addSlot(input.get(1), 20, 4);

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgetHolder.addSlot(output.get(0), 78, 0).large(true).recipeContext(this);
    }

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }
}

