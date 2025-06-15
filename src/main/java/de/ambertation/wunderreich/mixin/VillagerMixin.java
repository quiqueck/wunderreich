package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.AbstractVillagerAccessor;
import de.ambertation.wunderreich.network.CycleTradesMessage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.trading.MerchantOffers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Villager.class, priority = 100)
public class VillagerMixin {

    /**
     * Wraps the original updateTrades logic in a while loop to ensure trades force by an imprinter
     * are available. This method replicates the core logic from the original updateTrades method.
     */
    private void wunderreich_updateTradesProxy() {
        Villager self = (Villager) (Object) this;
        AbstractVillagerAccessor acc = (AbstractVillagerAccessor) this;

        boolean found;
        MerchantOffers merchantOffers = new MerchantOffers();
        VillagerTrades.ItemListing[] itemListings;
        int maxCount = 1000;
        do {
            //TODO: [MC Update] Check for changes in base Method
            //-------------------------------------
            VillagerData villagerData = self.getVillagerData();
            ResourceKey<VillagerProfession> resourceKey = villagerData.profession().unwrapKey().orElse(null);
            if (resourceKey != null) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> int2ObjectMap2;
                if (self.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                    Int2ObjectMap<VillagerTrades.ItemListing[]> int2ObjectMap = VillagerTrades.EXPERIMENTAL_TRADES
                            .get(resourceKey);
                    int2ObjectMap2 = int2ObjectMap != null ? int2ObjectMap : VillagerTrades.TRADES.get(resourceKey);
                } else {
                    int2ObjectMap2 = VillagerTrades.TRADES.get(resourceKey);
                }

                if (int2ObjectMap2 != null && !int2ObjectMap2.isEmpty()) {
                    itemListings = int2ObjectMap2.get(villagerData.level());
                    if (itemListings != null) {
                        merchantOffers = self.getOffers();
                        acc.wunderreich_addOffersFromItemListings(merchantOffers, itemListings, 2);
                    }
                }
            }
            //-------------------------------------
            found = CycleTradesMessage.hasSelectedTrades(self, merchantOffers);
            if (!found) {
                self.setOffers(new MerchantOffers());
                maxCount--;
            }
        } while (!found && maxCount > 0);
    }

    @Inject(method = "updateTrades", at = @At(value = "HEAD"), cancellable = true)
    void wunderreich_updateTrades(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        if (CycleTradesMessage.canSelectTrades(self)) {
            wunderreich_updateTradesProxy();
            ci.cancel();
        }
    }
}
