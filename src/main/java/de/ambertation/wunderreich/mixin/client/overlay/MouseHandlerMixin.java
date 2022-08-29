package de.ambertation.wunderreich.mixin.client.overlay;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.InputManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    public void wunderreich_onMove(long l, double mouseX, double mouseY, CallbackInfo ci) {
        if (Configs.MAIN.allowConstructionTools.get()) {
            if (l == Minecraft.getInstance().getWindow().getWindow()) {
                if (this instanceof MouseHandlerAccessor h) {
                    if (InputManager.INSTANCE.onMove(h, mouseX, mouseY)) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void wunderreich_onPress(long l, int button, int state, int unk, CallbackInfo ci) {
        if (Configs.MAIN.allowConstructionTools.get()) {
            if (l == Minecraft.getInstance().getWindow().getWindow()) {
                if (this instanceof MouseHandlerAccessor h) {
                    if (InputManager.INSTANCE.onPress(button, state, unk)) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
