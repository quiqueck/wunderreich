package de.ambertation.wunderreich.config;

import org.wunder.lib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemConfig extends DynamicConfig<Item> {

    public ItemConfig() {
        super(Wunderreich.VERSION_PROVIDER, "items");
    }

    public boolean isEnabled(Item item) {
        if (item == null) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null;
    }
}
