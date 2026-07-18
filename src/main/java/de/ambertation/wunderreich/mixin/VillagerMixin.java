package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.AbstractVillagerAccessor;
import de.ambertation.wunderreich.network.CycleTradesMessage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Villager.class, priority = 100)
public class VillagerMixin {

    /**
     * Wraps the original updateTrades logic in a while loop to ensure trades forced by an imprinter
     * are available. This method replicates the core logic from the (data-driven) updateTrades method.
     */
    private void wunderreich_updateTradesProxy(ServerLevel serverLevel) {
        Villager self = (Villager) (Object) this;
        AbstractVillagerAccessor acc = (AbstractVillagerAccessor) this;

        boolean found;
        MerchantOffers merchantOffers;
        int maxCount = 1000;
        do {
            //TODO: [MC Update] Check for changes in Villager.updateTrades(ServerLevel)
            //-------------------------------------
            merchantOffers = self.getOffers();
            VillagerData villagerData = self.getVillagerData();
            VillagerProfession profession = villagerData.profession().value();
            ResourceKey<TradeSet> tradeSet = profession.getTrades(villagerData.level());
            if (tradeSet != null) {
                acc.wunderreich_addOffersFromTradeSet(serverLevel, merchantOffers, tradeSet);
            }
            //-------------------------------------
            found = CycleTradesMessage.hasSelectedTrades(self, merchantOffers);
            if (!found) {
                self.setOffers(new MerchantOffers());
                maxCount--;
            }
        } while (!found && maxCount > 0);

        //If the retry budget was exhausted without ever rolling the whisperer's selected
        //enchantment, the last action inside the loop reset the offers to an empty list. Leaving
        //the villager with no offers soft-locks trading (it "refuses to trade" until its
        //workstation is rebuilt). Fall back to a regular, unfiltered trade set so the villager
        //always ends up with usable trades.
        if (!found) {
            merchantOffers = self.getOffers();
            VillagerData villagerData = self.getVillagerData();
            VillagerProfession profession = villagerData.profession().value();
            ResourceKey<TradeSet> tradeSet = profession.getTrades(villagerData.level());
            if (tradeSet != null) {
                acc.wunderreich_addOffersFromTradeSet(serverLevel, merchantOffers, tradeSet);
            }
        }
    }

    @Inject(method = "updateTrades", at = @At(value = "HEAD"), cancellable = true)
    void wunderreich_updateTrades(ServerLevel serverLevel, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        if (CycleTradesMessage.canSelectTrades(self)) {
            wunderreich_updateTradesProxy(serverLevel);
            ci.cancel();
        }
    }
}
