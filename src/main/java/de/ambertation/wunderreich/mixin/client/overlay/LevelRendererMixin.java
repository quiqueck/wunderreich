package de.ambertation.wunderreich.mixin.client.overlay;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.overlay.OverlayRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    void wunder_renderFirst(
            PoseStack poseStack,
            float f,
            long l,
            boolean bl,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f matrix4f,
            CallbackInfo ci
    ) {
        if (Configs.MAIN.allowConstructionTools.get()) {
            OverlayRenderer.INSTANCE.renderPositionBlocks(poseStack, camera);
        }
    }
}
