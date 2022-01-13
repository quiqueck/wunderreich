package de.ambertation.wunderreich.config;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemConfig extends DynamicConfig<Item> {

    public ItemConfig() {
        super("items");
    }

    public boolean isEnabled(Item item) {
        ResourceLocation id = Registry.ITEM.getKey(item);
        if (id == null) return false;

        return true;
    }
}
