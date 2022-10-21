package de.ambertation.wunderreich.interfaces;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;

public interface AbstractVillagerAccessor {
    void wunderreich_addOffersFromItemListings(
            MerchantOffers merchantOffers,
            VillagerTrades.ItemListing[] itemListings,
            int i
    );
}
