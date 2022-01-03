package de.ambertation.wunderreich.items;
;
import de.ambertation.wunderreich.registries.WunderreichItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import ru.bclib.items.ModelProviderItem;

public class VillagerWhisperer extends ModelProviderItem {

    public VillagerWhisperer() {
        super(WunderreichItems
                .makeItemSettings()
                .rarity(Rarity.RARE)
                .durability(20));
    }

    public String getEnchantmentID() {
        return "minecraft:unbreaking";
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }
}
