package de.ambertation.wunderreich.emi;

import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ImprinterEmiRecipe implements EmiRecipe {
    private final ImprinterRecipe recipe;
    private final EmiIngredient inputA;
    private final EmiIngredient inputB;
    private final EmiStack output;

    public ImprinterEmiRecipe(ImprinterRecipe recipe) {
        this.recipe = recipe;
        this.inputA = EmiIngredient.of(Arrays.stream(recipe.inputA.getItems()).map(EmiStack::of).toList());
        this.inputB = EmiIngredient.of(Arrays.stream(recipe.inputB.getItems()).map(EmiStack::of).toList());
        this.output = EmiStack.of(recipe.output);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return WunderreichEmiPlugin.IMPRINTER_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        // copying layout of ImprinterRecipe.getIngredients()
        return List.of(EmiStack.EMPTY, inputA, inputB);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 98;
    }

    @Override
    public int getDisplayHeight() {
        return 26;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        //Inputs
        widgets.addSlot(inputA, 0, 4);
        widgets.addSlot(inputB, 22, 4);

        //Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 44, 5);

        // Result
        widgets.addSlot(output, 72, 0).output(true).recipeContext(this);
    }
}
