package de.ambertation.wunderreich.integration.emi;

/*
 * EMI is not on this workspace's compile classpath: `dev.emi:emi-fabric` has no 26.2 build (the
 * dependency and `emi_version` are commented out in build.gradle / gradle.properties, and the
 * fabric.mod.json entrypoint is parked under "emi_disabled"). The whole EMI integration is
 * therefore kept as source-in-comment, exactly like EMIPlugin/EMIImprinterRecipe next to this
 * file, so it can be brought back in one step once EMI ships for this MC version.
 *
 * When re-enabling, note that the recipe list must NOT come from
 * `emiRegistry.getRecipeManager()` for aging: that only exists on the logical server. Use
 * `AgingRecipe.getUISortedRecipes(Minecraft.getInstance().level)` instead, which reads Fabric's
 * synced recipes and therefore also works on a dedicated server. See AgingRecipe#register().

import de.ambertation.wunderreich.integration.AgingRecipeUI;
import de.ambertation.wunderreich.recipes.AgingRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class EMIAgingRecipe implements EmiRecipe {
    private final Identifier id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final int time;
    private final float experience;

    public EMIAgingRecipe(RecipeHolder<AgingRecipe> holder) {
        final AgingRecipe recipe = holder.value();
        this.id = holder.id().identifier();
        this.input = List.of(
                EmiIngredient.of(recipe.input()),
                EmiIngredient.of(recipe.catalyst())
        );
        this.output = List.of(EmiStack.of(recipe.resultStack()));
        this.time = recipe.time();
        this.experience = recipe.experience();
    }

    static void addAllRecipes(EmiRegistry emiRegistry) {
        for (RecipeHolder<AgingRecipe> holder : AgingRecipe.getUISortedRecipes(Minecraft.getInstance().level)) {
            emiRegistry.addRecipe(new EMIAgingRecipe(holder));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.AGING_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    // The catalyst is required but handed back untouched, so it is a "catalyst" to EMI as well.
    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(input.get(1));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 124;
    }

    // Slots plus three lines of text (time, experience, catalyst note).
    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.addFillingArrow(50, 3, time * 50)
                    .tooltip((mx, my) -> AgingRecipeUI.timeTooltip(time)
                                                      .stream()
                                                      .map(dev.emi.emi.api.widget.TooltipComponent::of)
                                                      .toList());

        widgetHolder.addSlot(input.get(0), 2, 2)
                    .appendTooltip(AgingRecipeUI.inputTooltip());
        widgetHolder.addSlot(input.get(1), 24, 2)
                    .catalyst(true)
                    .appendTooltip(AgingRecipeUI.catalystTooltip());

        widgetHolder.addSlot(output.get(0), 100, 2)
                    .recipeContext(this);

        widgetHolder.addText(AgingRecipeUI.timeLabel(time).getVisualOrderText(), 62, 24, 0xFF404040, false);

        // Like a furnace, the result carries experience that is handed out when a player takes it.
        if (AgingRecipeUI.hasExperience(experience)) {
            widgetHolder.addText(
                    AgingRecipeUI.experienceLabel(experience).getVisualOrderText(),
                    62, 34, 0xFF404040, false
            );
        }

        widgetHolder.addText(AgingRecipeUI.catalystLabel().getVisualOrderText(), 62, 44, 0xFF404040, false);
    }

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }
}
*/
