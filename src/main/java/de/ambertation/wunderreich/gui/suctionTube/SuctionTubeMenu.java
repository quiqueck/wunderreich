package de.ambertation.wunderreich.gui.suctionTube;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.network.SuctionTubeContainerUpdatePacket;
import de.ambertation.wunderreich.registries.WunderreichMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Menu class for the Suction Tube configuration interface.
 * Provides filter slots for each input direction (DOWN, NORTH, EAST, SOUTH, WEST).
 * Each direction has 4 filter slots where players can place items to define what items
 * the tube should accept from that direction.
 */
public class SuctionTubeMenu extends AbstractContainerMenu {
    public static final int SLOTS_PER_DIRECTION = 4; // Each direction has 4 filter slots
    // GUI Layout Constants
    public static final int GUI_WIDTH = 306;
    public static final int GUI_HEIGHT = 180;
    public static final int SLOT_SIZE = 18;
    public static final int FILTER_CENTER_Y = 50;
    public static final int FILTER_SPACING_VERTICAL = 4;
    public static final int FILTER_SPACING_HORIZONTAL = 4;

    // Player inventory positioning
    public static final int PLAYER_INV_START_X = 73;
    public static final int PLAYER_HOTBAR_Y = GUI_HEIGHT - SLOT_SIZE - 6;
    public static final int PLAYER_INV_START_Y = PLAYER_HOTBAR_Y - 3 * SLOT_SIZE - 4;

    private static final int PLAYER_INVENTORY_START = 0;
    private static final int PLAYER_HOTBAR_START = 27;
    private static final int FILTER_SLOTS_START = 36;

    // 5 directions × 4 slots each = 20 filter slots
    private static final int FILTER_SLOTS_COUNT = SLOTS_PER_DIRECTION * SuctionTubeBlockEntity.DIRECTIONS.length;
    static final int FILTER_SLOTS_WIDTH = SLOTS_PER_DIRECTION * SLOT_SIZE;

    private final ContainerLevelAccess access;
    private final Map<Direction, Container> filterContainers;
    private final Map<Direction, ItemStack> containerConnections; // Cache for client-side
    private final Level level;
    private final SuctionTubeBlockEntity blockEntity;

    public SuctionTubeMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, ContainerLevelAccess.create(playerInventory.player.level(), pos));
    }

    /**
     * Encodes the input information into the stack count.
     * count & 0x0F is the signal strength (0-15)
     * count & 0x20 is the lock state (0 = unlocked, 32 = locked)
     * 0x10 is a fixed value to make sure count is always > 0
     *
     * @param isLocked
     * @param signalStrength
     * @return
     */
    public int encodeInputInfo(boolean isLocked, int signalStrength) {
        return 0x10 | (isLocked ? 0x20 : 0) | (signalStrength & 0xF);
    }

    public int decodeSignalStrength(int count) {
        // Decode the signal strength from the stack count
        return count & 0x0F; // Last 4 bits represent signal strength (0-15)
    }

    public boolean isDirectionLocked(int count) {
        // Check if the lock state is set in the stack count
        return (count & 0x20) != 0; // 5th bit indicates lock state (0 = unlocked, 32 = locked)
    }


    public SuctionTubeMenu(int containerId, Inventory playerInventory, BlockPos pos, ContainerLevelAccess access) {
        super(WunderreichMenuTypes.SUCTION_TUBE, containerId);

        this.access = access;
        this.filterContainers = new HashMap<>();
        this.containerConnections = new HashMap<>();

        // Get the block entity
        this.level = playerInventory.player.level();

        if (level.getBlockEntity(pos) instanceof SuctionTubeBlockEntity suctionTube) {
            blockEntity = suctionTube;
        } else {
            blockEntity = null; // No block entity found, handle gracefully
        }

        // Initialize container connection status
        if (!level.isClientSide() && blockEntity != null) {
            // Server-side: check actual container connections
            for (SuctionTubeBlockEntity.SuctionInput input : blockEntity.getInputs().getInputs()) {
                BlockPos checkPos = pos.relative(input.inDirection);
                boolean isLocked = blockEntity.getInputs().isDirectionDisabledByRedstone(input);
                int signalStrength = input.currentInputStrength();

                ItemStack stackWithState = new ItemStack(Blocks.BARRIER);
                if (input.hasContainer() || input.inputComperator() != null) {
                    BlockState state = level.getBlockState(checkPos);
                    stackWithState = state.getCloneItemStack(level, checkPos, false);
                }
                stackWithState.setCount(encodeInputInfo(isLocked, signalStrength));
                if (!stackWithState.isEmpty()) {
                    containerConnections.put(input.inDirection, stackWithState);
                }
            }

            // NOTE: the connection data must NOT be sent here. This constructor runs inside
            // ServerPlayer#openMenu -> createMenu, i.e. BEFORE the ClientboundOpenScreenPacket is
            // sent and before player.containerMenu is assigned. A packet sent now would arrive on
            // the client before the SuctionTubeMenu exists and would be silently dropped. Sending is
            // deferred to SuctionTubeBlockEntity#openMenu via sendConnectionsToClient(...).
        } else {
            // Client-side: leave containerConnections  empty, will be updated via network packet
        }

        // Initialize filter containers for each direction
        for (Direction direction : SuctionTubeBlockEntity.DIRECTIONS) {
            final boolean[] isInitializing = {true}; // Flag to prevent sync during initialization

            Container filterContainer = new SimpleContainer(SLOTS_PER_DIRECTION) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    // Only sync filter changes back to block entity after initialization
                    if (blockEntity != null && !isInitializing[0]) {
                        blockEntity.setFilterItems(direction, this);
                    }
                }
            };

            // Load existing filter items from block entity
            if (blockEntity != null) {
                ItemStack[] filterItems = blockEntity.getFilterItems(direction);
                for (int i = 0; i < Math.min(SLOTS_PER_DIRECTION, filterItems.length); i++) {
                    filterContainer.setItem(i, filterItems[i]);
                }
            }

            isInitializing[0] = false; // Enable sync after loading is complete

            filterContainers.put(direction, filterContainer);
        }

        // Add player inventory slots first (27 main inventory slots)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        playerInventory, col + row * 9 + PLAYER_INVENTORY_START,
                        PLAYER_INV_START_X + col * SLOT_SIZE,
                        PLAYER_INV_START_Y + row * SLOT_SIZE
                ));
            }
        }

        // Add player hotbar slots (9 hotbar slots)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    playerInventory, col + PLAYER_HOTBAR_START,
                    PLAYER_INV_START_X + col * SLOT_SIZE,
                    PLAYER_HOTBAR_Y
            ));
        }

        // Add filter slots (20 slots total: 5 directions × 4 slots each)
        // Position them in cross pattern:
        //       NNNN
        // WWWW  BBBB  EEEE 
        //       SSSS

        int[][] dirPositions = getAllFilterPositions();

        for (int dirIndex = 0; dirIndex < SuctionTubeBlockEntity.DIRECTIONS.length; dirIndex++) {
            Direction direction = SuctionTubeBlockEntity.DIRECTIONS[dirIndex];
            Container filterContainer = filterContainers.get(direction);

            int baseX = dirPositions[dirIndex][0];
            int baseY = dirPositions[dirIndex][1];

            for (int filterSlot = 0; filterSlot < SLOTS_PER_DIRECTION; filterSlot++) {
                int x = baseX + (filterSlot * SLOT_SIZE);

                this.addSlot(new FilterSlot(filterContainer, filterSlot, x, baseY));
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (slotIndex >= FILTER_SLOTS_START && slotIndex < FILTER_SLOTS_START + FILTER_SLOTS_COUNT) {
            // Filter slots are ghost slots: nothing real is stored here, so a shift-click can
            // never transfer a real item out of one. Only clear the displayed template,
            // mirroring a normal click on a filled filter slot. In practice this path is
            // unreachable: #clicked(...) below intercepts filter-slot indices before the
            // vanilla dispatch that would call this method, but it is kept safe here too in
            // case anything ever calls quickMoveStack directly.
            if (slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        if (slotIndex >= PLAYER_INVENTORY_START && slotIndex < PLAYER_HOTBAR_START + 9) {
            // Moving from player inventory to filter slots is not supported via shift-click.
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Intercepts every click gesture aimed at a filter-slot index and handles it directly,
     * instead of delegating to the vanilla {@code AbstractContainerMenu#doClick} dispatch.
     * <p>
     * This is necessary (not just a defensive nicety) because several vanilla dispatch
     * branches cannot be neutralized purely via {@link Slot} overrides:
     * <ul>
     *   <li>{@code ContainerInput.SWAP} (pressing 1-9/F while hovering a slot) reads
     *   {@code target.getItem()} and hands it straight to the player's hotbar, gated only by
     *   {@code Slot#mayPickup} - there is no hook to intercept the hand-off itself.</li>
     *   <li>Clicking a filled slot while holding a <em>different</em> item runs a "swap"
     *   branch that puts the slot's old contents on the cursor unconditionally once
     *   {@code Slot#mayPlace} allows entry - again with no interceptable hook.</li>
     * </ul>
     * Since both branches would otherwise hand the player a free copy of whatever template
     * item was set (a real duplication bug), filter-slot clicks are fully handled here
     * instead, where only {@link Slot#set} is ever used - the player's cursor and inventory
     * stacks are read but never mutated.
     */
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex >= FILTER_SLOTS_START && slotIndex < FILTER_SLOTS_START + FILTER_SLOTS_COUNT) {
            handleFilterSlotClick(slotIndex, containerInput, player);
            return;
        }
        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    /**
     * Ghost-slot click handling for a single filter slot. Sets/overwrites/clears the
     * displayed template item as appropriate, but never mutates the player's carried item or
     * inventory contents - see {@link #clicked} for why this bypasses vanilla dispatch.
     */
    private void handleFilterSlotClick(int slotIndex, ContainerInput containerInput, Player player) {
        Slot slot = this.slots.get(slotIndex);

        if (containerInput == ContainerInput.PICKUP) {
            ItemStack carried = this.getCarried();
            if (!carried.isEmpty()) {
                // Set (or overwrite) the template with a single copy of the held item's type.
                // The cursor stack itself is left completely untouched.
                ItemStack singleCopy = carried.copy();
                singleCopy.setCount(1);
                slot.set(singleCopy);
            } else if (slot.hasItem()) {
                // Clicking a filled filter slot with an empty cursor clears the template.
                // Nothing is ever handed back, since nothing was ever really taken.
                slot.set(ItemStack.EMPTY);
            }
        } else if (containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.THROW) {
            // Shift-click / drop gesture: just clear the template, never move a real item.
            if (slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
            }
        }
        // SWAP, CLONE, PICKUP_ALL, QUICK_CRAFT: intentionally left as no-ops for filter
        // slots. None of these gestures map cleanly to "set/clear template", and (as
        // documented on #clicked) their vanilla implementations cannot be trusted not to
        // hand out or duplicate a real item for what is only ever a template.
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, de.ambertation.wunderreich.registries.WunderreichBlocks.SUCTION_TUBE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Save filter data to block entity when menu is closed
        if (blockEntity != null && !player.level().isClientSide()) {
            for (Direction direction : SuctionTubeBlockEntity.DIRECTIONS) {
                Container filterContainer = filterContainers.get(direction);
                if (filterContainer != null) {
                    blockEntity.setFilterItems(direction, filterContainer);
                }
            }
            blockEntity.setChanged();
        }
    }

    /**
     * Sends the (server-computed) container connection data to the viewing client.
     * <p>
     * Must be called <em>after</em> the menu has been opened (i.e. after
     * {@code ServerPlayer#openMenu}) so that the client's {@code containerMenu} is already the
     * {@link SuctionTubeMenu} when the packet arrives. See the note in the constructor.
     */
    public void sendConnectionsToClient(ServerPlayer serverPlayer) {
        if (!containerConnections.isEmpty()) {
            SuctionTubeContainerUpdatePacket.send(serverPlayer, containerConnections);
        }
    }

    /**
     * Updates container connection status (called from network packet on client-side).
     *
     * @param connections Map of direction -> hasContainer
     */
    public void updateContainerConnections(Map<Direction, ItemStack> connections) {
        this.containerConnections.clear();
        this.containerConnections.putAll(connections);
    }

    /**
     * Gets the position for filter slots of a given direction.
     * Returns [x, y] coordinates for the first slot of that direction.
     */
    public static int[] getFilterPosition(Direction direction) {
        int iconW = SLOT_SIZE + 4;
        final int FILTER_WIDTH = FILTER_SLOTS_WIDTH + iconW;

        int downX = (GUI_WIDTH - FILTER_WIDTH) / 2;
        int downY = FILTER_CENTER_Y;

        return switch (direction) {
            case NORTH -> new int[]{
                    downX + iconW,
                    downY - (SLOT_SIZE + FILTER_SPACING_VERTICAL),
                    downX
            };
            case WEST -> new int[]{
                    downX - FILTER_WIDTH - FILTER_SPACING_HORIZONTAL + iconW,
                    downY,
                    downX - FILTER_WIDTH - FILTER_SPACING_HORIZONTAL
            };
            case SOUTH -> new int[]{
                    downX + iconW,
                    downY + (SLOT_SIZE + FILTER_SPACING_VERTICAL),
                    downX
            };
            case EAST -> new int[]{
                    downX + FILTER_WIDTH + FILTER_SPACING_HORIZONTAL + iconW,
                    downY,
                    downX + FILTER_WIDTH + FILTER_SPACING_HORIZONTAL
            };
            default -> new int[]{downX + iconW, downY, downX};
        };
    }

    /**
     * Gets all filter positions in the order they appear in DIRECTIONS array.
     */
    public static int[][] getAllFilterPositions() {
        int[][] positions = new int[SuctionTubeBlockEntity.DIRECTIONS.length][];
        for (int i = 0; i < SuctionTubeBlockEntity.DIRECTIONS.length; i++) {
            positions[i] = getFilterPosition(SuctionTubeBlockEntity.DIRECTIONS[i]);
        }
        return positions;
    }

    /**
     * Checks if a specific direction has a connected container.
     *
     * @param direction The direction to check
     * @return true if there's a container connected in that direction
     */
    public boolean hasConnectedContainer(Direction direction) {
        // Use cached value synced from server
        final ItemStack stack = containerConnections.getOrDefault(direction, null);
        // A connection counts as a container unless it is a comparator (comparators drive redstone
        // control, not item transfer). A stack is never both comparator and barrier at once.
        return stack != null && !stack.is(Blocks.COMPARATOR.asItem());
    }

    public boolean hasConnectedItem(Direction direction) {
        ItemStack stack = containerConnections.get(direction);
        return stack != null && !(stack.is(Blocks.BARRIER.asItem()));
    }

    public boolean isLockedDirection(Direction direction) {
        // Check if the direction is locked based on the stack count encoding
        ItemStack stack = containerConnections.get(direction);
        return stack != null && (stack.getCount() & 0x20) != 0; // Check if the lock bit is set
    }

    public int signalStrengthForDirection(Direction direction) {
        // Decode the signal strength from the stack count encoding
        ItemStack stack = containerConnections.get(direction);
        if (stack != null) {
            return decodeSignalStrength(stack.getCount()); // Last 4 bits represent signal strength (0-15)
        }
        return 0; // Default to 0 if no connection
    }

    /**
     * Gets the connected container for a specific direction.
     *
     * @param direction The direction to check
     * @return The container if connected, null otherwise
     */
    @Nullable
    public ItemStack getConnectedContainerItem(Direction direction) {
        return containerConnections.get(direction);
    }

    /**
     * Custom slot for filter items. Filter slots are pure "ghost" slots: the displayed item
     * is only a template used for the block entity's item-matching logic (see
     * {@code SuctionTubeBlockEntity#getFilterItems}) and is never real, storable inventory.
     * {@code getItem()} intentionally keeps the default behavior (returns the container's
     * stored template) so rendering and filter matching keep working unchanged.
     * <p>
     * Actually setting/clearing the template is handled entirely by
     * {@link SuctionTubeMenu#clicked}, which intercepts every click gesture aimed at a
     * filter-slot index before it reaches the vanilla dispatch in the private, non-overridable
     * {@code AbstractContainerMenu#doClick}. The overrides below are a defensive second line
     * of protection, not the primary mechanism: {@link #mayPlace} / {@link #mayPickup} both
     * report "no" so that if a vanilla dispatch path is ever reached for this slot regardless
     * (e.g. a quick-craft drag skipping this slot, or a PICKUP_ALL double-click sweep
     * triggered from an unrelated slot), it can neither insert nor remove a real item; the
     * {@link #safeInsert} / {@link #remove} overrides make even a hypothetical direct call
     * safe, touching only the displayed template and never the player's cursor or inventory.
     */
    private static class FilterSlot extends Slot {
        public FilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // Never a real placement target - see class javadoc.
        }

        @Override
        public boolean mayPickup(Player player) {
            return false; // Never a real pickup source - see class javadoc.
        }

        @Override
        public int getMaxStackSize() {
            return 1; // Only ever displays a single template item.
        }

        @Override
        public @NotNull ItemStack safeInsert(ItemStack inputStack, int inputAmount) {
            // Defensive fallback only (see class javadoc): set the template to a single copy
            // of the item type without ever consuming from the input stack.
            if (!inputStack.isEmpty()) {
                ItemStack singleCopy = inputStack.copy();
                singleCopy.setCount(1);
                this.set(singleCopy);
            } else {
                this.set(ItemStack.EMPTY);
            }
            return inputStack;
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            // Defensive fallback only (see class javadoc): clear the template but never
            // report a real item as having been removed.
            this.set(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
    }
}
