package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Suction Tube block that transfers items from surrounding containers
 * to a container above it, with redstone signal control for selective direction disabling
 * and redstone signal output when items are transferred.
 *
 * <h3>Basic Functionality:</h3>
 * The Suction Tube pulls items from containers in the following directions relative to itself:
 * <ul>
 *   <li>DOWN (bottom face)</li>
 *   <li>NORTH (horizontal)</li>
 *   <li>EAST (horizontal)</li>
 *   <li>SOUTH (horizontal)</li>
 *   <li>WEST (horizontal)</li>
 * </ul>
 * Items are transferred to the container located above the Suction Tube (UP direction).
 *
 * <h3>Redstone Signal Control (Input):</h3>
 * The Suction Tube accepts redstone signals from comparators facing INTO the block to selectively disable
 * item transfer from specific sides. The redstone signal strength is used as a bitmask where
 * each bit corresponds to a direction according to this order:
 * <ul>
 *   <li>Bit 0: NORTH direction</li>
 *   <li>Bit 1: EAST direction</li>
 *   <li>Bit 2: SOUTH direction</li>
 *   <li>Bit 3: WEST direction</li>
 * </ul>
 * <p>
 * When a bit is set (1), the corresponding direction is <strong>disabled</strong> for item transfer.
 * Only comparators that are oriented to face into the Suction Tube block are considered for input.
 *
 * <h3>Redstone Signal Output:</h3>
 * When an item is successfully transferred, the Suction Tube emits a redstone signal for one tick.
 * The signal strength corresponds to the direction from which the item was taken:
 * <ul>
 *   <li>DOWN direction: Signal strength 1 (bit 0)</li>
 *   <li>NORTH direction: Signal strength 1 (bit 0)</li>
 *   <li>EAST direction: Signal strength 2 (bit 1)</li>
 *   <li>SOUTH direction: Signal strength 4 (bit 2)</li>
 *   <li>WEST direction: Signal strength 8 (bit 3)</li>
 * </ul>
 * <p>
 * Comparators and repeaters facing AWAY from the block will detect this output signal.
 *
 * <h3>Usage Examples:</h3>
 * <ul>
 *   <li><strong>Input Control - Disable EAST side:</strong> Place a comparator on the EAST side facing INTO the block,
 *       outputting signal strength 2 (binary: 0010, bit 1 set) - this disables item transfer from the EAST direction</li>
 *   <li><strong>Output Detection:</strong> Place a comparator on any side facing AWAY from the block to detect
 *       when items are transferred and from which direction they came</li>
 *   <li><strong>Combined Usage:</strong> Use multiple comparators - some facing in for control, others facing out for detection</li>
 * </ul>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li>Transfer cooldown: 8 ticks (same as vanilla hopper)</li>
 *   <li>Transfers one item at a time</li>
 *   <li>Randomizes source container checking order to prevent bias</li>
 *   <li>Respects WorldlyContainer face restrictions</li>
 *   <li>Output signal duration: 1 tick</li>
 * </ul>
 */
public class SuctionTubeBlockEntity extends BlockEntity implements MenuProvider {
    private int transferCooldown = 0;
    private static final int TRANSFER_COOLDOWN = 8; // Same as hopper
    private final Random random = new Random();

    // Redstone output fields
    private int redstoneOutputSignal = 0;
    private int redstoneOutputTicks = 0;

    // Filter storage: direction -> array of 4 filter items
    private final Map<Direction, ItemStack[]> filterItems = new HashMap<>();

    public SuctionTubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE, blockPos, blockState);

        // Initialize filter arrays for each direction
        for (Direction direction : DIRECTIONS) {
            filterItems.put(direction, new ItemStack[4]);
            for (int i = 0; i < 4; i++) {
                filterItems.get(direction)[i] = ItemStack.EMPTY;
            }
        }

        shuffleSourceContainerOrder();
    }

    public SuctionTubeBlockEntity(
            BlockEntityType<?> blockEntityType,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(blockEntityType, blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SuctionTubeBlockEntity blockEntity) {
        if (level.isClientSide) return;

        // Handle redstone output timing
        if (blockEntity.redstoneOutputTicks > 0) {
            blockEntity.redstoneOutputTicks--;
            if (blockEntity.redstoneOutputTicks <= 0) {
                blockEntity.redstoneOutputSignal = 0;
                level.updateNeighborsAt(pos, state.getBlock());
            }
        }

        --blockEntity.transferCooldown;
        if (blockEntity.transferCooldown <= 0) {
            blockEntity.transferCooldown = TRANSFER_COOLDOWN;
            blockEntity.tryTransferItem();
        }
    }

    // Directions for the containers relative to the suction tube
    public static final Direction[] DIRECTIONS = {
            Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    // Randomized order of container indices to try transferring from
    private static final int[] CONTAINER_INDEX_ORDER = {0, 1, 2, 3, 4};

    /**
     * Attempts to transfer one item from any available source container to the destination container above.
     *
     * <p>This method:
     * <ol>
     *   <li>Checks for a valid destination container above the Suction Tube</li>
     *   <li>Gets the current redstone disable mask to determine which directions are disabled</li>
     *   <li>Iterates through source containers in randomized order</li>
     *   <li>Skips directions that are disabled by redstone signals</li>
     *   <li>Attempts to transfer one item from the first available source</li>
     *   <li>Re-randomizes the order for the next transfer attempt if successful</li>
     * </ol>
     *
     * <p>The randomization ensures fair distribution when multiple source containers are available.
     * Redstone control allows selective disabling of specific source directions.
     */
    private void tryTransferItem() {
        if (level == null || level.isClientSide) return;

        // Get container above (destination)
        BlockPos abovePos = worldPosition.above();
        Container destContainer = getContainerAt(level, abovePos);

        if (destContainer == null) return;

        // Get redstone disable mask
        int redstoneDisableMask = getRedstoneDisableMask();

        //randomly pick one available container to transfer from without adding a new datastructure
        for (int i : CONTAINER_INDEX_ORDER) {
            // Check if this direction is disabled by redstone
            if (isDirectionDisabledByRedstone(DIRECTIONS[i], redstoneDisableMask)) {
                continue; // Skip this direction
            }

            Container c = getContainerAt(level, worldPosition.relative(DIRECTIONS[i]));
            if (c != null && transferItemFromToWithFilter(c, destContainer, DIRECTIONS[i])) {
                // Emit redstone signal based on the direction the item came from
                emitRedstoneSignalForTransfer(DIRECTIONS[i], i);
                shuffleSourceContainerOrder();
                return; // Successfully transferred an item
            }
        }
    }

    /**
     * Checks if the given item stack passes the filter for the specified direction.
     * If no filter items are set for a direction, all items pass.
     * If filter items are set, only items matching the filter pass.
     */
    private boolean passesFilter(ItemStack itemStack, Direction direction) {
        ItemStack[] filters = filterItems.get(direction);
        if (filters == null) return true;

        // Check if any filter slot is set
        boolean hasFilters = false;
        for (ItemStack filter : filters) {
            if (!filter.isEmpty()) {
                hasFilters = true;
                if (ItemStack.isSameItemSameComponents(itemStack, filter)) {
                    return true;
                }
            }
        }

        // If no filters are set, allow all items
        return !hasFilters;
    }

    // Shuffle the CONTAINER_INDEX_ORDER array to randomize the next transfer attempt

    private void shuffleSourceContainerOrder() {
        for (int cIndx = 0; cIndx < CONTAINER_INDEX_ORDER.length; cIndx++) {
            int randomIndex = random.nextInt(CONTAINER_INDEX_ORDER.length);
            int temp = CONTAINER_INDEX_ORDER[cIndx];
            CONTAINER_INDEX_ORDER[cIndx] = CONTAINER_INDEX_ORDER[randomIndex];
            CONTAINER_INDEX_ORDER[randomIndex] = temp;
        }
    }

    /**
     * Gets the redstone disable mask by checking redstone signals from comparators facing into this block.
     * Only comparators that are oriented to face into the Suction Tube are considered.
     * The signal strength from each valid direction is combined using bitwise OR to create a unified
     * bitmask that determines which directions should be disabled.
     *
     * <p>Each bit in the returned mask corresponds to a direction:
     * <ul>
     *   <li>Bit 0 (value 1): NORTH direction</li>
     *   <li>Bit 1 (value 2): EAST direction</li>
     *   <li>Bit 2 (value 4): SOUTH direction</li>
     *   <li>Bit 3 (value 8): WEST direction</li>
     * </ul>
     *
     * @return Combined redstone disable mask (0-15), where each set bit disables the corresponding direction
     */
    private int getRedstoneDisableMask() {
        if (level == null) return 0;

        int combinedMask = 0;

        // Check redstone signal from each horizontal direction
        for (int i = 1; i < DIRECTIONS.length; i++) { // Start from 1 to skip DOWN
            Direction direction = DIRECTIONS[i];
            BlockPos signalPos = worldPosition.relative(direction);

            // Only accept signal if there's a comparator facing into this block
            if (isComparatorFacingInto(signalPos, direction)) {
                int signalStrength = level.getSignal(signalPos, direction);
                // Use the signal strength as a bitmask
                combinedMask |= signalStrength;
            }
        }

        return combinedMask;
    }

    /**
     * Checks if a specific direction is disabled by the redstone signal.
     *
     * <p>For horizontal directions (NORTH, EAST, SOUTH, WEST), this method checks if the
     * corresponding bit is set in the redstone disable mask.
     *
     * <p>For the DOWN direction (bottom face), the control is more complex:
     * The bottom face is controlled by whichever horizontal direction is currently providing
     * a redstone signal. The bit that gets checked corresponds to the direction providing
     * the signal, not the bottom face itself.
     *
     * <p>Examples:
     * <ul>
     *   <li>If EAST provides signal strength 1 (bit 0 set), and bit 0 corresponds to NORTH,
     *       then the bottom face will be disabled</li>
     *   <li>If NORTH provides signal strength 2 (bit 1 set), and bit 1 corresponds to EAST,
     *       then both the EAST horizontal direction and potentially the bottom face are affected</li>
     * </ul>
     *
     * @param direction           The direction to check (DOWN, NORTH, EAST, SOUTH, or WEST)
     * @param redstoneDisableMask The redstone disable mask obtained from {@link #getRedstoneDisableMask()}
     * @return true if the direction is disabled and should not transfer items, false otherwise
     */
    private boolean isDirectionDisabledByRedstone(Direction direction, int redstoneDisableMask) {
        // DOWN (bottom) is controlled by horizontal redstone inputs
        if (direction == Direction.DOWN) {
            // Find which comparator input controls the bottom face
            // This is determined by which direction has a comparator facing into the block with a signal
            for (int bitIndex = 1; bitIndex < DIRECTIONS.length; bitIndex++) {
                Direction redstoneDirection = DIRECTIONS[bitIndex];
                BlockPos signalPos = worldPosition.relative(redstoneDirection);

                // Only check if there's a comparator facing into this block
                if (isComparatorFacingInto(signalPos, redstoneDirection)) {
                    int signalStrength = level.getSignal(signalPos, redstoneDirection);

                    if (signalStrength > 0) {
                        // Check if the bit corresponding to this redstone input direction is set
                        return (redstoneDisableMask & (1 << (bitIndex - 1))) != 0;
                    }
                }
            }
            return false;
        }

        // For horizontal directions, check the corresponding bit
        for (int bitIndex = 1; bitIndex < DIRECTIONS.length; bitIndex++) {
            if (DIRECTIONS[bitIndex] == direction) {
                return (redstoneDisableMask & (1 << (bitIndex - 1))) != 0;
            }
        }

        return false;
    }

    /**
     * Checks if there is a comparator at the given position that is facing into this block.
     *
     * @param comparatorPos     The position to check for a comparator
     * @param directionFromTube The direction from the tube to the comparator position
     * @return true if there's a comparator facing into this block, false otherwise
     */
    private boolean isComparatorFacingInto(BlockPos comparatorPos, Direction directionFromTube) {
        if (level == null) return false;

        BlockState blockState = level.getBlockState(comparatorPos);

        // Check if it's a comparator block
        if (!blockState.is(net.minecraft.world.level.block.Blocks.COMPARATOR)) {
            return false;
        }

        // Get the facing direction of the comparator
        Direction comparatorFacing = blockState.getValue(net.minecraft.world.level.block.ComparatorBlock.FACING);

        // The comparator should be facing the opposite direction of directionFromTube
        // (i.e., facing into the tube)
        return comparatorFacing == directionFromTube;
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            return container;
        }
        return HopperBlockEntity.getContainerAt(level, pos);
    }

    private static boolean transferItemFromTo(Container source, Container destination, Direction direction) {
        // Get the block entity to access filter
        if (source instanceof WorldlyContainer worldlySource) {
            int[] slots = worldlySource.getSlotsForFace(direction.getOpposite());
            for (int slot : slots) {
                if (tryTakeAndTransfer(source, destination, slot, direction)) {
                    return true;
                }
            }
        } else {
            int containerSize = source.getContainerSize();
            for (int i = 0; i < containerSize; ++i) {
                if (tryTakeAndTransfer(source, destination, i, direction)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean transferItemFromToWithFilter(Container source, Container destination, Direction direction) {
        if (source instanceof WorldlyContainer worldlySource) {
            int[] slots = worldlySource.getSlotsForFace(direction.getOpposite());
            for (int slot : slots) {
                ItemStack sourceStack = source.getItem(slot);
                if (!sourceStack.isEmpty() && passesFilter(sourceStack, direction)) {
                    if (tryTakeAndTransfer(source, destination, slot, direction)) {
                        return true;
                    }
                }
            }
        } else {
            int containerSize = source.getContainerSize();
            for (int i = 0; i < containerSize; ++i) {
                ItemStack sourceStack = source.getItem(i);
                if (!sourceStack.isEmpty() && passesFilter(sourceStack, direction)) {
                    if (tryTakeAndTransfer(source, destination, i, direction)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean tryTakeAndTransfer(
            Container source,
            Container destination,
            int sourceSlot,
            Direction direction
    ) {
        ItemStack sourceStack = source.getItem(sourceSlot);
        if (sourceStack.isEmpty()) {
            return false;
        }

        // Check if we can extract from source
        if (source instanceof WorldlyContainer worldlySource && !worldlySource.canTakeItemThroughFace(
                sourceSlot,
                sourceStack,
                direction.getOpposite()
        )) {
            return false;
        }

        ItemStack extractedStack = sourceStack.copy();
        extractedStack.setCount(1); // Extract one item at a time

        // Try to insert into destination
        ItemStack remainingStack = addItem(destination, extractedStack, direction);
        if (remainingStack.isEmpty()) {
            // Successfully transferred, remove from source
            sourceStack.shrink(1);
            source.setChanged();
            destination.setChanged();
            return true;
        }

        return false;
    }

    private static ItemStack addItem(Container container, ItemStack stack, Direction direction) {
        if (container instanceof WorldlyContainer worldlyContainer) {
            int[] slots = worldlyContainer.getSlotsForFace(direction);
            return addItem(worldlyContainer, stack, slots);
        } else {
            return addItem(container, stack);
        }
    }

    private static ItemStack addItem(Container container, ItemStack stack, int[] slots) {
        for (int slot : slots) {
            stack = tryInsertInSlot(container, stack, slot);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    private static ItemStack addItem(Container container, ItemStack stack) {
        int containerSize = container.getContainerSize();
        for (int i = 0; i < containerSize && !stack.isEmpty(); ++i) {
            stack = tryInsertInSlot(container, stack, i);
        }
        return stack;
    }

    private static ItemStack tryInsertInSlot(Container container, ItemStack stackToInsert, int slot) {
        ItemStack slotStack = container.getItem(slot);
        if (canPlaceItemInContainer(container, stackToInsert, slot)) {
            if (slotStack.isEmpty()) {
                container.setItem(slot, stackToInsert.copy());
                stackToInsert.setCount(0);
            } else if (canMergeItems(slotStack, stackToInsert)) {
                int maxStackSize = Math.min(stackToInsert.getMaxStackSize(), slotStack.getMaxStackSize());
                int canAdd = Math.min(stackToInsert.getCount(), maxStackSize - slotStack.getCount());
                if (canAdd > 0) {
                    slotStack.grow(canAdd);
                    stackToInsert.shrink(canAdd);
                    container.setItem(slot, slotStack);
                }
            }
        }
        return stackToInsert;
    }

    private static boolean canPlaceItemInContainer(Container container, ItemStack stack, int slot) {
        if (container instanceof WorldlyContainer worldlyContainer) {
            return worldlyContainer.canPlaceItemThroughFace(slot, stack, Direction.UP);
        }
        return container.canPlaceItem(slot, stack);
    }

    private static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    /**
     * Emits a redstone signal for one tick based on which direction an item was transferred from.
     * The signal strength corresponds to the bit position of the source direction.
     *
     * @param sourceDirection The direction the item came from
     * @param directionIndex  The index of the direction in the DIRECTIONS array
     */
    private void emitRedstoneSignalForTransfer(Direction sourceDirection, int directionIndex) {
        if (level == null) return;

        // Calculate signal strength based on direction
        int signalStrength;
        if (sourceDirection == Direction.DOWN) {
            // For DOWN direction, use bit 0 (value 1)
            signalStrength = 15; // Full signal strength for DOWN
        } else {
            // For horizontal directions, use the bit corresponding to their position
            // NORTH = bit 0 (1), EAST = bit 1 (2), SOUTH = bit 2 (4), WEST = bit 3 (8)
            signalStrength = 1 << (directionIndex - 1);
        }

        redstoneOutputSignal = signalStrength;
        redstoneOutputTicks = 1; // Emit for one tick

        // Update neighboring blocks to notify them of the signal change
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    /**
     * Gets the current redstone signal strength being emitted by this block.
     * This is used by the block to provide redstone output.
     *
     * @return The redstone signal strength (0-15)
     */
    public int getRedstoneSignal() {
        return redstoneOutputSignal;
    }

    /**
     * Gets the redstone signal strength for a specific direction.
     * Used by the block to provide directional redstone output.
     *
     * @param direction The direction to get the signal for
     * @return The redstone signal strength for that direction (0-15)
     */
    public int getRedstoneSignal(Direction direction) {
        return redstoneOutputSignal;
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.wunderreich.suction_tube");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SuctionTubeMenu(containerId, playerInventory, worldPosition);
    }

    /**
     * Opens the configuration menu for the given player.
     */
    public void openMenu(ServerPlayer player) {
        player.openMenu(this);
    }

    /**
     * Gets the filter items for a specific direction.
     */
    public ItemStack[] getFilterItems(Direction direction) {
        ItemStack[] filters = filterItems.get(direction);
        if (filters == null) {
            filters = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                filters[i] = ItemStack.EMPTY;
            }
            filterItems.put(direction, filters);
        }
        return filters;
    }

    /**
     * Sets the filter items for a specific direction from a container.
     */
    public void setFilterItems(Direction direction, Container container) {
        ItemStack[] filters = filterItems.computeIfAbsent(direction, k -> new ItemStack[4]);

        for (int i = 0; i < Math.min(4, container.getContainerSize()); i++) {
            filters[i] = container.getItem(i).copy();
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ItemStack[] filters;
        String listKey;
        ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList;
        // Save filter items using list-based approach similar to Hopper
        for (Direction direction : DIRECTIONS) {
            filters = filterItems.get(direction);
            if (filters != null) {
                listKey = direction.getName();
                typedOutputList = valueOutput.list(
                        listKey,
                        ItemStackWithSlot.CODEC
                );

                for (int i = 0; i < filters.length; i++) {
                    if (!filters[i].isEmpty()) {
                        typedOutputList.add(new ItemStackWithSlot(i, filters[i]));
                    }
                }

                // If no filters for this direction, discard the list
                if (typedOutputList.isEmpty()) {
                    valueOutput.discard(listKey);
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ItemStack[] filters;
        String listKey;
        // Load filter items using list-based approach similar to Hopper
        for (Direction direction : DIRECTIONS) {
            filters = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                filters[i] = ItemStack.EMPTY;
            }

            listKey = direction.getName();
            for (ItemStackWithSlot itemStackWithSlot : valueInput.listOrEmpty(listKey, ItemStackWithSlot.CODEC)) {
                if (itemStackWithSlot.slot() >= 0 && itemStackWithSlot.slot() < 4) {
                    filters[itemStackWithSlot.slot()] = itemStackWithSlot.stack();
                }
            }

            filterItems.put(direction, filters);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // If we cleared a signal during loading, notify neighbors now that the world is ready
        if (level != null && !level.isClientSide) {
            //level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
}