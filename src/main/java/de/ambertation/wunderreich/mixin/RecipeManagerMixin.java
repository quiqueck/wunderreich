package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.recipes.ImprinterOverrides;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichRecipes;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = RecipeManager.class, priority = 200)
public class RecipeManagerMixin {

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Unique
    private ResourceManager wunder_resourceManager;

    // Capture the ResourceManager for this reload so the imprinter override layer can be loaded from
    // it (synchronously, before the recipes are built below), guaranteeing the overrides are applied
    // regardless of reload-listener ordering.
    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
            at = @At("HEAD"))
    private void wunder_captureResourceManager(
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfoReturnable<RecipeMap> cir
    ) {
        this.wunder_resourceManager = resourceManager;
    }

    @ModifyArg(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/world/item/crafting/RecipeMap;"),
            index = 0)
    private Iterable<RecipeHolder<?>> wunder_addCustomRecipes(Iterable<RecipeHolder<?>> iterable) {
        List<RecipeHolder<?>> originalList;
        Set<Identifier> existingRecipeIds;

        if (iterable instanceof List<RecipeHolder<?>> iterableAsList) {
            originalList = iterableAsList;
            // Create a Set of existing recipe IDs for fast lookup (O(1) instead of O(n))
            existingRecipeIds = new HashSet<>();
            for (RecipeHolder<?> holder : originalList) {
                existingRecipeIds.add(holder.id().identifier());
            }
        } else {
            // If the iterable is not a List, convert it to a List and build the Set in parallel
            originalList = new ArrayList<>();
            existingRecipeIds = new HashSet<>();
            iterable.forEach(holder -> {
                originalList.add(holder);
                existingRecipeIds.add(holder.id().identifier());
            });
        }

        // Load the datapack override layer before the imprinter recipes are generated, so the
        // "disabled" flag can suppress recipes and overridden values are available to the recipes'
        // lazy suppliers. Only the raw JSON is parsed here (no ItemStack materialization).
        ImprinterOverrides.ensureLoaded(wunder_resourceManager);

        // Register ImprinterRecipe for level
        ImprinterRecipe.registerForLevel((RecipeManager) (Object) this, registries);

        // Inject the generated imprinter recipes into the recipe map directly. Their ItemStacks
        // are materialized lazily, so wrapping them in RecipeHolders here (during the reload's
        // prepare phase, before item data components are bound) does not trigger materialization.
        for (ImprinterRecipe imprinterRecipe : ImprinterRecipe.getRegisteredRecipes()) {
            if (existingRecipeIds.contains(imprinterRecipe.id)) continue;
            ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, imprinterRecipe.id);
            originalList.add(new RecipeHolder<>(resourceKey, imprinterRecipe));
            existingRecipeIds.add(imprinterRecipe.id);
        }

        // Add custom recipes from WunderreichRecipes
        WunderreichRecipes.RECIPES
                .entrySet()
                .stream()
                .filter(entry -> !existingRecipeIds.contains(entry.getKey()))
                .forEach(entry -> {
                    try {
                        Recipe<?> recipe = Recipe.CODEC.parse(
                                registries.createSerializationContext(JsonOps.INSTANCE),
                                entry.getValue()
                        ).getOrThrow();
                        ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, entry.getKey());
                        RecipeHolder<?> recipeHolder = new RecipeHolder<>(resourceKey, recipe);
                        originalList.add(recipeHolder);
                    } catch (Exception e) {
                        // Handle parsing errors gracefully
                        System.err.println("Failed to parse custom recipe " + entry.getKey() + ": " + e.getMessage());
                    }
                });

        return originalList;
    }

}
