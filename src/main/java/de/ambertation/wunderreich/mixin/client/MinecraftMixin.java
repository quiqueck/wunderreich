package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.inventory.BoxOfEirContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	@Final
	private LevelStorageSource levelSource;
	
	@Inject(method="loadLevel", at=@At("HEAD"))
	private void wunderreich_loadData(String levelID, CallbackInfo ci){
		BoxOfEirContainer.LevelData.init(levelSource, levelID);
	}
	
	@Inject(method="createLevel", at=@At("HEAD"))
	private void wunderreich_loadData(String levelID, LevelSettings levelSettings, RegistryHolder registryHolder, WorldGenSettings worldGenSettings, CallbackInfo ci) {
		BoxOfEirContainer.LevelData.init(levelSource, levelID);
	}
}