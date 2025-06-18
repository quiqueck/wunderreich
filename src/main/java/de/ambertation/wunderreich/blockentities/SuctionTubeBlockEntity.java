package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.registries.WunderreichBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Suction Tube block that transfers items from surrounding containers
 * to a container above it, with redstone signal control for selective direction disabling.
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
 * <h3>Redstone Signal Control:</h3>
 * The Suction Tube accepts redstone signals from horizontal directions to selectively disable
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
 *
 * <h3>Usage Examples:</h3>
 * <ul>
 *   <li><strong>Disable EAST side:</strong> Place a comparator on the EAST side outputting signal strength 2
 *       (binary: 0010, bit 1 set) - this disables item transfer from the EAST direction</li>
 *   <li><strong>Disable NORTH and SOUTH:</strong> Place a comparator on any side outputting signal strength 5
 *       (binary: 0101, bits 0 and 2 set) - this disables both NORTH and SOUTH directions</li>
 *   <li><strong>Disable bottom face:</strong> The bottom face (DOWN) is controlled by whichever horizontal
 *       direction provides the redstone signal. For example, if EAST provides signal strength 1
 *       (binary: 0001, bit 0 set), it disables the bottom face because bit 0 (NORTH) is set</li>
 *   <li><strong>No signal:</strong> When no redstone signal is present, all directions are enabled
 *       (backward compatible behavior)</li>
 * </ul>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li>Transfer cooldown: 8 ticks (same as vanilla hopper)</li>
 *   <li>Transfers one item at a time</li>
 *   <li>Randomizes source container checking order to prevent bias</li>
 *   <li>Respects WorldlyContainer face restrictions</li>
 * </ul>
 */
public class SuctionTubeBlockEntity extends BlockEntity {
    private int transferCooldown = 0;
    private static final int TRANSFER_COOLDOWN = 8; // Same as hopper
    private final Random random = new Random();

    public SuctionTubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE, blockPos, blockState);

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

        --blockEntity.transferCooldown;
        if (blockEntity.transferCooldown <= 0) {
            blockEntity.transferCooldown = TRANSFER_COOLDOWN;
            blockEntity.tryTransferItem();
        }
    }

    // Directions for the containers relative to the suction tube
    private static final Direction[] DIRECTIONS = {
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
            if (c != null && transferItemFromTo(c, destContainer, DIRECTIONS[i])) {
                shuffleSourceContainerOrder();
                return; // Successfully transferred an item
            }
        }
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
    private static Container getContainerAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            return container;
        }
        return HopperBlockEntity.getContainerAt(level, pos);
    }

    private static boolean transferItemFromTo(Container source, Container destination, Direction direction) {
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
}