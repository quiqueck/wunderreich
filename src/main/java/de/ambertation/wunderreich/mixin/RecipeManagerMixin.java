package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichRecipes;

import com.mojang.serialization.JsonOps;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 26.3 turned recipes into a datapack registry: {@code RecipeManager} is no longer a reload listener with
 * a {@code prepare(ResourceManager, ProfilerFiller)} phase, it is simply built from
 * {@code HolderLookup.Provider} in its constructor.
 * <p>
 * Instead of overwriting {@code RecipeManager.recipes} at constructor TAIL (which breaks Fabric API's
 * serializer index decoration on {@code RecipeMap.create} and causes player disconnects with "Invalid player data"),
 * we extend the {@code HolderLookup} passed into {@code RecipeMap.create}.
 */
@Mixin(value = RecipeManager.class, priority = 300)
public class RecipeManagerMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"
            )
    )
    private HolderLookup<Recipe<?>> wunder_addCustomRecipes(
            HolderLookup<Recipe<?>> datapackRecipes,
            @Local(argsOnly = true) HolderLookup.Provider registries
    ) {
        final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> extraRecipes = new LinkedHashMap<>();
        final Set<Identifier> existingRecipeIds = new HashSet<>();

        for (Holder.Reference<Recipe<?>> ref : datapackRecipes.listElements().toList()) {
            existingRecipeIds.add(ref.key().identifier());
        }

        // Register ImprinterRecipe for level
        ImprinterRecipe.registerForLevel(registries);

        // Inject the generated imprinter recipes
        for (ImprinterRecipe imprinterRecipe : ImprinterRecipe.getRegisteredRecipes()) {
            if (existingRecipeIds.contains(imprinterRecipe.id)) continue;
            ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, imprinterRecipe.id);
            extraRecipes.put(resourceKey, new RecipeHolder<>(resourceKey, imprinterRecipe));
            existingRecipeIds.add(imprinterRecipe.id);
        }

        // Add custom recipes from WunderreichRecipes
        WunderreichRecipes.RECIPES
                .entrySet()
                .stream()
                .filter(entry -> !existingRecipeIds.contains(entry.getKey()))
                .forEach(entry -> {
                    try {
                        Recipe<?> recipe = Recipe.DIRECT_CODEC.parse(
                                registries.createSerializationContext(JsonOps.INSTANCE),
                                entry.getValue()
                        ).getOrThrow();
                        ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, entry.getKey());
                        extraRecipes.put(resourceKey, new RecipeHolder<>(resourceKey, recipe));
                        existingRecipeIds.add(entry.getKey());
                    } catch (Exception e) {
                        System.err.println("Failed to parse custom recipe " + entry.getKey() + ": " + e.getMessage());
                    }
                });

        return wunder_extendLookup(datapackRecipes, extraRecipes);
    }

    @Unique
    private static HolderLookup<Recipe<?>> wunder_extendLookup(
            HolderLookup<Recipe<?>> base,
            Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> extra
    ) {
        if (extra.isEmpty()) return base;

        final Map<ResourceKey<Recipe<?>>, Holder.Reference<Recipe<?>>> added = new LinkedHashMap<>();
        extra.forEach((key, holder) -> {
            Holder.Reference<Recipe<?>> reference = Holder.Reference.createStandAlone(base, key);
            reference.bindValue(holder.value());
            added.put(key, reference);
        });

        return new HolderLookup<Recipe<?>>() {
            @Override
            public Stream<Holder.Reference<Recipe<?>>> listElements() {
                return Stream.concat(
                        base.listElements().filter(ref -> !added.containsKey(ref.key())),
                        added.values().stream()
                );
            }

            @Override
            public Optional<Holder.Reference<Recipe<?>>> get(ResourceKey<Recipe<?>> key) {
                Holder.Reference<Recipe<?>> ours = added.get(key);
                return ours != null ? Optional.of(ours) : base.get(key);
            }

            @Override
            public Stream<HolderSet.Named<Recipe<?>>> listTags() {
                return base.listTags();
            }

            @Override
            public Optional<HolderSet.Named<Recipe<?>>> get(TagKey<Recipe<?>> tag) {
                return base.get(tag);
            }
        };
    }
}
