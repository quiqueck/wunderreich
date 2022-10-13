package de.ambertation.wunderreich.interfaces;

import de.ambertation.wunderreich.loot.LootTableJsonBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

public interface CanDropLoot {
    default LootTableJsonBuilder buildLootTable() {
        if (this instanceof SlabBlock slab) {
            return LootTableJsonBuilder
                    .create(slab)
                    .dropSelfSlab();
        }
        if (this instanceof Block bl)
            return LootTableJsonBuilder.create(bl).dropSelf();

        return LootTableJsonBuilder.create("wunderreich.empty", LootTableJsonBuilder.LootTypes.UNKNOWN);
    }
}
