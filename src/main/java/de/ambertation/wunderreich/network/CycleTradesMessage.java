package de.ambertation.wunderreich.network;

import de.ambertation.wunderlib.network.ServerBoundNetworkPayload;
import de.ambertation.wunderlib.network.ServerBoundPacketHandler;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.interfaces.IMerchantMenu;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
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
        return Objects.equals(stack, that.stack) && Objects.equals(
                player,
                that.player
        ) && slot == that.slot;
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

public class CycleTradesMessage extends ServerBoundNetworkPayload<CycleTradesMessage> {
    public static final ServerBoundPacketHandler<CycleTradesMessage> HANDLER = new ServerBoundPacketHandler<>(
            Wunderreich.ID("cycle_trades"),
            CycleTradesMessage::new
    );

    protected CycleTradesMessage(FriendlyByteBuf buf) {
        super(HANDLER);
    }

    protected CycleTradesMessage() {
        super(HANDLER);
    }

    public static ClosestWhisperer holds(Player player, Item item) {
        if (player.getMainHandItem().is(item))
            return new ClosestWhisperer(player.getMainHandItem(), player, EquipmentSlot.MAINHAND);
        if (player.getOffhandItem().is(item))
            return new ClosestWhisperer(player.getOffhandItem(), player, EquipmentSlot.OFFHAND);
        return null;
    }

    public static ClosestWhisperer containsWhisperer(Player player) {
        ClosestWhisperer res = null;
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BLANK_WHISPERER)) {
            res = holds(player, WunderreichItems.BLANK_WHISPERER);
        }
        if (res == null && Configs.ITEM_CONFIG.isEnabled(WunderreichItems.WHISPERER)) {
            if (player.getMainHandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = new ClosestWhisperer(player.getMainHandItem(), player, EquipmentSlot.MAINHAND);
            } else if (player.getOffhandItem().getItem() instanceof TrainedVillagerWhisperer) {
                res = new ClosestWhisperer(player.getOffhandItem(), player, EquipmentSlot.OFFHAND);
            }
        }
        return res;
    }

    public static ClosestWhisperer getClosestWhisperer(Villager villager, boolean doLog) {
        if (villager.level() instanceof ServerLevel server) {
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
        final Holder<VillagerProfession> professionHoler = villagerData.profession();
        if (!professionHoler.is(VillagerProfession.LIBRARIAN)) return false;
        //if (profession == null || !PoiType.LIBRARIAN.equals(profession.getJobPoiType())) return false;

        ClosestWhisperer whispererStack = getClosestWhisperer(villager, doLog);
        return whispererStack != null;
    }

    public static boolean hasSelectedTrades(Villager villager, MerchantOffers offers) {
        if (offers == null) return true;
        if (!canSelectTrades(villager, false)) return true;
        ClosestWhisperer whispererStack = getClosestWhisperer(villager, false);
        if (whispererStack == null) return true;
        VillagerWhisperer whisperer = (VillagerWhisperer) whispererStack.stack().getItem();

        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(Items.ENCHANTED_BOOK)) {
                final ItemStack results = offer.getResult();
                var enchantments = results.get(DataComponents.STORED_ENCHANTMENTS);
                if (!enchantments.isEmpty()) {
                    for (var enc : enchantments.entrySet()) {
                        final Holder<Enchantment> type = enc.getKey();

                        final int duraCost = WunderreichRules.Whispers.cyclingNeedsWhisperer() ? 1 : 2;
                        if (whisperer instanceof TrainedVillagerWhisperer trained) {
                            if (type.is(trained.getEnchantment(whispererStack.stack()))) {
                                whispererStack.stack().hurtAndBreak(
                                        duraCost,
                                        whispererStack.player(),
                                        whispererStack.slot()
                                );
                                return true;
                            }
                        } else {
                            whispererStack.stack().hurtAndBreak(
                                    duraCost,
                                    whispererStack.player(),
                                    whispererStack.slot()
                            );
                            return true;
                        }
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
                ClosestWhisperer whisp = containsWhisperer(player);
                if (whisp == null) return;
                whisp.stack().hurtAndBreak(
                        1,
                        whisp.player(),
                        whisp.slot()
                );
            }

            villager.setOffers(null);

            player.sendMerchantOffers(
                    menu.containerId,
                    villager.getOffers(),
                    villager.getVillagerData().level(),
                    villager.getVillagerXp(),
                    villager.showProgressBar(),
                    villager.canRestock()
            );
        }
    }

    public static void send() {
        ServerBoundPacketHandler.sendToServer(new CycleTradesMessage());
    }


    @Override
    protected void prepareOnClient() {

    }

    @Override
    protected void processOnServer(ServerPlayer player, PacketSender responseSender) {

    }

    @Override
    protected void processOnGameThread(MinecraftServer server, ServerPlayer player) {
        cycleTrades(player);
    }

    @Override
    protected void write(FriendlyByteBuf buf) {

    }

    protected record Content() {
    }
}
