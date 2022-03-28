package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.loot.LootTableJsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTables;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(value = LootTables.class, priority = 200)
public class LootTablesMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    public void wunderreich_interceptApply(Map<ResourceLocation, JsonElement> map,
                                           ResourceManager resourceManager,
                                           ProfilerFiller profiler,
                                           CallbackInfo info) {

        LootTableJsonBuilder
                .getAllBlocks()
                .filter(e -> !map.containsKey(e.id()))
                .forEach(e -> map.put(e.id(), e.json().get()));
    }
}
