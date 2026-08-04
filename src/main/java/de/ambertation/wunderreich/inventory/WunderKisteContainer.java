package de.ambertation.wunderreich.inventory;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.nbt.ItemStackHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class WunderKisteContainer extends SimpleContainer implements WorldlyContainer {
    private static final int[] slots = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
    };
    public final WunderKisteDomain.ID domain;

    public WunderKisteContainer(WunderKisteDomain.ID domain) {
        super(slots.length);
        this.domain = domain;
    }

    public void load(HolderLookup.Provider provider) {
        CompoundTag global = LevelData.getInstance().getWunderkisteInventory(domain);
        ListTag items;
        if (!global.contains("items")) {
            items = new ListTag();
            global.put("items", items);
        } else {
            items = global.getList("items").orElseThrow();
        }
        fromTag(provider, items);
    }

    public void save(HolderLookup.Provider provider) {
        CompoundTag global = LevelData.getInstance().getWunderkisteInventory(domain);
        global.put("items", createTag(provider));
        LevelData.getInstance().saveWunderkisteInventory(domain);
    }

    public void fromTag(HolderLookup.Provider provider, ListTag listTag) {
        int j;
        for (j = 0; j < this.getContainerSize(); ++j) {
            this.setItem(j, ItemStack.EMPTY);
        }

        for (j = 0; j < listTag.size(); ++j) {
            CompoundTag compoundTag = listTag.getCompound(j).orElse(null);
            if (compoundTag == null) continue;

            int k = compoundTag.getByteOr("Slot", (byte) 0) & 255;
            if (k < this.getContainerSize()) {
                this.setItem(k, ItemStackHelper.parseOptional(provider, compoundTag));
            }
        }

    }

    public ListTag createTag(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                var t = ItemStackHelper.save(itemStack, provider, compoundTag);
                listTag.add(t);
            }
        }

        return listTag;
    }

    public boolean stillValid(Player player) {
        final WunderKisteBlockEntity chest = ((ActiveChestStorage) player).getActiveWunderKiste();
        //return chest != null && !chest.stillValid(player) ? false : super.stillValid(player);
        return (chest == null || chest.stillValid(player)) && super.stillValid(player);
    }

    public void startOpen(Player player) {
        final WunderKisteBlockEntity chest = ((ActiveChestStorage) player).getActiveWunderKiste();
        if (chest != null) {
            chest.startOpen(player);
        }

        super.startOpen(player);
    }

    public void stopOpen(Player player) {
        final ActiveChestStorage cPlayer = (ActiveChestStorage) player;
        final WunderKisteBlockEntity chest = cPlayer.getActiveWunderKiste();
        if (chest != null) {
            save(player.registryAccess());
            chest.stopOpen(player);
        }

        super.stopOpen(player);
        cPlayer.setActiveWunderKiste(null);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        // Items may be inserted through every face, including DOWN. The suction tube sits below its
        // destination and therefore pushes into the bottom face; blocking DOWN here made it impossible
        // to feed a Wunderkiste from a suction tube. Vanilla hoppers never insert through DOWN anyway,
        // so allowing it does not change their behaviour.
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        // Items may be extracted through every face. A hopper below still empties the Wunderkiste
        // (it pulls through DOWN), and hoppers are the only vanilla block that extracts at all, so this
        // is only relevant for our own machines: the suction tube pulls through the face pointing at
        // it, which is never DOWN, and restricting that would make a Wunderkiste unusable as a source.
        return true;
    }


}