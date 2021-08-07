package de.ambertation.wunderreich.blockentities;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class BoxOfEirBlockEntity extends BlockEntity implements LidBlockEntity {
	//EnderChestBlockEntity
	private final ChestLidController chestLidController = new ChestLidController();
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {
			//System.out.println("Open " + blockPos.getZ());
			level.playSound((Player)null, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
		}
		
		protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {
			//System.out.println("Close " + blockPos.getZ());
			level.playSound((Player)null, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
		}
		
		protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
			level.blockEvent(BoxOfEirBlockEntity.this.worldPosition, Wunderreich.BOX_OF_EIR, 1, j);
			BoxOfEirBlock.updateAllBoxes(level.getServer(), true, false);
		}
		
		protected boolean isOwnContainer(Player player) {
			return ((ActiveChestStorage)player).isActiveBoxOfEir(BoxOfEirBlockEntity.this);			
		}
	};
	
	public BoxOfEirBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(Wunderreich.BLOCK_ENTITY_BOX_OF_EIR, blockPos, blockState);
	}
	
	public static void lidAnimateTick(Level level, BlockPos blockPos, BlockState blockState, BoxOfEirBlockEntity boxOfEirBlockEntity) {
		boxOfEirBlockEntity.chestLidController.tickLid();
	}
	
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
			return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}
	
	public void recheckOpen() {
		if (!this.remove) {
			this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}
	
	public boolean isOpen(){
		return this.openersCounter.getOpenerCount() > 0;
	}
	
	public float getOpenNess(float f) {
		return this.chestLidController.getOpenness(f);
	}
}
