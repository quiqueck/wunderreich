package de.ambertation.wunderreich.integration.jei;

import de.ambertation.wunderreich.integration.AgingRecipeUI;
import de.ambertation.wunderreich.recipes.AgingRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;

/**
 * JEI category for {@code wunderreich:aging} (the Chronarium).
 * <p>
 * Layout: {@code [input] [catalyst] --> [result]}, with two lines of text underneath naming the
 * processing time and stating that the catalyst is not consumed. Both slots additionally carry a
 * tooltip, because "the second slot is not an ingredient" is the one thing a viewer cannot show
 * with geometry alone.
 */
public class AgingCategory implements IRecipeCategory<RecipeHolder<AgingRecipe>> {
    public static final IRecipeHolderType<AgingRecipe> TYPE = IRecipeHolderType.create(AgingRecipe.Type.INSTANCE);

    private static final int WIDTH = 124;
    /**
     * 18px of slots plus three 10px lines of text (time, experience, catalyst note).
     */
    private static final int HEIGHT = 56;

    private static final int SLOT_Y = 2;
    private static final int INPUT_X = 2;
    private static final int CATALYST_X = 24;
    private static final int ARROW_X = 50;
    private static final int OUTPUT_X = 100;

    private final IDrawable icon;

    public AgingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(WunderreichBlocks.CHRONARIUM);
    }

    @Override
    public IRecipeHolderType<AgingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return AgingRecipeUI.title();
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AgingRecipe> recipeHolder, IFocusGroup focuses) {
        final AgingRecipe recipe = recipeHolder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
               .add(recipe.input())
               .addRichTooltipCallback((slot, tooltip) -> tooltip.addAll(AgingRecipeUI.inputTooltip()));

        // The catalyst is still an INPUT for JEI's purposes (looking up "what is this vine used
        // for?" has to find the aging recipes), but the tooltip makes clear it is given back.
        builder.addSlot(RecipeIngredientRole.INPUT, CATALYST_X, SLOT_Y)
               .add(recipe.catalyst())
               .addRichTooltipCallback((slot, tooltip) -> tooltip.addAll(AgingRecipeUI.catalystTooltip()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
               .add(recipe.result())
               .setOutputSlotBackground();
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<AgingRecipe> recipeHolder,
            IFocusGroup focuses
    ) {
        final AgingRecipe recipe = recipeHolder.value();
        final int time = recipe.time();

        // The arrow fills up over exactly the number of ticks the recipe takes, so the duration is
        // readable even without the label below.
        builder.addAnimatedRecipeArrow(time).setPosition(ARROW_X, SLOT_Y + 1);

        builder.addText(AgingRecipeUI.timeLabel(time), WIDTH, 10)
               .setTextAlignment(HorizontalAlignment.CENTER)
               .setPosition(0, 24);

        // Like a furnace, the result carries experience that is handed out when a player takes it.
        if (AgingRecipeUI.hasExperience(recipe.experience())) {
            builder.addText(AgingRecipeUI.experienceLabel(recipe.experience()), WIDTH, 10)
                   .setTextAlignment(HorizontalAlignment.CENTER)
                   .setPosition(0, 34);
        }

        builder.addText(AgingRecipeUI.catalystLabel(), WIDTH, 10)
               .setTextAlignment(HorizontalAlignment.CENTER)
               .setPosition(0, 44);
    }
}
