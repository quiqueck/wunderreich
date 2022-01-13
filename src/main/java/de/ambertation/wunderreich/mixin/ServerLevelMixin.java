package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.interfaces.BoxOfEirContainerProvider;
import de.ambertation.wunderreich.inventory.BoxOfEirContainer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.NotNull;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Inject(method = "saveLevelData", at = @At("TAIL"))
    public void wunderreich_save(CallbackInfo ci) {
        if (getServer() instanceof BoxOfEirContainerProvider) {
            BoxOfEirContainer boxOfEirContainer = ((BoxOfEirContainerProvider) getServer()).getBoxOfEirContainer();
            if (boxOfEirContainer != null) {
                boxOfEirContainer.save();
            }
        }
    }
}
