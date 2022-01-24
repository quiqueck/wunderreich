package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.recipes.ImprinterRecipe;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientRecipeBook.class, priority = 200)
public class ClientRecipeBookMixin {

    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private static void wunderreich_getCategory(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        RecipeType<?> recipeType = recipe.getType();
        if (recipeType == ImprinterRecipe.Type.INSTANCE) {
            cir.setReturnValue(RecipeBookCategories.UNKNOWN);
            cir.cancel();
        }
    }
}
