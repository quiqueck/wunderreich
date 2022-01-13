package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.interfaces.IMerchantMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import ru.bclib.util.Triple;

public class CycleTradesMessage {
    public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "cycle_trades");

    public static void register() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler,
                    CHANNEL,
                    (_server, _player, _handler, _buf, _responseSender) -> {
                        cycleTrades(_player);
                    });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayNetworking.unregisterReceiver(handler, CHANNEL);
        });
    }

    public static void send() {
        ClientPlayNetworking.send(CHANNEL, PacketByteBufs.create());
    }

    public static ItemStack holds(Player player, Item item) {
        if (player.getMainHandItem().is(item)) return player.getMainHandItem();
        if (player.getOffhandItem().is(item)) return player.getOffhandItem();
        return null;
    }

    public static ItemStack containsWhisperer(Player player) {
        ItemStack res = null;
        if (WunderreichConfigs.ITEM_CONFIG.isEnabled(WunderreichItems.BLANK_WHISPERER)) {
            res = holds(player, WunderreichItems.BLANK_WHISPERER);
        }
        if (res == null && WunderreichConfigs.ITEM_CONFIG.isEnabled(WunderreichItems.WHISPERER)) {
            if (player.getMainHandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = player.getOffhandItem();
            }
        }
        return res;
    }

    public static Triple<ItemStack, Player, EquipmentSlot> getClosestWhisperer(Villager villager, boolean doLog) {
        if (villager.level instanceof ServerLevel server) {
            Player p = server.getNearestPlayer(villager, 6);
            if (p == null) return null;

            final ItemStack mainHand = p.getMainHandItem();
            final ItemStack offHand = p.getOffhandItem();
            final EquipmentSlot slot;
            final ItemStack whisperer;
            if (mainHand.is(WunderreichItems.WHISPERER)) {
                whisperer = mainHand;
                slot = EquipmentSlot.MAINHAND;
            } else if (offHand.is(WunderreichItems.WHISPERER)) {
                whisperer = offHand;
                slot = EquipmentSlot.OFFHAND;
            } else {
                return null;
            }

            if (doLog) {
                Wunderreich.LOGGER.info("Player " + p.getName() + " uses Whisperer on Librarian");
            }
            return new Triple<>(whisperer, p, slot);
        }

        return null;
    }

    public static boolean canSelectTrades(Villager villager) {
        return canSelectTrades(villager, true);
    }

    public static boolean canSelectTrades(Villager villager, boolean doLog) {
        if (!WunderreichConfigs.MAIN.allowLibrarianSelection()) return false;
        if (villager == null || villager.getVillagerXp() > 0) return false;

        VillagerData villagerData = villager.getVillagerData();
        VillagerProfession profession = villagerData.getProfession();
        if (profession == null || !PoiType.LIBRARIAN.equals(profession.getJobPoiType())) return false;

        Triple<ItemStack, Player, EquipmentSlot> whispererStack = getClosestWhisperer(villager, doLog);
        if (whispererStack == null) return false;

        return true;
    }

    public static boolean hasSelectedTrades(Villager villager, MerchantOffers offers) {
        if (offers == null) return true;
        if (!canSelectTrades(villager, false)) return true;
        Triple<ItemStack, Player, EquipmentSlot> whispererStack = getClosestWhisperer(villager, false);
        if (whispererStack == null) return true;
        VillagerWhisperer whisperer = (VillagerWhisperer) whispererStack.first.getItem();

        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(Items.ENCHANTED_BOOK)) {
                var enchantments = EnchantedBookItem.getEnchantments(offer.getResult());
                if (!enchantments.isEmpty()) {
                    ResourceLocation type = EnchantmentHelper.getEnchantmentId(enchantments.getCompound(0));

                    final int duraCost = WunderreichConfigs.MAIN.cyclingNeedsWhisperer.get() ? 1 : 2;
                    if (whisperer instanceof TrainedVillagerWhisperer trained) {
                        if (type.equals(trained.getEnchantmentID(whispererStack.first))) {
                            whispererStack.first.hurtAndBreak(duraCost,
                                    whispererStack.second,
                                    player -> player.broadcastBreakEvent(whispererStack.third));
                            return true;
                        }
                    } else {
                        whispererStack.first.hurtAndBreak(duraCost,
                                whispererStack.second,
                                player -> player.broadcastBreakEvent(whispererStack.third));
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }


    //Code adopted from "Easy Villagers"
    public static void cycleTrades(ServerPlayer player) {
        if (!(player.containerMenu instanceof MerchantMenu)) {
            return;
        }
        if (!WunderreichConfigs.MAIN.allowTradesCycling.get()) return;
        MerchantMenu menu = (MerchantMenu) player.containerMenu;

        Villager villager = ((IMerchantMenu) menu).getVillager();
        if (villager == null || villager.getVillagerXp() > 0) {
            return;
        }

        if (WunderreichConfigs.MAIN.cyclingNeedsWhisperer.get()) {
            ItemStack whisp = containsWhisperer(player);
            if (whisp == null) return;
            whisp.hurtAndBreak(1,
                    player,
                    pp -> pp.broadcastBreakEvent(player.getMainHandItem().is(whisp.getItem())
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND));
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

        player.sendMerchantOffers(menu.containerId,
                villager.getOffers(),
                villager.getVillagerData().getLevel(),
                villager.getVillagerXp(),
                villager.showProgressBar(),
                villager.canRestock());
    }
}
