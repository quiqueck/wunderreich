package de.ambertation.wunderreich.interfaces;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

@FunctionalInterface
public interface ItemTagSupplier {
    void supplyTags(Consumer<Tag.Named<Item>> itemTags);
}
