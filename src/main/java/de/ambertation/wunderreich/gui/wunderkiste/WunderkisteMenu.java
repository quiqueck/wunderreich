package de.ambertation.wunderreich.gui.wunderkiste;

import de.ambertation.wunderreich.inventory.WunderKisteContainer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WunderkisteMenu
        extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 9;
    private static final int SLOTS_IN_HOTBAR = SLOTS_PER_ROW;
    public static final int FIRST_CONTAINER_SLOT = 0;

    private static final int OFFSET_X = 8;
    private static final int OFFSET_Y = 18;
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = SLOT_WIDTH;
    private static final int INVENTORY_TOP_SPACE = 13;
    private static final int HOTBAR_TOP_SPACE = 4;
    private final WunderKisteContainer container;
    private final int containerRows;

    public WunderkisteMenu(int containerId, Inventory inventory, WunderKisteContainer container) {
        super(MenuType.GENERIC_9x3, containerId);
        final int rows = 3;
        AbstractContainerMenu.checkContainerSize(container, rows * SLOTS_PER_ROW);
        this.container = container;
        this.containerRows = rows;

        container.startOpen(inventory.player);

        final int INVENTORY_OFFSET_Y = this.containerRows * SLOT_HEIGHT + OFFSET_Y + INVENTORY_TOP_SPACE;
        final int HOTBAR_OFFSET_Y = 3 * SLOT_HEIGHT + INVENTORY_OFFSET_Y + HOTBAR_TOP_SPACE;
        for (int row = 0; row < this.containerRows; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                this.addSlot(new Slot(container,
                                      FIRST_CONTAINER_SLOT + col + row * SLOTS_PER_ROW,
                                      OFFSET_X + col * SLOT_WIDTH,
                                      OFFSET_Y + row * SLOT_HEIGHT));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                this.addSlot(new Slot(inventory,
                                      col + row * SLOTS_PER_ROW + SLOTS_IN_HOTBAR,
                                      OFFSET_X + col * SLOT_WIDTH,
                                      INVENTORY_OFFSET_Y + row * SLOT_HEIGHT));
            }
        }

        for (int col = 0; col < SLOTS_IN_HOTBAR; ++col) {
            this.addSlot(new Slot(inventory, col, OFFSET_X + col * SLOT_WIDTH, HOTBAR_OFFSET_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotID);
        if (slot != null && slot.hasItem()) {
            final ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (slotID < this.containerRows * 9
                    ? !this.moveItemStackTo(slotItem,
                                            this.containerRows * 9,
                                            this.slots.size(),
                                            true)
                    : !this.moveItemStackTo(slotItem, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }
            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}
