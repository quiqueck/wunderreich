package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.AbstractVillagerAccessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin implements AbstractVillagerAccessor {
    public void wunderreich_addOffersFromTradeSet(
            ServerLevel serverLevel,
            MerchantOffers merchantOffers,
            ResourceKey<TradeSet> tradeSet
    ) {
        addOffersFromTradeSet(serverLevel, merchantOffers, tradeSet);
    }

    @Shadow
    protected abstract void addOffersFromTradeSet(
            ServerLevel serverLevel,
            MerchantOffers merchantOffers,
            ResourceKey<TradeSet> tradeSet
    );
}
