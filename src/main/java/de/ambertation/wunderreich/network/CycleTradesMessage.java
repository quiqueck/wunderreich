package de.ambertation.wunderreich.network;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.interfaces.IMerchantMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.util.Objects;

record ClosestWhisperer(ItemStack stack, Player player, EquipmentSlot slot) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClosestWhisperer)) return false;
        ClosestWhisperer that = (ClosestWhisperer) o;
        return Objects.equals(stack, that.stack) && Objects.equals(player,
                that.player) && slot == that.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, player, slot);
    }

    @Override
    public String toString() {
        return "ClosestWhisperer{" +
                "stack=" + stack +
                ", player=" + player +
                ", slot=" + slot +
                '}';
    }
}

public class CycleTradesMessage extends ServerBoundPacketHandler<CycleTradesMessage.Content> {
    public static final CycleTradesMessage INSTANCE = ServerBoundPacketHandler.register("cycle_trades",
            new CycleTradesMessage());

    protected CycleTradesMessage() {
    }

    public static ItemStack holds(Player player, Item item) {
        if (player.getMainHandItem().is(item)) return player.getMainHandItem();
        if (player.getOffhandItem().is(item)) return player.getOffhandItem();
        return null;
    }

    public static ItemStack containsWhisperer(Player player) {
        ItemStack res = null;
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BLANK_WHISPERER)) {
            res = holds(player, WunderreichItems.BLANK_WHISPERER);
        }
        if (res == null && Configs.ITEM_CONFIG.isEnabled(WunderreichItems.WHISPERER)) {
            if (player.getMainHandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = player.getOffhandItem();
            }
        }
        return res;
    }

    public static ClosestWhisperer getClosestWhisperer(Villager villager, boolean doLog) {
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
            return new ClosestWhisperer(whisperer, p, slot);
        }

        return null;
    }

    public static boolean canSelectTrades(Villager villager) {
        return canSelectTrades(villager, true);
    }

    public static boolean canSelectTrades(Villager villager, boolean doLog) {
        if (!WunderreichRules.Whispers.allowLibrarianSelection()) return false;
        if (villager == null || villager.getVillagerXp() > 0) return false;

        VillagerData villagerData = villager.getVillagerData();
        VillagerProfession profession = villagerData.getProfession();
        if (VillagerProfession.LIBRARIAN.equals(profession)) return false;
        //if (profession == null || !PoiType.LIBRARIAN.equals(profession.getJobPoiType())) return false;

        ClosestWhisperer whispererStack = getClosestWhisperer(villager, doLog);
        return whispererStack != null;
    }

//    public final static ResourceLocation CHANNEL = new ResourceLocation(Wunderreich.MOD_ID, "cycle_trades");
//    public static void register() {
//        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
//            ServerPlayNetworking.registerReceiver(handler,
//                    CHANNEL,
//                    (_server, _player, _handler, _buf, _responseSender) -> {
//                        cycleTrades(_player);
//                    });
//        });
//
//        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
//            ServerPlayNetworking.unregisterReceiver(handler, CHANNEL);
//        });
//    }
//
//    public static void send() {
//        ClientPlayNetworking.send(CHANNEL, PacketByteBufs.create());
//    }

    public static boolean hasSelectedTrades(Villager villager, MerchantOffers offers) {
        if (offers == null) return true;
        if (!canSelectTrades(villager, false)) return true;
        ClosestWhisperer whispererStack = getClosestWhisperer(villager, false);
        if (whispererStack == null) return true;
        VillagerWhisperer whisperer = (VillagerWhisperer) whispererStack.stack().getItem();

        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(Items.ENCHANTED_BOOK)) {
                var enchantments = EnchantedBookItem.getEnchantments(offer.getResult());
                if (!enchantments.isEmpty()) {
                    ResourceLocation type = EnchantmentHelper.getEnchantmentId(enchantments.getCompound(0));

                    final int duraCost = WunderreichRules.Whispers.cyclingNeedsWhisperer() ? 1 : 2;
                    if (whisperer instanceof TrainedVillagerWhisperer trained) {
                        if (type.equals(trained.getEnchantmentID(whispererStack.stack()))) {
                            whispererStack.stack().hurtAndBreak(duraCost,
                                    whispererStack.player(),
                                    player -> player.broadcastBreakEvent(whispererStack.slot()));
                            return true;
                        }
                    } else {
                        whispererStack.stack().hurtAndBreak(duraCost,
                                whispererStack.player(),
                                player -> player.broadcastBreakEvent(whispererStack.slot()));
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
        if (!WunderreichRules.Whispers.allowTradesCycling()) return;
        MerchantMenu menu = (MerchantMenu) player.containerMenu;

        if (menu instanceof IMerchantMenu mmenu) {
            Villager villager = mmenu.wunder_getVillager();
            if (villager == null || villager.getVillagerXp() > 0) {
                return;
            }

            if (WunderreichRules.Whispers.cyclingNeedsWhisperer()) {
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

    public void send() {
        sendToServer(null);
    }

    @Override
    protected void serializeOnClient(FriendlyByteBuf buf, Content content) {

    }

    @Override
    protected Content deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender) {
        return null;
    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player, Content content) {
        cycleTrades(player);
    }

    protected record Content() {
    }
}
