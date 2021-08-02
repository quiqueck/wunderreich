package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.BoxOfEirContainerProvider;
import de.ambertation.wunderreich.inventory.BoxOfEirContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	@Shadow @NotNull public abstract MinecraftServer getServer();
	
	@Inject(method="saveLevelData", at=@At("TAIL"))
	public void wunderreich_save(CallbackInfo ci){
		if (getServer() instanceof BoxOfEirContainerProvider) {
			BoxOfEirContainer boxOfEirContainer = ((BoxOfEirContainerProvider) getServer()).getBoxOfEirContainer();
			if (boxOfEirContainer != null) {
				boxOfEirContainer.save();
			}
		}
	}
}
