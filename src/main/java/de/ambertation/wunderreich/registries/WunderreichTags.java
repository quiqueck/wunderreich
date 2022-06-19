package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.ItemTagSupplier;
import de.ambertation.wunderreich.utils.TagRegistry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;

public class WunderreichTags {
    public static final TagKey<Block> MINEABLE_TROWEL = TagRegistry.BLOCK.createCommon("mineable/trowel");
    public static final TagKey<Block> MINEABLE_SHEARS = FabricMineableTags.SHEARS_MINEABLE;

    static void supplyForBlock(Block bl) {
        if (bl instanceof BlockTagSupplier supl) {
            Item itm = bl.asItem();
            supl.supplyTags(
                    (tag) -> TagRegistry.BLOCK.add(tag, bl),
                    (tag) -> TagRegistry.ITEM.add(tag, itm)
            );
        }
    }

    static void supplyForItem(Item itm) {
        if (itm instanceof ItemTagSupplier supl) {
            supl.supplyTags(
                    (tag) -> TagRegistry.ITEM.add(tag, itm)
            );
        }
    }


}
