package de.ambertation.wunderreich.mixin.client.overlay;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.OverlayRenderer;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"))
    void wunderreich_render(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Configs.MAIN.allowConstructionTools.get()) {
            OverlayRenderer.INSTANCE.renderHUD(guiGraphics);
        }
    }
}
