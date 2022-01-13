package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.ItemTagSupplier;
import de.ambertation.wunderreich.utils.TagRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.tag.TagFactory;

public class WunderreichTags {
    public static final TagRegistry<Block> BLOCK = new TagRegistry<>();
    public static final TagRegistry<Item> ITEM = new TagRegistry<>();

    public static final Tag.Named<Block> MINEABLE_TROWEL = TagFactory.BLOCK.create(new ResourceLocation("c",
            "mineable/trowel"));


    static void supplyForBlock(Block bl) {
        if (bl instanceof BlockTagSupplier supl) {
            Item itm = bl.asItem();
            supl.supplyTags(
                    (tag) -> BLOCK.add(tag, bl),
                    (tag) -> ITEM.add(tag, itm)
            );
        }
    }

    static void supplyForItem(Item itm) {
        if (itm instanceof ItemTagSupplier supl) {
            supl.supplyTags(
                    (tag) -> ITEM.add(tag, itm)
            );
        }
    }


}
