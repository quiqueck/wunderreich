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
     * Gets the redstone disable mask by checking redstone signals from all horizontal directions.
     * The signal strength from each direction is treated as a bitmask.
     *
     * @return Combined redstone disable mask
     */
    private int getRedstoneDisableMask() {
        if (level == null) return 0;

        int combinedMask = 0;

        // Check redstone signal from each horizontal direction
        for (int i = 1; i < DIRECTIONS.length; i++) { // Start from 1 to skip DOWN
            Direction direction = DIRECTIONS[i];
            BlockPos signalPos = worldPosition.relative(direction);
            int signalStrength = level.getSignal(signalPos, direction);

            // Use the signal strength as a bitmask
            combinedMask |= signalStrength;
        }

        return combinedMask;
    }

    /**
     * Checks if a specific direction is disabled by the redstone signal.
     *
     * @param direction           The direction to check
     * @param redstoneDisableMask The redstone disable mask
     * @return true if the direction is disabled
     */
    private boolean isDirectionDisabledByRedstone(Direction direction, int redstoneDisableMask) {
        // DOWN (bottom) is controlled by horizontal redstone inputs
        if (direction == Direction.DOWN) {
            // Find which redstone input controls the bottom face
            // This is determined by which direction has a redstone signal
            for (int bitIndex = 1; bitIndex < DIRECTIONS.length; bitIndex++) {
                Direction redstoneDirection = DIRECTIONS[bitIndex];
                BlockPos signalPos = worldPosition.relative(redstoneDirection);
                int signalStrength = level.getSignal(signalPos, redstoneDirection);

                if (signalStrength > 0) {
                    // Check if the bit corresponding to this redstone input direction is set
                    return (redstoneDisableMask & (1 << (bitIndex - 1))) != 0;
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

    // Helper class to store container and its relative direction
    private static class ContainerInfo {
        final Container container;
        final Direction direction;

        ContainerInfo(Container container, Direction direction) {
            this.container = container;
            this.direction = direction;
        }
    }
}