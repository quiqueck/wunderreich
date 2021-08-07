package de.ambertation.wunderreich.inventory;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class BoxOfEirContainer extends SimpleContainer implements WorldlyContainer {
	//TODO: this will be replaced with BCLibs WorldDataAPI once a stable release (of BCLib) is reached
	public static class LevelData {
		private static boolean wrapCall(LevelStorageSource levelSource, String levelID, Function<LevelStorageAccess, Boolean> runWithLevel) {
			LevelStorageSource.LevelStorageAccess levelStorageAccess;
			try {
				levelStorageAccess = levelSource.createAccess(levelID);
			} catch (IOException e) {
				System.err.println("Unable to load level " + levelID);
				SystemToast.onWorldAccessFailure(Minecraft.getInstance(), levelID);
				Minecraft.getInstance().setScreen((Screen)null);
				return true;
			}
			
			boolean returnValue = runWithLevel.apply(levelStorageAccess);
			
			try {
				levelStorageAccess.close();
			} catch (IOException e) {
				System.err.println("Failed to get Lock on level " + levelID);
			}
			
			return returnValue;
		}
		
		public static void init(LevelStorageSource levelSource, String levelID) {
			wrapCall(levelSource, levelID, (levelStorageAccess) -> {
				init(levelStorageAccess);
				return true;
			});
		}
		
		public static void init(LevelStorageSource.LevelStorageAccess session){
			init(new File(session.getLevelPath(LevelResource.ROOT).toFile(), "data/" + Wunderreich.MOD_ID + ".nbt"));
		}
		
		private static CompoundTag root;
		private static File dataFile;
		
		protected static void init(File dataFile){
			LevelData.dataFile = dataFile;
			if (dataFile.exists()) {
				try {
					root = NbtIo.readCompressed(dataFile);
				}
				catch (IOException e) {
					System.err.println("Failed to load inventory for Boxes of Eir:" + e);
				}
			} else {
				root = new CompoundTag();
				root.putString("version", Wunderreich.VERSION);
				save();
			}
		}
		
		public static void save(){
			try {
				if (!dataFile.getParentFile().exists()){
					dataFile.getParentFile().mkdirs();
				}
				NbtIo.writeCompressed(root, dataFile);
			}
			catch (IOException e) {
				System.err.println("Failed to save inventory for Boxes of Eir:" + e);
			}
		}
		
		public static CompoundTag getCompoundTag(String path) {
			String[] parts = path.split("\\.");
			CompoundTag tag = root;
			for (String part : parts) {
				if (tag.contains(part)) {
					tag = tag.getCompound(part);
				}
				else {
					CompoundTag t = new CompoundTag();
					tag.put(part, t);
					tag = t;
				}
			}
			return tag;
		}
	}
	
	public BoxOfEirContainer() {
		super(slots.length);
	}

	public void load(){
		CompoundTag global = LevelData.getCompoundTag("global");
		ListTag items;
		if (!global.contains("items")){
			items = new ListTag();
			global.put("items", items);
		} else {
			items = global.getList("items", 10);
		}
		fromTag(items);
	}
	
	public void save() {
		CompoundTag global = LevelData.getCompoundTag("global");
		global.put("items", createTag());
		LevelData.save();
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
		final BoxOfEirBlockEntity chest = ((ActiveChestStorage)player).getActiveBoxOfEir();
		return chest != null && !chest.stillValid(player) ? false : super.stillValid(player);
	}
	
	public void startOpen(Player player) {
		final BoxOfEirBlockEntity chest = ((ActiveChestStorage)player).getActiveBoxOfEir();
		if (chest != null) {
			chest.startOpen(player);
		}
		
		super.startOpen(player);
	}
	
	public void stopOpen(Player player) {
		final ActiveChestStorage cPlayer = (ActiveChestStorage)player;
		final BoxOfEirBlockEntity chest = cPlayer.getActiveBoxOfEir();
		if (chest != null) {
			chest.stopOpen(player);
		}
		
		super.stopOpen(player);
		cPlayer.setActiveBoxOfEir(null);
	}
	
	private static final int[] slots =  {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
	@Override
	public int[] getSlotsForFace(Direction direction) {
		return slots;
	}
	
	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return direction != Direction.DOWN;
	}
	
	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return direction == Direction.DOWN;
	}
}