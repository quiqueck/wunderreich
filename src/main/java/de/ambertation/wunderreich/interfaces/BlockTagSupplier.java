package de.ambertation.wunderreich.interfaces;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

@FunctionalInterface
public interface BlockTagSupplier {
    void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags);
}
