package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.interfaces.IMerchantMenu;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements IMerchantMenu {
    @Shadow
    @Final
    private Merchant trader;

    public Villager getVillager() {
        if (!(trader instanceof Villager)) {
            return null;
        }

        return (Villager) trader;
    }
}
