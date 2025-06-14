package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.loot.LootTableHelper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

public interface CanDropLoot {
    default void buildLootTable(LootTableHelper.BlockLootProvider provider) {
        if (this instanceof SlabBlock slab) {
            if (needsSilkTouch()) provider.dropSlabWhenSilkTouch(slab);
            else provider.dropSlab(slab);
        } else if (this instanceof Block bl) {
            if (needsSilkTouch()) provider.dropWhenSilkTouch(bl);
            else provider.dropSelf(bl);
        }
    }

    default boolean needsSilkTouch() {
        return false;
    }
}
