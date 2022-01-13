package de.ambertation.wunderreich.inventory;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class BoxOfEirContainer extends SimpleContainer implements WorldlyContainer {
    private static final int[] slots = {
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20,
            21,
            22,
            23,
            24,
            25,
            26
    };

    public BoxOfEirContainer() {
        super(slots.length);
    }

    public void load() {
        CompoundTag global = LevelData.getInstance().getGlobalInventory();
        ListTag items;
        if (!global.contains("items")) {
            items = new ListTag();
            global.put("items", items);
        } else {
            items = global.getList("items", 10);
        }
        fromTag(items);
    }

    public void save() {
        CompoundTag global = LevelData.getInstance().getGlobalInventory();
        global.put("items", createTag());
        LevelData.getInstance().saveLevelConfig();
    }

    public void fromTag(ListTag listTag) {
        int j;
        for (j = 0; j < this.getContainerSize(); ++j) {
            this.setItem(j, ItemStack.EMPTY);
        }

        for (j = 0; j < listTag.size(); ++j) {
            CompoundTag compoundTag = listTag.getCompound(j);
            int k = compoundTag.getByte("Slot") & 255;
            if (k < this.getContainerSize()) {
                this.setItem(k, ItemStack.of(compoundTag));
            }
        }

    }

    public ListTag createTag() {
        ListTag listTag = new ListTag();

        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                itemStack.save(compoundTag);
                listTag.add(compoundTag);
            }
        }

        return listTag;
    }

    public boolean stillValid(Player player) {
        final BoxOfEirBlockEntity chest = ((ActiveChestStorage) player).getActiveBoxOfEir();
        //return chest != null && !chest.stillValid(player) ? false : super.stillValid(player);
        return (chest == null || chest.stillValid(player)) && super.stillValid(player);
    }

    public void startOpen(Player player) {
        final BoxOfEirBlockEntity chest = ((ActiveChestStorage) player).getActiveBoxOfEir();
        if (chest != null) {
            chest.startOpen(player);
        }

        super.startOpen(player);
    }

    public void stopOpen(Player player) {
        final ActiveChestStorage cPlayer = (ActiveChestStorage) player;
        final BoxOfEirBlockEntity chest = cPlayer.getActiveBoxOfEir();
        if (chest != null) {
            chest.stopOpen(player);
        }

        super.stopOpen(player);
        cPlayer.setActiveBoxOfEir(null);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return direction == Direction.DOWN;
    }
}