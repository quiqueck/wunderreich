package de.ambertation.wunderreich.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class WunderreichTags {
    public static final TagKey<Block> MINEABLE_TROWEL = createWover("mineable/trowel");
    public static final TagKey<Block> MINEABLE_SHEARS = createWover("mineable/shears");

    public static TagKey<Block> createWover(String name) {
        return TagKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath("wover", name));
    }
}
