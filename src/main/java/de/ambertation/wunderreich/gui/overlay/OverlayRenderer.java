package de.ambertation.wunderreich.gui.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderreich.items.Ruler;
import de.ambertation.wunderreich.items.data.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.utils.sdf.Bounds;
import de.ambertation.wunderreich.utils.sdf.Pos;
import de.ambertation.wunderreich.utils.sdf.Shape;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class OverlayRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static final OverlayRenderer INSTANCE = new OverlayRenderer();
    private final List<Pos> positions = new ArrayList<>(64);

    public void renderBlocks(PoseStack poseStack, Matrix4f matrix, Camera camera) {
        ItemStack ruler = null;
        for (ItemStack stack : Minecraft.getInstance().player.getHandSlots()) {
            {
                if (stack.is(WunderreichItems.RULER)) ruler = stack;
                break;
            }
        }
        if (ruler == null) return;

        //Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            ConstructionData constructionData = Ruler.getConstructionData(ruler);

            if (constructionData != null) {
                Vec3 camPos = camera.getPosition().reverse();
                Bounds box = constructionData.getBoundingBox();
                if (box != null) {
                    Pos pos = new Pos(camera.getPosition());
                    if (constructionData.inReach(pos)) {
                        renderPositions(poseStack, matrix, camPos);
                    }
                }
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double x, double y, double z) {
        ItemStack ruler = null;
        for (ItemStack stack : Minecraft.getInstance().player.getHandSlots()) {
            {
                if (stack.is(WunderreichItems.RULER)) ruler = stack;
                break;
            }
        }
        if (ruler == null) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (camera.isInitialized()) {
            Pos pos = new Pos(camera.getPosition());
            ConstructionData constructionData = Ruler.getConstructionData(ruler);
            ConstructionData.lastTarget = getTargetedBlock(Minecraft.getInstance().getCameraEntity(), 8, 3);
            Vec3 camPos = camera.getPosition().reverse();
            AABB boxOutline = new AABB(ConstructionData.lastTarget).move(camPos);

            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());


            LevelRenderer.renderLineBox(
                    poseStack, vertexConsumer,
                    boxOutline.minX, boxOutline.minY, boxOutline.minZ,
                    boxOutline.maxX, boxOutline.maxY, boxOutline.maxZ,
                    255f / 0xFF, 231f / 0xFF, 76f / 0xFF, 1
            );

            if (constructionData != null) {
                Bounds box = constructionData.getBoundingBox();
                if (box != null) {
                    boxOutline = box.toAABB().move(camPos);

                    if (constructionData.inReach(pos)) {
                        renderBlockOutline(
                                vertexConsumer,
                                poseStack,
                                boxOutline,
                                53f / 0xFF,
                                167f / 0xFF,
                                255f / 0xFF,
                                1
                        );


                        Shape e = box.innerBox();
                        positions.clear();
                        for (double xx = box.min.x; xx <= box.max.x; xx++) {
                            for (double xy = box.min.y; xy <= box.max.y; xy++) {
                                for (double xz = box.min.z; xz <= box.max.z; xz++) {
                                    final Pos p = new Pos(xx, xy, xz);
                                    double dist = e.dist(p);
                                    if (dist < 0 && dist > -1) positions.add(p);
                                }
                            }
                        }
                        renderPositionOutlines(vertexConsumer, poseStack, camPos);

                    } else
                        renderBlockOutline(
                                vertexConsumer,
                                poseStack,
                                boxOutline,
                                255f / 0xFF,
                                89f / 0xFF,
                                100f / 0xFF,
                                0.5f
                        );
                }
            }
        } else {
            ConstructionData.lastTarget = null;
        }
    }

    private void renderPositions(
            PoseStack poseStack, Matrix4f matrix,
            Vec3 camPos
    ) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.enableDepthTest();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        //render alpha components without depth-write
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (Pos p : positions) {
            renderBlock(bufferBuilder, poseStack, matrix, p, camPos, 217f / 0xff, 187f / 0xff, 249f / 0xff, 0.85f);
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        BufferUploader.drawWithShader(bufferBuilder.end());


        //render to depth Buffer
        bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (Pos p : positions) {
            renderBlock(bufferBuilder, poseStack, matrix, p, camPos, 1, 1, 1, 0.5f);
        }
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        BufferUploader.drawWithShader(bufferBuilder.end());


        //reset rendering system
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
    }

    private void renderPositionOutlines(
            VertexConsumer vertexConsumer,
            PoseStack poseStack,
            Vec3 camPos
    ) {
        for (Pos pos : positions) {
            renderBlockOutline(vertexConsumer, poseStack, pos, camPos, 99f / 0xff, 42f / 0xff, 80f / 0xff, 1f);
        }
    }

    private void renderBlock(
            BufferBuilder builder,
            PoseStack poseStack, Matrix4f matrix,
            Pos pos, Vec3 camPos,
            float r, float g, float b, float a
    ) {
        Matrix4f m = poseStack.last().pose();
        Matrix3f rotation = poseStack.last().normal();
        float lx = (float) ((int) pos.x + camPos.x + 0.2);
        float ly = (float) ((int) pos.y + camPos.y + 0.2);
        float lz = (float) ((int) pos.z + camPos.z + 0.2);
        float hx = (float) ((int) pos.x + camPos.x + 1 - 0.2);
        float hy = (float) ((int) pos.y + camPos.y + 1 - 0.2);
        float hz = (float) ((int) pos.z + camPos.z + 1 - 0.2);
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();

        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();

        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
    }

    private void renderBlockOutline(
            BufferBuilder builder,
            PoseStack poseStack, Matrix4f matrix,
            Pos pos, Vec3 camPos,
            float r, float g, float b, float a
    ) {
        Matrix4f m = poseStack.last().pose();
        Matrix3f rotation = poseStack.last().normal();

        final float nLen = (float) (1 / Math.sqrt(2));
        float lx = (float) ((int) pos.x + camPos.x + 0.1999);
        float ly = (float) ((int) pos.y + camPos.y + 0.1999);
        float lz = (float) ((int) pos.z + camPos.z + 0.1999);
        float hx = (float) ((int) pos.x + camPos.x + 1 - 0.1999);
        float hy = (float) ((int) pos.y + camPos.y + 1 - 0.1999);
        float hz = (float) ((int) pos.z + camPos.z + 1 - 0.1999);
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, -nLen, 0, -nLen).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, -nLen, 0, -nLen).endVertex();


//        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//
//        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//
//        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
//
//
//        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//
//        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
//        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
//
//        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//
//        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
//        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();

    }

    public static void renderFilledBox(
            double d,
            double e,
            double f,
            double g,
            double h,
            double i,
            float j,
            float k,
            float l,
            float m
    ) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, d, e, f, g, h, i, j, k, l, m);
        tesselator.end();
    }

    private void renderBlockOutline(
            VertexConsumer vertexConsumer,
            PoseStack poseStack,
            Pos pos, Vec3 camPos,
            float r, float g, float b, float a
    ) {
//        LevelRenderer.renderVoxelShape(
//                poseStack, vertexConsumer, Blocks.STONE.defaultBlockState().getShape(null, pos.toBlockPos()),
//                (int) pos.x + camPos.x, (int) pos.y + camPos.y, (int) pos.z + camPos.z,
//                r, g, b, a
//        );
        LevelRenderer.renderLineBox(
                poseStack, vertexConsumer,
                (int) pos.x + camPos.x + 0.2, (int) pos.y + camPos.y + 0.2, (int) pos.z + camPos.z + 0.2,
                (int) pos.x + camPos.x + 1 - 0.2, (int) pos.y + camPos.y + 1 - 0.2, (int) pos.z + camPos.z + 1 - 0.2,
                r, g, b, a
        );
//        renderFilledBox(
//                (int) pos.x + camPos.x + 0.2, (int) pos.y + camPos.y + 0.2, (int) pos.z + camPos.z + 0.2,
//                (int) pos.x + camPos.x + 1 - 0.2, (int) pos.y + camPos.y + 1 - 0.2, (int) pos.z + camPos.z + 1 - 0.2,
//                r, g, b, a
//        );

    }

    private void renderBlockOutline(
            VertexConsumer vertexConsumer,
            PoseStack poseStack,
            AABB boxOutline,
            float r, float g, float b, float a
    ) {
        LevelRenderer.renderLineBox(
                poseStack, vertexConsumer,
                boxOutline.minX, boxOutline.minY, boxOutline.minZ,
                boxOutline.maxX, boxOutline.maxY, boxOutline.maxZ,
                r, g, b, a
        );
    }

    @NotNull
    private BlockPos getTargetedBlock(Entity cameraEntity, int reach, int emptyDist) {
//        HitResult hitResult = cameraEntity.pick(reach, 0.0f, false);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();
//
//        hitResult = cameraEntity.pick(reach, 0.0f, true);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();

        return Pos.toBlockPos(cameraEntity.getEyePosition()
                                          .add(cameraEntity.getViewVector(1.0F).scale(emptyDist)));
    }
}
