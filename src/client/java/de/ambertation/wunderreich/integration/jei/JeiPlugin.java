package de.ambertation.wunderreich.integration.jei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichDataComponents;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JeiPlugin implements IModPlugin {
    private static final Identifier UID = Wunderreich.ID("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Without this, JEI treats every trained whisperer as one interchangeable stack of
        // WunderreichItems.WHISPERER and collapses them in the ingredient list/search - the
        // stored enchantment (WHISPERER component) is what actually distinguishes them.
        registration.registerFromDataComponentTypes(WunderreichItems.WHISPERER, WunderreichDataComponents.WHISPERER);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> variants = new ArrayList<>();
        TrainedVillagerWhisperer.addAllVariants(variants);
        registration.addExtraItemStacks(variants);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ImprinterCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<ImprinterRecipe>> recipes = ImprinterRecipe.getUISortedRecipes()
                                                                       .stream()
                                                                       .map(recipe -> new RecipeHolder<>(
                                                                               ResourceKey.create(Registries.RECIPE, recipe.id),
                                                                               recipe
                                                                       ))
                                                                       .toList();
        registration.addRecipes(ImprinterCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ImprinterCategory.TYPE, WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new ImprinterTransferHandler(),
                ImprinterCategory.TYPE
        );
    }

    private static class ImprinterTransferHandler implements IRecipeTransferHandler<WhispererMenu, RecipeHolder<ImprinterRecipe>> {
        @Override
        public Class<? extends WhispererMenu> getContainerClass() {
            return WhispererMenu.class;
        }

        @Override
        public Optional<MenuType<WhispererMenu>> getMenuType() {
            return Optional.of(WunderreichMenuTypes.WHISPERER);
        }

        @Override
        public IRecipeType<RecipeHolder<ImprinterRecipe>> getRecipeType() {
            return ImprinterCategory.TYPE;
        }

        @Override
        public IRecipeTransferError transferRecipe(
                WhispererMenu container,
                RecipeHolder<ImprinterRecipe> recipeHolder,
                IRecipeSlotsView recipeSlotsView,
                Player player,
                boolean maxTransfer,
                boolean doTransfer
        ) {
            if (doTransfer) {
                container.tryMoveItems(recipeHolder.value());
            }
            return null;
        }
    }
}
