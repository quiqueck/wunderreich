package de.ambertation.wunderreich.gui.chronarium;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * Furnace-like menu for the {@link de.ambertation.wunderreich.blocks.Chronarium}.
 * <p>
 * Slot coordinates match the hand written {@code textures/gui/chronarium.png} (176x166 panel at
 * 0,0 of a 256x256 sheet).
 */
public class ChronariumMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = ChronariumBlockEntity.INPUT_SLOT;
    public static final int CATALYST_SLOT = ChronariumBlockEntity.CATALYST_SLOT;
    public static final int OUTPUT_SLOT = ChronariumBlockEntity.OUTPUT_SLOT;

    /**
     * Number of machine slots; the player inventory starts right after them.
     */
    public static final int CONTAINER_SLOT_COUNT = ChronariumBlockEntity.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = CONTAINER_SLOT_COUNT;      // 3
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;  // 30
    private static final int PLAYER_SLOTS_END = PLAYER_HOTBAR_START + 9;         // 39

    // Slot positions, relative to leftPos/topPos.
    public static final int INPUT_SLOT_X = 56;
    public static final int INPUT_SLOT_Y = 17;
    public static final int CATALYST_SLOT_X = 56;
    public static final int CATALYST_SLOT_Y = 53;
    public static final int OUTPUT_SLOT_X = 116;
    public static final int OUTPUT_SLOT_Y = 35;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 84;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /**
     * Client side constructor, used by the {@link net.minecraft.world.inventory.MenuType}. The real
     * contents arrive via the normal container sync.
     */
    public ChronariumMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(ChronariumBlockEntity.CONTAINER_SIZE),
                new SimpleContainerData(ChronariumBlockEntity.NUM_DATA_VALUES),
                ContainerLevelAccess.NULL
        );
    }

    /**
     * Server side constructor.
     */
    public ChronariumMenu(
            int containerId,
            Inventory playerInventory,
            ChronariumBlockEntity blockEntity,
            ContainerData data
    ) {
        this(
                containerId,
                playerInventory,
                blockEntity,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
        );
    }

    private ChronariumMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(WunderreichMenuTypes.CHRONARIUM, containerId);
        checkContainerSize(container, ChronariumBlockEntity.CONTAINER_SIZE);
        checkContainerDataCount(data, ChronariumBlockEntity.NUM_DATA_VALUES);

        this.container = container;
        this.data = data;
        this.access = access;

        this.addSlot(new Slot(container, INPUT_SLOT, INPUT_SLOT_X, INPUT_SLOT_Y));
        this.addSlot(new Slot(container, CATALYST_SLOT, CATALYST_SLOT_X, CATALYST_SLOT_Y));
        this.addSlot(new OutputSlot(playerInventory.player, container, OUTPUT_SLOT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));

        this.addStandardInventorySlots(playerInventory, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);

        this.addDataSlots(data);
    }

    // ---------------------------------------------------------------- screen helpers

    public int getProgress() {
        return this.data.get(ChronariumBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return this.data.get(ChronariumBlockEntity.DATA_MAX_PROGRESS);
    }

    public boolean isAging() {
        return getMaxProgress() > 0 && getProgress() > 0;
    }

    /**
     * The progress scaled onto {@code pixels}, clamped to {@code [0, pixels]}.
     */
    public int getProgressScaled(int pixels) {
        final int max = getMaxProgress();
        if (max <= 0) return 0;
        final int scaled = getProgress() * pixels / max;
        return Math.max(0, Math.min(pixels, scaled));
    }

    // ---------------------------------------------------------------- menu contract

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, WunderreichBlocks.CHRONARIUM);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        final Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return result;

        final ItemStack slotStack = slot.getItem();
        result = slotStack.copy();

        if (slotIndex == OUTPUT_SLOT) {
            // Finished items go back to the player, hotbar first (vanilla furnace behaviour).
            if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(slotStack, result);
        } else if (slotIndex == INPUT_SLOT || slotIndex == CATALYST_SLOT) {
            if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From the player inventory into the machine. Prefer the input slot; only fall back to
            // the catalyst slot when the input slot cannot take (any more of) this item, so a
            // second stack of the same kind still ends up somewhere sensible.
            if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)
                    && !this.moveItemStackTo(slotStack, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
                // Not accepted by the machine: shuffle between inventory and hotbar instead.
                if (slotIndex < PLAYER_HOTBAR_START) {
                    if (!this.moveItemStackTo(slotStack, PLAYER_HOTBAR_START, PLAYER_SLOTS_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return result;
    }

    /**
     * The result slot: items can only ever be taken out of it, never put in - and taking from it is
     * what pays out the experience the machine has banked.
     * <p>
     * This is {@code FurnaceResultSlot} with the names changed. The two details worth knowing:
     * <ul>
     *     <li>{@link #removeCount} accumulates across a shift-click, because
     *     {@code onQuickCraft}/{@code remove} can move a whole stack in several steps and
     *     {@code onCraftedBy} wants the total.</li>
     *     <li>The award lives <b>here</b>, in the menu, and not in
     *     {@link ChronariumBlockEntity#finish}. That is exactly why a hopper pulling the output
     *     never gets any experience: a hopper talks to the container directly and never touches a
     *     {@link Slot}. Vanilla behaves the same way, and automation forfeiting the experience is a
     *     deliberate part of the furnace's design rather than an accident.</li>
     * </ul>
     */
    private static class OutputSlot extends Slot {
        private final Player player;
        private int removeCount;

        OutputSlot(Player player, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            if (this.hasItem()) {
                this.removeCount += Math.min(amount, this.getItem().getCount());
            }

            return super.remove(amount);
        }

        @Override
        public void onTake(Player player, ItemStack carried) {
            this.checkTakeAchievements(carried);
            super.onTake(player, carried);
        }

        @Override
        protected void onQuickCraft(ItemStack picked, int count) {
            this.removeCount += count;
            this.checkTakeAchievements(picked);
        }

        @Override
        protected void checkTakeAchievements(ItemStack carried) {
            carried.onCraftedBy(this.player, this.removeCount);
            if (this.player instanceof ServerPlayer serverPlayer
                    && this.container instanceof ChronariumBlockEntity chronarium) {
                chronarium.awardUsedRecipesAndPopExperience(serverPlayer);
            }

            this.removeCount = 0;
        }
    }
}
