package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.registries.WunderreichItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

;

public class VillagerWhisperer extends Item {

    public VillagerWhisperer() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.RARE)
                .durability(40)
            );
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

}
