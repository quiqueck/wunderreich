package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class VillagerWhisperer extends Item {

    public VillagerWhisperer() {
        this(WunderreichRules.Whispers.durability());
    }

    public VillagerWhisperer(int durability) {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.RARE)
                .durability(durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

}
