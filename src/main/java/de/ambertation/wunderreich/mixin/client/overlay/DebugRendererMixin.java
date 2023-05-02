package de.ambertation.wunderreich.mixin.client.overlay;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.OverlayRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {


    @Inject(method = "render", at = @At("HEAD"))
    void wunder_renderFirst(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
            double x, double y, double z,
            CallbackInfo ci
    ) {
        if (Configs.MAIN.allowConstructionTools.get()) {
            OverlayRenderer.INSTANCE.render(poseStack, bufferSource, x, y, z);
        }
    }
}
