package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.AbstractVillagerAccessor;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin implements AbstractVillagerAccessor {
    public void wunderreich_addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListings, int i){
        addOffersFromItemListings(merchantOffers,itemListings, i);
    }

    @Shadow protected abstract void addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListings, int i);
}
