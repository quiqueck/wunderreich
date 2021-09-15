package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.MainConfig;
import de.ambertation.wunderreich.interfaces.IMerchantMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;

public class CycleTradesMessage {
	public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "cycle_trades");
	public static void register(){
		ServerPlayConnectionEvents.INIT.register((handler, server)->{
			ServerPlayNetworking.registerReceiver(handler,CHANNEL, (_server, _player, _handler, _buf, _responseSender) -> {
				cycleTrades(_player);
			});
		});
		
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayNetworking.unregisterReceiver(handler, CHANNEL);
		});
	}
	
	public static void send(){
		ClientPlayNetworking.send(CHANNEL, PacketByteBufs.create());
	}
	
	//Code adopted from "Easy Villagers"
	public static void cycleTrades(ServerPlayer player){
		if (!(player.containerMenu instanceof MerchantMenu)) {
			return;
		}
		if (!Configs.MAIN.get(MainConfig.ALLOW_TRADES_CYCLING)) return;
		MerchantMenu menu = (MerchantMenu) player.containerMenu;
		
		Villager villager = ((IMerchantMenu)menu).getVillager();
		if (villager==null || villager.getVillagerXp() > 0){
			return;
		}
		
		villager.setOffers(null);
//		for (MerchantOffer merchantoffer : villager.getOffers()) {
//			merchantoffer.resetSpecialPriceDiff();
//		}
//
//		int i = villager.getPlayerReputation(Minecraft.getInstance().player);
//		if (i != 0) {
//			for (MerchantOffer merchantoffer : villager.getOffers()) {
//				merchantoffer.addToSpecialPriceDiff((int)(-Math.floor((float) i * merchantoffer.getPriceMultiplier())));
//			}
//		}
		
		player.sendMerchantOffers(menu.containerId, villager.getOffers(), villager.getVillagerData().getLevel(), villager.getVillagerXp(), villager.showProgressBar(), villager.canRestock());
	}
}
