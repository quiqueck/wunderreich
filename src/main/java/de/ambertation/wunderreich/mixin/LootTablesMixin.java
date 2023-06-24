package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.loot.LootTableJsonBuilder;

import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;


@Mixin(value = LootDataManager.class, priority = 200)
public class LootTablesMixin {
    @ModifyArg(method = "scheduleElementParse", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), index = 1)
    private static <T> Object wunderreich_injectLootTables(Object key, Object value) {
        if (value instanceof Map map) {
            if (key instanceof LootDataType lootDataType && lootDataType == LootDataType.TABLE) {
                LootTableJsonBuilder
                        .getAllBlocks()
                        .filter(e -> !map.containsKey(e.id()))
                        .forEach(e -> lootDataType.deserialize(e.id(), e.json().get()).ifPresent((object) -> {
                            map.put(e.id(), object);
                        }));
            }
        }
        return value;
    }
}
