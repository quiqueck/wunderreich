package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.loot.LootTableJsonBuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;


@Mixin(value = ReloadableServerRegistries.class, priority = 200)
public class LootTablesMixin {
    @ModifyArg(method = "method_61240", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V"))
    private static Map<ResourceLocation, JsonElement> wunderreich_injectLootTables(
            ResourceManager resourceManager,
            ResourceKey<? extends Registry<JsonElement>> resourceKey,
            DynamicOps<JsonElement> dynamicOps,
            Codec<JsonElement> codec,
            Map<ResourceLocation, JsonElement> map
    ) {
        final String tablePath = Registries.elementsDirPath(LootDataType.TABLE.registryKey());
        String lootPath = Registries.elementsDirPath(resourceKey);
        if (tablePath.equals(lootPath)) {
            LootTableJsonBuilder
                    .getAllBlocks()
                    .filter(e -> !map.containsKey(e.id()))
                    .forEach(e -> map.put(e.id(), e.json().get()));
        }

        return map;
    }
}
