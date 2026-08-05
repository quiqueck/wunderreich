package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class WunderKisteBlockEntity extends BlockEntity implements LidBlockEntity, Nameable, Container {
    private final ChestLidController chestLidController = new ChestLidController();
    private Component domainName;

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        valueInput.read("CustomName", ComponentSerialization.CODEC).ifPresent(name -> this.domainName = name);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.domainName);
    }

    public WunderKisteBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WunderreichBlockEntities.BLOCK_ENTITY_WUNDER_KISTE, blockPos, blockState);
    }

    public static void lidAnimateTick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            WunderKisteBlockEntity wunderKisteBlockEntity
    ) {
        wunderKisteBlockEntity.chestLidController.tickLid();
    }

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos blockPos, @NotNull BlockState blockState) {
            //System.out.println("Open " + blockPos.getZ());
            level.playSound(
                    null,
                    (double) blockPos.getX() + 0.5D,
                    (double) blockPos.getY() + 0.5D,
                    (double) blockPos.getZ() + 0.5D,
                    SoundEvents.ENDER_CHEST_OPEN,
                    SoundSource.BLOCKS,
                    0.5F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        protected void onClose(Level level, BlockPos blockPos, @NotNull BlockState blockState) {
            //System.out.println("Close " + blockPos.getZ());
            level.playSound(
                    null,
                    (double) blockPos.getX() + 0.5D,
                    (double) blockPos.getY() + 0.5D,
                    (double) blockPos.getZ() + 0.5D,
                    SoundEvents.ENDER_CHEST_CLOSE,
                    SoundSource.BLOCKS,
                    0.5F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        protected void openerCountChanged(
                Level level,
                @NotNull BlockPos blockPos,
                @NotNull BlockState blockState,
                int i,
                int j
        ) {
            assert WunderreichBlocks.WUNDER_KISTE != null;
            level.blockEvent(WunderKisteBlockEntity.this.worldPosition, WunderreichBlocks.WUNDER_KISTE, 1, j);
            WunderKisteBlock.updateAllBoxes(blockState, level.getBlockEntity(blockPos), level.getServer(), true, false);
        }

        public boolean isOwnContainer(@NotNull Player player) {
            return ((ActiveChestStorage) player).isActiveWunderKiste(WunderKisteBlockEntity.this);
        }
    };

    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.chestLidController.shouldBeOpen(j > 0);
            return true;
        } else {
            return super.triggerEvent(i, j);
        }
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(
                    player,
                    this.getLevel(),
                    this.getBlockPos(),
                    this.getBlockState(),
                    player.getContainerInteractionRange()
            );
        }

    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr(
                    (double) this.worldPosition.getX() + 0.5D,
                    (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D
            ) > 64.0D);
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public boolean isOpen() {
        return this.openersCounter.getOpenerCount() > 0;
    }

    public float getOpenNess(float f) {
        return this.chestLidController.getOpenness(f);
    }


    public void setDomainName(Component component) {
        this.domainName = component;
    }

    public WunderKisteDomain.ID getDomainName() {
        if (this.domainName == null) return null;
        return WunderKisteDomain.ID.forString(domainName.getString());
    }

    protected Component getDefaultName() {
        return Component.translatable("container.wunderreich.wunder_kiste");
    }

    public void setCustomName(Component component) {
        setDomainName(component);
    }

    @Override
    public Component getName() {
        if (this.domainName != null) {
            return this.domainName;
        }
        return this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.domainName;
    }

    @Override
    public boolean hasCustomName() {
        return this.getCustomName() != null && this.getDomainName() != null && !this.getDomainName().isEmpty();
    }


    /**
     * The default {@link BlockEntity#preRemoveSideEffects} drops the contents of every block entity
     * that implements {@link Container}. Our {@link Container} implementation is only a view onto the
     * shared {@link WunderKisteContainer} of the domain/network, so the default behaviour would drop
     * (and, since {@code Containers.dropItemStack} splits the stacks in place, permanently empty) the
     * inventory that is shared by every Wunderkiste of that network.
     * <p>
     * The network inventory must survive breaking a box - it exists even when no box of that network
     * is placed in the world at all. So we deliberately do nothing here (same as vanilla's
     * ShulkerBoxBlockEntity, which keeps its contents in the dropped item instead).
     */
    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
    }

    @Override
    public int getContainerSize() {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        return container != null ? container.getContainerSize() : 0;
    }

    @Override
    public boolean isEmpty() {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        return container == null || container.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        return container != null ? container.getItem(i) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int i, int j) {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        return container != null ? container.removeItem(i, j) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        return container != null ? container.removeItemNoUpdate(i) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        if (container != null) {
            container.setItem(i, itemStack);
        }
    }

    @Override
    public void clearContent() {
        final WunderKisteContainer container = WunderKisteBlock.getContainer(this.getBlockState(), this, this.level);
        if (container != null) {
            container.clearContent();
        }
    }
}
