package de.ambertation.wunderreich.interfaces;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

@FunctionalInterface
public interface BlockTagSupplier {
    void supplyTags(Consumer<Tag<Block>> blockTags, Consumer<Tag<Item>> itemTags);
}
