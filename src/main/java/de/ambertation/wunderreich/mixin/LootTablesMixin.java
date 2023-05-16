package de.ambertation.wunderreich.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(value = LootDataManager.class, priority = 200)
public class LootTablesMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    public void wunderreich_interceptApply(
            Map<LootDataType<?>, Map<ResourceLocation, ?>> map,
            CallbackInfo info
    ) {
        System.out.println("WunderReich: LootTablesMixin.apply");
//        LootTableJsonBuilder
//                .getAllBlocks()
//                .filter(e -> !map.containsKey(e.id()))
//                .forEach(e -> map.put(e.id(), e.json().get()));
    }
}
