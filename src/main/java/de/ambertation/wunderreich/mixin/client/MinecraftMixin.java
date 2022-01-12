package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.config.LevelData;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 200)
public class MinecraftMixin {
    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void wunderreich_loadLevel(String levelID, CallbackInfo ci) {
        LevelData.getInstance().loadNewLevel(levelSource, levelID);
    }
}
