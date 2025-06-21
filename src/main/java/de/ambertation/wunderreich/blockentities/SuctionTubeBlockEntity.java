package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.blocks.SuctionTube;
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
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Random;
import org.jetbrains.annotations.Nullable;


class SuctionInput {
    public final Direction inDirection;
    public final ItemStack[] filter;
    private Container container;
    private ComparatorBlockEntity comparator;
    private boolean isInputComparator;
    private byte inputStrength;
    private final byte redstoneBit;
    byte redstoneBitOverride = -1; // -1 means no override, 0-3 are valid bits

    SuctionInput(Direction inDirection, byte redstoneBit) {
        this.inDirection = inDirection;
        this.redstoneBit = redstoneBit;
        this.filter = createEmptyFilter();
    }

    public static ItemStack[] createEmptyFilter() {
        return new ItemStack[]{
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY
        };
    }

    public boolean hasContainer() {
        return container != null;
    }

    public Container container() {
        return container;
    }

    public ComparatorBlockEntity outputComperator() {
        return isInputComparator ? null : comparator;
    }

    public ComparatorBlockEntity inputComperator() {
        return isInputComparator ? comparator : null;
    }

    public byte redstoneBit() {
        return (byte) Math.max(this.redstoneBit, this.redstoneBitOverride);
    }

    public byte currentInputStrength() {
        return inputStrength;
    }

    /**
     * Checks if the given item stack passes the filter for the specified direction.
     * If no filter items are set for a direction, all items pass.
     * If filter items are set, only items matching the filter pass.
     */
    public boolean passesFilter(ItemStack itemStack) {
        // If the item stack is empty, it cannot pass any filter
        if (itemStack.isEmpty()) return false;

        boolean hasNoFilterItems = true;

        // Check if any filter slot matches the input
        for (ItemStack filter : this.filter) {
            if (!filter.isEmpty()) {
                hasNoFilterItems = false;
                if (ItemStack.isSameItemSameComponents(itemStack, filter)) {
                    return true; // Item matches one of the filters
                }
            }
        }

        // If no filters are set, allow all items
        return hasNoFilterItems;
    }

    public boolean tryTransferItemToDestination(Container destination) {
        if (this.container instanceof WorldlyContainer worldlySource) {
            final int[] slots = worldlySource.getSlotsForFace(this.inDirection.getOpposite());
            for (int slot : slots) {
                if (tryTakeAndTransfer(slot, destination)) {
                    return true;
                }
            }
        } else {
            final int containerSize = this.container.getContainerSize();
            for (int slot = 0; slot < containerSize; ++slot) {
                if (tryTakeAndTransfer(slot, destination)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryTakeAndTransfer(
            final int sourceSlotIndex, final Container destination
    ) {
        final ItemStack sourceStack = this.container.getItem(sourceSlotIndex);
        if (!passesFilter(sourceStack)) {
            return false;
        }

        // Try to insert into destination
        if (tryMoveOne(destination, sourceStack)) {
            this.container.setChanged();
            destination.setChanged();
            return true;
        }

        return false;
    }

    private boolean tryMoveOne(Container destination, ItemStack stackToMoveOneFrom) {
        if (destination instanceof WorldlyContainer worldlyContainer) {
            int[] slots = worldlyContainer.getSlotsForFace(Direction.DOWN);
            for (int slot : slots) {
                worldlyContainer.canPlaceItemThroughFace(slot, stackToMoveOneFrom.copyWithCount(1), Direction.DOWN);
                if (tryMoveOneToSlot(destination, stackToMoveOneFrom, slot)) {
                    return true;
                }
            }
        } else {
            int containerSize = destination.getContainerSize();
            for (int i = 0; i < containerSize && !stackToMoveOneFrom.isEmpty(); ++i) {
                if (tryMoveOneToSlot(destination, stackToMoveOneFrom, i)) {
                    return true;
                }
                ;
            }
        }
        return false;
    }

    private static boolean tryMoveOneToSlot(Container destination, ItemStack stackToMoveOneFrom, int slot) {
        final ItemStack slotStack = destination.getItem(slot);
        if (canPlaceItemInContainer(destination, stackToMoveOneFrom, slot)) {
            if (slotStack.isEmpty()) {
                //create a new stack with a single item from the source stack
                destination.setItem(slot, stackToMoveOneFrom.copyWithCount(1));

                // make sure to remove it from the source stack
                stackToMoveOneFrom.shrink(1);

                return true;
            } else if (canMergeItems(slotStack, stackToMoveOneFrom)) {
                int maxStackSize = Math.min(stackToMoveOneFrom.getMaxStackSize(), slotStack.getMaxStackSize());
                int canAdd = Math.min(stackToMoveOneFrom.getCount(), maxStackSize - slotStack.getCount());
                if (canAdd > 0) {
                    // grow the stack in the destination slot
                    slotStack.grow(1);

                    // shrink the source stack
                    stackToMoveOneFrom.shrink(1);

                    //destination.setItem(slot, slotStack);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean canPlaceItemInContainer(Container destination, ItemStack stackToInsert, int slot) {
        if (destination.canPlaceItem(slot, stackToInsert)) {
            if (destination instanceof WorldlyContainer worldlyContainer) {
                return worldlyContainer.canPlaceItemThroughFace(slot, stackToInsert, Direction.UP);
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    void neighborChanged(Level level, BlockPos blockPos) {
        this.comparator = null;
        final BlockPos myPos = blockPos.relative(inDirection);
        BlockEntity blockEntity = level.getBlockEntity(myPos);

        if (blockEntity instanceof Container) {
            this.container = SuctionTubeBlockEntity.getContainerAt(level, myPos);
        }

        if (blockEntity instanceof ComparatorBlockEntity comparatorBlockEntity) {
            // Get the facing direction of the comparator
            Direction comparatorFacing = comparatorBlockEntity.getBlockState().getValue(ComparatorBlock.FACING);

            // This comparator is inputting a signal, so change the inputValue
            if (comparatorFacing == inDirection) {
                this.inputStrength = (byte) Math.max(
                        Byte.MIN_VALUE,
                        Math.min(Byte.MAX_VALUE, level.getSignal(myPos, inDirection))
                );
                this.isInputComparator = true;
                this.comparator = comparatorBlockEntity;
            } else if (comparatorFacing == inDirection.getOpposite()) {
                this.isInputComparator = false;
                this.comparator = comparatorBlockEntity;
            } else {
                this.comparator = null;
            }
        }
    }
}

class SuctionInputs {
    // The First one has to be DOWN, otherwise the BitMask will not work correctly
    static final Direction[] DIRECTIONS = {
            Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    static final byte DOWN_DIRECTION_IDEX = 0;

    // Randomized order of container indices to try transferring from
    private final byte[] randomizedIndexOrder = {0, 1, 2, 3, 4};
    private final Random random = new Random();

    // Redstone disable mask: bit 0 = NORTH, bit 1 = EAST, bit 2 = SOUTH, bit 3 = WEST
    private byte redstoneDisableMask;
    // Redstone output signal strength
    private byte redstoneOutputSignal;
    // Number of ticks the redstone output signal is active
    private byte redstoneOutputTicks;


    private final SuctionInput[] inputs;

    SuctionInputs() {
        assert (SuctionTubeBlockEntity.DIRECTIONS.length == 5) : "SuctionTubeBlockEntity.DIRECTIONS must have exactly 5 directions";
        assert (SuctionTubeBlockEntity.DIRECTIONS[DOWN_DIRECTION_IDEX] == Direction.DOWN) : "Direction at index " + DOWN_DIRECTION_IDEX + " in SuctionTubeBlockEntity.DIRECTIONS must be DOWN";

        inputs = new SuctionInput[SuctionTubeBlockEntity.DIRECTIONS.length];
        for (int dirIndex = 0; dirIndex < SuctionTubeBlockEntity.DIRECTIONS.length; dirIndex++) {
            inputs[dirIndex] = new SuctionInput(SuctionTubeBlockEntity.DIRECTIONS[dirIndex], (byte) (dirIndex - 1));
        }

        this.shuffleSourceContainerOrder();
    }

    public SuctionInput forDirection(Direction direction) {
        for (SuctionInput input : inputs) {
            if (input.inDirection == direction) {
                return input;
            }
        }
        return null; // No input found for this direction
    }

    private void shuffleSourceContainerOrder() {
        for (int cIndx = 0; cIndx < randomizedIndexOrder.length; cIndx++) {
            byte randomIndex = (byte) random.nextInt(randomizedIndexOrder.length);
            byte temp = randomizedIndexOrder[cIndx];
            randomizedIndexOrder[cIndx] = randomizedIndexOrder[randomIndex];
            randomizedIndexOrder[randomIndex] = temp;
        }
    }

    void loadAdditional(ValueInput valueInput) {
        String listKey;
        ValueInput.TypedInputList<ItemStackWithSlot> itemList;

        for (SuctionInput input : inputs) {
            listKey = input.inDirection.getName();
            itemList = valueInput.listOrEmpty(listKey, ItemStackWithSlot.CODEC);
            for (int i = 0; i < 4; i++) input.filter[i] = ItemStack.EMPTY;

            for (ItemStackWithSlot itemStackWithSlot : itemList) {
                if (itemStackWithSlot.slot() >= 0 && itemStackWithSlot.slot() < input.filter.length) {
                    input.filter[itemStackWithSlot.slot()] = itemStackWithSlot.stack();
                }
            }
        }
    }

    void saveAdditional(ValueOutput valueOutput) {
        String listKey;
        ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList;

        for (SuctionInput input : inputs) {
            if (input.filter.length == 0) continue;
            listKey = input.inDirection.getName();

            typedOutputList = valueOutput.list(listKey, ItemStackWithSlot.CODEC);
            for (int i = 0; i < input.filter.length; i++) {
                // Only add non-empty filters to the output list
                if (!input.filter[i].isEmpty()) {
                    typedOutputList.add(new ItemStackWithSlot(i, input.filter[i]));
                }
            }

            // If no filters for this direction, discard the list
            if (typedOutputList.isEmpty()) {
                valueOutput.discard(listKey);
            }
        }
    }

    public int redstoneOutputSignal() {
        return redstoneOutputSignal;
    }

    void tickRedstoneOutput(Level level, BlockPos worldPosition, SuctionTube suctionBlock) {
        if (redstoneOutputTicks > 0) {
            redstoneOutputTicks--;
            if (redstoneOutputTicks <= 0) {
                redstoneOutputSignal = 0;
                level.updateNeighborsAt(worldPosition, suctionBlock);
            }
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
     */
    private void updateRedstoneMask() {
        byte combinedMask = 0;
        for (SuctionInput input : inputs) {
            if (input.inputComperator() != null) {
                // If this input has a comparator facing into the block, use its signal strength
                combinedMask |= input.currentInputStrength();
            }
        }
        this.redstoneDisableMask = combinedMask;
    }

    public void neighborChanged(Level level, BlockPos pos) {
        SuctionInput downInput = null;
        int newDownOverride = -1; // -1 means no override
        for (var input : this.inputs) {
            input.neighborChanged(level, pos);
            // Remember which input is the DOWN one
            if (input.inDirection == Direction.DOWN) {
                downInput = input;
            } else if (input.inputComperator() != null) {
                newDownOverride = Math.max(newDownOverride, input.redstoneBit());
            }
        }
        if (downInput != null) {
            // If we have a DOWN input, set its override to the maximum of all other inputs
            downInput.redstoneBitOverride = (byte) newDownOverride;
        }

        this.updateRedstoneMask();
    }

    private boolean isDirectionDisabledByRedstone(SuctionInput input) {
        if (input.redstoneBit() < 0) return true;
        return (this.redstoneDisableMask & (1 << input.redstoneBit())) != 0;
    }

    /**
     * Emits a redstone signal for one tick based on which direction an item was transferred from.
     * The signal strength corresponds to the bit position of the source direction.
     */
    private void emitRedstoneSignalForTransfer(
            Level level,
            BlockPos worldPosition,
            SuctionTube suctionBlock,
            SuctionInput outputSource
    ) {
        if (level == null) return;

        // Calculate signal strength based on direction
        int signalStrength;
        if (outputSource.inDirection == Direction.DOWN) {
            // For DOWN direction, has no bit, so we use a fixed value of 5
            signalStrength = DIRECTIONS.length;
        } else {
            // For horizontal directions, use the bit corresponding to their position
            // NORTH = bit 0 (1), EAST = bit 1 (2), SOUTH = bit 2 (3), WEST = bit 3 (4)
            signalStrength = outputSource.redstoneBit();
        }

        redstoneOutputSignal = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, signalStrength));
        redstoneOutputTicks = 1; // Emit for one tick

        // Update neighboring blocks to notify them of the signal change
        level.updateNeighborsAt(worldPosition, suctionBlock);
    }

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
    public boolean tryTransferItem(Level level, BlockPos worldPosition, SuctionTube suctionBlock) {
        if (level == null || level.isClientSide) return false;
        // Get container above (destination)
        Container destContainer = SuctionTubeBlockEntity.getContainerAt(level, worldPosition.above());
        if (destContainer == null) return false;


        //randomly pick one available container to transfer from without adding a new datastructure
        for (int i : randomizedIndexOrder) {
            final SuctionInput input = inputs[i];
            // Check if this direction is disabled by redstone
            if (isDirectionDisabledByRedstone(input)) {
                continue; // Skip this direction
            }

            if (!input.hasContainer()) {
                // If the container is null, skip this input
                continue;
            }

            if (input.tryTransferItemToDestination(destContainer)) {
                // Emit redstone signal based on the direction the item came from
                emitRedstoneSignalForTransfer(level, worldPosition, suctionBlock, input);
                shuffleSourceContainerOrder();
                return true; // Successfully transferred an item
            }
        }
        return false;
    }
}


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

    private static final int TRANSFER_COOLDOWN = 8; // Same as hopper
    // Directions for the containers relative to the suction tube
    public static final Direction[] DIRECTIONS = SuctionInputs.DIRECTIONS;

    private int transferCooldown = 0;
    private final SuctionInputs inputs;
    private boolean didInitialize = false;

    public SuctionTubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE, blockPos, blockState);
    }

    public SuctionTubeBlockEntity(
            BlockEntityType<?> blockEntityType,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(blockEntityType, blockPos, blockState);

        // Initialize suction inputs for each direction
        inputs = new SuctionInputs();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SuctionTubeBlockEntity blockEntity) {
        if (level.isClientSide) return;
        if (!blockEntity.didInitialize) {
            blockEntity.didInitialize = true;
            blockEntity.inputs.neighborChanged(level, pos);
        }

        // Handle transfer cooldown
        --blockEntity.transferCooldown;
        if (blockEntity.transferCooldown <= 0) {
            blockEntity.transferCooldown = TRANSFER_COOLDOWN;
            blockEntity.inputs.tryTransferItem(level, pos, (SuctionTube) state.getBlock());
        }

        // Handle redstone output timing
        blockEntity.inputs.tickRedstoneOutput(level, pos, (SuctionTube) state.getBlock());
    }

    // Update the Neighboring State (redstone signals, and attached containers
    public void neighborChanged(Level level, BlockPos pos) {
        inputs.neighborChanged(level, pos);
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            return container;
        }
        return HopperBlockEntity.getContainerAt(level, pos);
    }

    /**
     * Gets the redstone signal strength for a specific direction.
     * Used by the block to provide directional redstone output.
     *
     * @param direction The direction to get the signal for
     * @return The redstone signal strength for that direction (0-15)
     */
    public int getRedstoneSignal(Direction direction) {
        return this.inputs.redstoneOutputSignal();
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
        SuctionInput input = inputs.forDirection(direction);
        if (input == null) return SuctionInput.createEmptyFilter();
        return input.filter;
    }

    /**
     * Sets the filter items for a specific direction from a container.
     */
    public void setFilterItems(Direction direction, Container container) {
        SuctionInput input = inputs.forDirection(direction);
        if (input == null) return;

        for (int i = 0; i < Math.min(input.filter.length, container.getContainerSize()); i++) {
            input.filter[i] = container.getItem(i).copy();
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        // Save currently set filters
        this.inputs.saveAdditional(valueOutput);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ItemStack[] filters;
        String listKey;

        // Load the filter items from the ValueInput
        this.inputs.loadAdditional(valueInput);
    }
}

