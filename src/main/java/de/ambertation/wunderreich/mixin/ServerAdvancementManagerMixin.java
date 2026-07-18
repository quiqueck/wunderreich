package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.registries.WunderreichAdvancements;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ServerAdvancementManager.class, priority = 200)
public class ServerAdvancementManagerMixin {
    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    public void wunder_interceptApply(
            Map<Identifier, Advancement> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo info
    ) {
        final var dynamicOps = registries.createSerializationContext(JsonOps.INSTANCE);
        WunderreichAdvancements.ADVANCEMENTS
                .entrySet()
                .stream()
                .filter(e -> !map.containsKey(e.getKey()))
                .forEach(e -> {
                    DataResult<Advancement> advancement = Advancement.CODEC.parse(dynamicOps, e.getValue());
                    map.put(e.getKey(), advancement.getOrThrow());
                });
    }
}