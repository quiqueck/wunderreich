package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.registries.WunderreichRecipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 200)
public class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    public void interceptApply(Map<ResourceLocation, JsonElement> map,
                               ResourceManager resourceManager,
                               ProfilerFiller profiler,
                               CallbackInfo info) {

        WunderreichRecipes.RECIPES
                .entrySet()
                .stream()
                .filter(e -> !map.containsKey(e.getKey()))
                .forEach(e -> map.put(e.getKey(), e.getValue()));
        //map.putAll(WunderreichRecipes.RECIPES);
    }

}
