package de.ambertation.wunderreich.integration.jei;

import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;

public class ImprinterCategory implements IRecipeCategory<RecipeHolder<ImprinterRecipe>> {
    public static final IRecipeHolderType<ImprinterRecipe> TYPE = IRecipeHolderType.create(ImprinterRecipe.Type.INSTANCE);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 26;

    private final IDrawable icon;

    public ImprinterCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public IRecipeHolderType<ImprinterRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.wunderreich.whisper_imprinter");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ImprinterRecipe> recipeHolder, IFocusGroup focuses) {
        ImprinterRecipe recipe = recipeHolder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 0, 4)
               .add(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.INPUT, 23, 4)
               .add(WhisperRule.blank());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 4)
               .add(recipe.getOutput());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<ImprinterRecipe> recipeHolder, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(46, 3);
    }
}
