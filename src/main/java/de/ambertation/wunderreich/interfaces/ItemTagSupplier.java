package de.ambertation.wunderreich.interfaces;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

@FunctionalInterface
public interface ItemTagSupplier {
    void supplyTags(Consumer<TagKey<Item>> itemTags);
}
