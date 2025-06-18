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

import org.jetbrains.annotations.Nullable;

public class SuctionTubeBlockEntity extends BlockEntity {
    private int transferCooldown = 0;
    private static final int TRANSFER_COOLDOWN = 8; // Same as hopper

    public SuctionTubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE, blockPos, blockState);
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

    private void tryTransferItem() {
        if (level == null || level.isClientSide) return;

        // Get container below (source)
        BlockPos belowPos = worldPosition.below();
        Container sourceContainer = getContainerAt(level, belowPos);
        
        // Get container above (destination)
        BlockPos abovePos = worldPosition.above();
        Container destContainer = getContainerAt(level, abovePos);

        if (sourceContainer != null && destContainer != null) {
            transferItemFromTo(sourceContainer, destContainer, Direction.UP);
        }
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

    private static boolean tryTakeAndTransfer(Container source, Container destination, int sourceSlot, Direction direction) {
        ItemStack sourceStack = source.getItem(sourceSlot);
        if (sourceStack.isEmpty()) {
            return false;
        }

        // Check if we can extract from source
        if (source instanceof WorldlyContainer worldlySource && !worldlySource.canTakeItemThroughFace(sourceSlot, sourceStack, direction.getOpposite())) {
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