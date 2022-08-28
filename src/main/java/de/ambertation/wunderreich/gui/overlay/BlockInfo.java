package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;


@Environment(EnvType.CLIENT)
final class BlockInfo {
    public final Float3 pos;
    public final double camDistSquare;
    public final float deflate;
    public final int color;
    public final float alpha;
    public final int outlineColor;
    public final float outlineAlpha;

    BlockInfo(
            Float3 pos, double camDistSquare, float deflate,
            int color, float alpha,
            int outlineColor, float outlineAlpha
    ) {
        this.pos = pos;
        this.camDistSquare = camDistSquare;
        this.deflate = deflate;
        this.color = color;
        this.alpha = alpha;
        this.outlineColor = outlineColor;
        this.outlineAlpha = outlineAlpha;
    }

    public static BlockInfo withCamPos(
            Float3 pos, Float3 camPos, float deflate,
            int color, float alpha,
            int outlineColor, float outlineAlpha
    ) {
        return new BlockInfo(
                pos,
                pos.sub(camPos).lengthSquare(),
                deflate,
                color,
                alpha,
                outlineColor,
                outlineAlpha
        );
    }

    static void renderTransparentPositions(RenderContext ctx, List<BlockInfo> positions, Float3 refPlanePosition) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.enableDepthTest();


        //render alpha components without depth-write
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        drawAll(ctx, tesselator, positions);
        drawRefPlane(ctx, tesselator, refPlanePosition);

        //render to depth Buffer
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        drawAll(ctx, tesselator, positions);
        //drawRefPlane(ctx, tesselator, refPlanePosition);

        //reset rendering system
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
    }

    static void drawAll(RenderContext ctx, Tesselator tesselator, List<BlockInfo> positions) {
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (BlockInfo nfo : positions) {
            SolidPrimitives.renderSingleBlock(ctx, bufferBuilder, nfo);
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    static void drawRefPlane(RenderContext ctx, Tesselator tesselator, Float3 refPlanePosition) {
        if (refPlanePosition != null) {
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            SolidPrimitives.renderQuadXZ(
                    ctx,
                    bufferBuilder,
                    refPlanePosition,
                    Float2.IDENTITY,
                    0xFFFFFFFF,
                    .2f
            );

            BufferUploader.drawWithShader(bufferBuilder.end());
        }
    }
}
