package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.inventory.BoxOfEirContainer;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Main.class)
abstract public class MainMixin {
	@ModifyArg(method="main", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;convertFromRegionFormatIfNeeded(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)V"))
	private static LevelStorageSource.LevelStorageAccess bclib_callServerFix(LevelStorageSource.LevelStorageAccess session){
		BoxOfEirContainer.LevelData.init(session);
		return session;
	}
}

