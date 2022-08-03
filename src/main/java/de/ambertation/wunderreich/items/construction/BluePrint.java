package de.ambertation.wunderreich.items.construction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import de.ambertation.wunderreich.registries.WunderreichItems;

public class BluePrint extends Item {
    public BluePrint() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.UNCOMMON)
                .durability(1000));
    }
}
