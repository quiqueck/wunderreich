package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.registries.WunderreichScreens;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WhispererMenu
        extends AbstractContainerMenu {
    protected static final int INGREDIENT_SLOT_A = 0;
    protected static final int INGREDIENT_SLOT_B = 1;
    protected static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int HOTBAR_SLOT_START = 30;
    private static final int HOTBAR_SLOT_END = 39;
    private static final int INGREDIENT_SLOT_A_X = 136;
    private static final int INGREDIENT_SLOT_B_X = 162;
    private static final int RESULT_SLOT_X = 220;
    private static final int ROW_Y = 37;
    private final WhisperContainer container;
    private final ContainerLevelAccess access;

    public WhispererMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public WhispererMenu(int containerId, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(WunderreichScreens.WHISPERER, containerId);
        container = new WhisperContainer();
        access= containerLevelAccess;


        this.addSlot(new Slot(this.container, INGREDIENT_SLOT_A, INGREDIENT_SLOT_A_X, ROW_Y));
        this.addSlot(new Slot(this.container, INGREDIENT_SLOT_B, INGREDIENT_SLOT_B_X, ROW_Y));
        this.addSlot(new WhispererResultSlot(inventory.player, this.container, RESULT_SLOT, RESULT_SLOT_X, ROW_Y));

        for (int i = 0; i < INV_SLOT_START; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + i * 9 + 9, 108 + k * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 108 + i * 18, 142));
        }
    }


    @Override
    public void slotsChanged(Container container) {
        this.container.updateResultItem();
        super.slotsChanged(container);
    }

    public void setSelectionHint(int ruleIndex) {
        this.container.setLastSelectedRule(ruleIndex);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return false;
    }

    private void playImprintSound(){
        this.access.execute((level, blockPos) -> {
            level.playSound(null,blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.1f + 0.9f);
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (i == RESULT_SLOT) {
                if (!this.moveItemStackTo(slotItem, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotItem, result);
                this.playImprintSound();
            } else if (i != INGREDIENT_SLOT_A && i != INGREDIENT_SLOT_B) {
                if (i >= INV_SLOT_START && i < INV_SLOT_END) {
                    if (!this.moveItemStackTo(slotItem, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= HOTBAR_SLOT_START && i < HOTBAR_SLOT_END && !this.moveItemStackTo(slotItem, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotItem.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotItem);
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
            ItemStack removedItem = this.container.removeItemNoUpdate(INGREDIENT_SLOT_A);
            if (!removedItem.isEmpty()) {
                player.drop(removedItem, false);
            }
            if (!(removedItem = this.container.removeItemNoUpdate(INGREDIENT_SLOT_B)).isEmpty()) {
                player.drop(removedItem, false);
            }
        } else if (player instanceof ServerPlayer) {
            player.getInventory().placeItemBackInInventory(this.container.removeItemNoUpdate(INGREDIENT_SLOT_A));
            player.getInventory().placeItemBackInInventory(this.container.removeItemNoUpdate(INGREDIENT_SLOT_B));
        }
    }

    public void tryMoveItems(int ruleIndex) {
        if (this.getEnchants().size() > ruleIndex) {
            ItemStack slotItem = this.container.getItem(INGREDIENT_SLOT_A);
            if (!slotItem.isEmpty()) {
                if (!this.moveItemStackTo(slotItem, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return;
                }

                this.container.setItem(INGREDIENT_SLOT_A, slotItem);
            }

            ItemStack containerItem = this.container.getItem(INGREDIENT_SLOT_B);
            if (!containerItem.isEmpty()) {
                if (!this.moveItemStackTo(containerItem, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return;
                }

                this.container.setItem(INGREDIENT_SLOT_B, containerItem);
            }

            if (this.container.getItem(INGREDIENT_SLOT_A).isEmpty() && this.container.getItem(INGREDIENT_SLOT_B).isEmpty()) {
                final WhisperRule rule = this.getEnchants().get(ruleIndex);
                ItemStack costA = rule.cost;
                this.moveFromInventoryToPaymentSlot(INGREDIENT_SLOT_A, costA);
                ItemStack costB = rule.costB;
                this.moveFromInventoryToPaymentSlot(INGREDIENT_SLOT_B, costB);
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int containerIndex, ItemStack inventory) {
        if (!inventory.isEmpty()) {
            for (int j = INV_SLOT_START; j < HOTBAR_SLOT_END; ++j) {
                final ItemStack slotStack = this.slots.get(j).getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(inventory, slotStack)) {
                    final ItemStack containerStack = this.container.getItem(containerIndex);
                    final int occupiedCount = containerStack.isEmpty() ? 0 : containerStack.getCount();
                    final int moveCount = Math.min(inventory.getMaxStackSize() - occupiedCount, slotStack.getCount());
                    final ItemStack result = slotStack.copy();
                    final int count = occupiedCount + moveCount;

                    slotStack.shrink(moveCount);
                    result.setCount(count);

                    this.container.setItem(containerIndex, result);
                    if (count >= inventory.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }
    }

    public List<WhisperRule> getEnchants() {
        return WhisperContainer.getAllEnchants();
    }
}

