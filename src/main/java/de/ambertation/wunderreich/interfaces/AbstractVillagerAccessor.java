package de.ambertation.wunderreich.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;

public interface AbstractVillagerAccessor {
    void wunderreich_addOffersFromTradeSet(
            ServerLevel serverLevel,
            MerchantOffers merchantOffers,
            ResourceKey<TradeSet> tradeSet
    );
}
