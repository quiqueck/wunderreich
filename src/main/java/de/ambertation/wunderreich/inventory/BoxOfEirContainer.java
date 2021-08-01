package de.ambertation.wunderreich.inventory;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BoxOfEirContainer extends SimpleContainer {
	private static BoxOfEirContainer INSTANCE;
	public static BoxOfEirContainer getInstance(){
		if (INSTANCE == null){
			INSTANCE = new BoxOfEirContainer();
		}
		return INSTANCE;
	}
	
	@Nullable
	private BoxOfEirBlockEntity activeChest;
	
	public BoxOfEirContainer() {
		super(27);
	}
	
	public void setActiveChest(BoxOfEirBlockEntity boxOfEirBlockEntity) {
		this.activeChest = boxOfEirBlockEntity;
	}
	
	public boolean isActiveChest(BoxOfEirBlockEntity boxOfEirBlockEntity) {
		return this.activeChest == boxOfEirBlockEntity;
	}
	
	public void fromTag(ListTag listTag) {
		int j;
		for(j = 0; j < this.getContainerSize(); ++j) {
			this.setItem(j, ItemStack.EMPTY);
		}
		
		for(j = 0; j < listTag.size(); ++j) {
			CompoundTag compoundTag = listTag.getCompound(j);
			int k = compoundTag.getByte("Slot") & 255;
			if (k >= 0 && k < this.getContainerSize()) {
				this.setItem(k, ItemStack.of(compoundTag));
			}
		}
		
	}
	
	public ListTag createTag() {
		ListTag listTag = new ListTag();
		
		for(int i = 0; i < this.getContainerSize(); ++i) {
			ItemStack itemStack = this.getItem(i);
			if (!itemStack.isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)i);
				itemStack.save(compoundTag);
				listTag.add(compoundTag);
			}
		}
		
		return listTag;
	}
	
	public boolean stillValid(Player player) {
		return this.activeChest != null && !this.activeChest.stillValid(player) ? false : super.stillValid(player);
	}
	
	public void startOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.startOpen(player);
		}
		
		super.startOpen(player);
	}
	
	public void stopOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.stopOpen(player);
		}
		
		super.stopOpen(player);
		this.activeChest = null;
	}
}