package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class WunderKisteBlockEntity extends BlockEntity implements LidBlockEntity, Nameable {
    private final ChestLidController chestLidController = new ChestLidController();
    private Component networkName;

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("NetworkName", 8)) {
            this.networkName = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (this.networkName != null) {
            compoundTag.putString("NetworkName", Component.Serializer.toJson(this.networkName));
        }
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
                    level.random.nextFloat() * 0.1F + 0.9F
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
                    level.random.nextFloat() * 0.1F + 0.9F
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
            WunderKisteBlock.updateAllBoxes(blockState, level.getServer(), true, false);
        }

        protected boolean isOwnContainer(@NotNull Player player) {
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
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
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


    public void setNetworkName(Component component) {
        this.networkName = component;
    }

    public String getNetworkName() {
        if (this.networkName == null) return null;
        return this.networkName.getString();
    }

    protected Component getDefaultName() {
        return Component.translatable("container.wunderreich.wunder_kiste");
    }

    public void setCustomName(Component component) {
        setNetworkName(component);
    }

    @Override
    public Component getName() {
        if (this.networkName != null) {
            return this.networkName;
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
        return this.networkName;
    }
}
