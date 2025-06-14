package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class VillagerWhisperer extends Item {

    public VillagerWhisperer(ResourceKey<Item> key) {
        this(WunderreichRules.Whispers.durability(), key);
    }

    public VillagerWhisperer(int durability, ResourceKey<Item> key) {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.RARE)
                .durability(durability)
                .setId(key)
        );
    }
}
