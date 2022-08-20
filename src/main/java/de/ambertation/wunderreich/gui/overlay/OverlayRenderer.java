package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.SDFMove;
import de.ambertation.lib.math.sdf.interfaces.BoundedShape;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

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
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class OverlayRenderer implements DebugRenderer.SimpleDebugRenderer {
    record RenderInfo(Float3 pos, double camDistSquare, float deflate,
                      int color, float alpha,
                      int outlineColor, float outlineAlpha) {
        public static RenderInfo withCamPos(
                Float3 pos, Float3 camPos, float deflate,
                int color, float alpha,
                int outlineColor, float outlineAlpha
        ) {
            return new RenderInfo(
                    pos,
                    pos.sub(camPos).lengthSquare(),
                    deflate,
                    color,
                    alpha,
                    outlineColor,
                    outlineAlpha
            );
        }
    }

    public static final int[] FILL_COLORS = {
            0xFFAA574A,
            0xFFBE647D,
            0xFFB381B3,
            0xFF84A5D7,
            0xFF39C6DC,
            0xFF30E0BF,
            0xFF8BF28F,
            0xFFE8F966
    };

    public static final int[] OUTLINE_COLORS = {
            0xFF331E2A,
            0xFF44374C,
            0xFF4A546E,
            0xFF45748C,
            0xFF3896A0,
            0xFF39B8A9,
            0xFF5ED9A5,
            0xFF97F799
    };
    public static final int COLOR_MINION_YELLOW = 0xFFFFE74C;
    public static final int COLOR_FIERY_ROSE = 0xFFFF5964;
    public static final int COLOR_PURPLE = 0xFF5F00BA;
    public static final int COLOR_MEDIUM_PURPLE = 0xFFAB69F2;
    public static final int COLOR_RICH_BLACK = 0xFF090909;
    public static final int COLOR_BLUE_JEANS = 0xFF35A7FF;
    public static final int COLOR_MAUVE = 0xFFD9BBF9;
    public static final int COLOR_DARK_MAUVE = 0xFFCBA2F6;
    public static final int COLOR_DARK_GREEN_MOSS = 0xFF3F612D;

    public static final int COLOR_SELECTION = COLOR_MINION_YELLOW;
    public static final int COLOR_OUT_OF_REACH = COLOR_FIERY_ROSE;
    public static final int COLOR_BOUNDING_BOX = COLOR_BLUE_JEANS;
    public static final int COLOR_BLOCK_PREVIEW_FILL = COLOR_MAUVE;
    public static final int COLOR_BLOCK_PREVIEW_OUTLINE = COLOR_DARK_MAUVE;

    public static final OverlayRenderer INSTANCE = new OverlayRenderer();
    private final List<RenderInfo> positions = new ArrayList<>(64);

    float time = 0;

    @ApiStatus.Internal
    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double x, double y, double z) {
        final Player player = Minecraft.getInstance().player;
        positions.clear();

        //-4, 63, -24


        ItemStack ruler = player.getMainHandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) ruler = player.getOffhandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) return;


        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (camera.isInitialized()) {
            Float3 pos = Float3.of(camera.getPosition());

            ConstructionData.setLastTargetOnClient(getTargetedBlock(Minecraft.getInstance().getCameraEntity(), 8, 5));
            Vec3 camPos = camera.getPosition().reverse();
            final Float3 camP = pos;

            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
            renderBlockOutline(vertexConsumer, poseStack,
                    ConstructionData.getLastTarget(), camPos, .01f, COLOR_SELECTION, 1
            );

            ConstructionData constructionData = ConstructionData.getConstructionData(ruler);
            if (constructionData != null) {
                SDF sdf_active = constructionData.getActiveSDF();
                if (sdf_active == null) return;
                SDF sdf_root = sdf_active.getRoot();
                SDF sdf = sdf_active;
                SDF sdf_moved_root = sdf_root;


                if (sdf != null && !(sdf instanceof Empty)) {
                    Float3 offset = constructionData.CENTER.get();
                    if (offset != null) {
                        sdf = new SDFMove(sdf, offset);
                        sdf_moved_root = new SDFMove(sdf_root, offset);
                    }


                    Bounds box = sdf.getBoundingBox();
                    Bounds rootBox = sdf_moved_root.getBoundingBox();
                    final Float3 targetPos = Float3.of(ConstructionData.getLastTarget());
                    final Bounds.Interpolate targetCorner = box.isCornerOrCenter(targetPos);

                    time += Minecraft.getInstance().getDeltaFrameTime();
                    if (time > 10000) time -= 10000;
                    double scaledTime = time * 0.02;
                    float phase = (float) (Math.sin(Math.PI * 2 * (scaledTime - Math.floor(scaledTime))) + 1) / 2;


                    if (constructionData.inReach(pos)) {
                        //resize/move bounding Box
                        if (constructionData.getSelectedCorner() != null) {
                            Bounds.Interpolate selectedCorner = constructionData.getSelectedCorner();

                            box = constructionData.getNewBoundsForSelectedCorner();
                            renderBlockOutline(
                                    vertexConsumer, poseStack,
                                    box.get(selectedCorner), camPos, 0.1f,
                                    COLOR_PURPLE, 1
                            );

                            if (sdf_active instanceof BoundedShape bs) {
                                if (offset != null) {
                                    bs.setFromBoundingBox(box.move(offset.mul(-1)));
                                } else {
                                    bs.setFromBoundingBox(box);
                                }
                                constructionData.SDF_DATA.set(sdf_root);
                            }
                        }

                        renderBlockOutline(vertexConsumer, poseStack, box, camPos, 0, COLOR_BOUNDING_BOX, 1);
                        renderBlockOutline(vertexConsumer, poseStack, rootBox, camPos, 0, COLOR_BOUNDING_BOX, .25f);


                        if (constructionData.getSelectedCorner() == null) {
                            for (Bounds.Interpolate corner : Bounds.Interpolate.CORNERS_AND_CENTER) {
                                if ((targetCorner != null && targetCorner.idx == corner.idx)) {
                                    positions.add(RenderInfo.withCamPos(
                                            box.get(corner), camP, 0.1f,
                                            COLOR_FIERY_ROSE, 0.5f,
                                            COLOR_FIERY_ROSE, 0.8f
                                    ));
                                } else {
                                    positions.add(RenderInfo.withCamPos(
                                            box.get(corner), camP, 0.1f,
                                            blendColors(phase, COLOR_BOUNDING_BOX, COLOR_SELECTION), 0.8f,
                                            COLOR_SELECTION, phase
                                    ));
                                    renderBlockOutline(
                                            vertexConsumer, poseStack, box.get(corner), camPos, 0.1f,
                                            blendColors(phase, COLOR_BOUNDING_BOX, COLOR_SELECTION), 1
                                    );
                                }
                            }
                        }

                        renderSDF(camP, sdf_moved_root, rootBox.blockAligned(), 0.3f, 0.75f, 0, false);
                        renderSDF(camP, sdf, box.blockAligned(), 0.2f, 0.95f, 1, true);

                        renderPositionOutlines(vertexConsumer, poseStack, camPos);

                    } else
                        renderBlockOutline(vertexConsumer, poseStack, box, camPos, 0, COLOR_OUT_OF_REACH, 1);
                }
            }

            positions.sort((a, b) -> {
                if (Math.abs(b.camDistSquare - a.camDistSquare) < 0.001) return 0;
                if (b.camDistSquare > a.camDistSquare) return 1;
                return -1;
            });
        } else {
            ConstructionData.setLastTargetOnClient(null);
        }


    }

    private void renderSDF(
            Float3 camP,
            SDF sdf,
            Bounds box,
            float deflate,
            float alpha,
            float lineAlpha,
            boolean debugDist
    ) {
        sdf.evaluate(box, (p, ed) -> {
            int mIdx = 0;
            if (ed.source() instanceof MaterialProvider mp)
                mIdx = mp.getMaterialIndex();

            positions.add(RenderInfo.withCamPos(
                    p,
                    camP,
                    deflate,
                    FILL_COLORS[mIdx % FILL_COLORS.length],
                    alpha,
                    OUTLINE_COLORS[mIdx % OUTLINE_COLORS.length],
                    lineAlpha
            ));
        }, debugDist ? (p, ed, didPlace) -> {
            DebugRenderer.renderFloatingText(
                    ed.source().getGraphIndex() + ":" + (Math.round(4 * ed.dist()) / 4.0),
                    p.x + 0.5, p.y + 0.5, p.z + 0.5,
                    ed.dist() < 0 ? COLOR_FIERY_ROSE : COLOR_BLUE_JEANS
            );
        } : null);
    }

    @ApiStatus.Internal
    public void renderBlocks(PoseStack poseStack, Camera camera) {
        if (camera.isInitialized()) {
            Vec3 camPos = camera.getPosition().reverse();
            renderPositions(poseStack, camPos);
        }
    }

    private int blendColors(float t, int c1, int c2) {
        int r = (int) (t * FastColor.ARGB32.red(c2) + (1 - t) * FastColor.ARGB32.red(c1));
        int g = (int) (t * FastColor.ARGB32.green(c2) + (1 - t) * FastColor.ARGB32.green(c1));
        int b = (int) (t * FastColor.ARGB32.blue(c2) + (1 - t) * FastColor.ARGB32.blue(c1));
        int a = (int) (t * FastColor.ARGB32.alpha(c2) + (1 - t) * FastColor.ARGB32.alpha(c1));
        return FastColor.ARGB32.color(a, r, g, b);
    }


    private void renderBlockOutline(
            VertexConsumer vertexConsumer, PoseStack poseStack,
            BlockPos pos, Vec3 camPos, float deflate,
            int color, float alpha
    ) {
        final float x = (float) (pos.getX() + camPos.x);
        final float y = (float) (pos.getY() + camPos.y);
        final float z = (float) (pos.getZ() + camPos.z);
        renderLineBox(vertexConsumer, poseStack,
                x + deflate, y + deflate, z + deflate,
                1 + x - deflate, 1 + y - deflate, 1 + z - deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private void renderBlockOutline(
            VertexConsumer vertexConsumer, PoseStack poseStack,
            Float3 pos, Vec3 camPos, float deflate,
            int color,
            float alpha
    ) {
        final float x = (float) (pos.x + camPos.x);
        final float y = (float) (pos.y + camPos.y);
        final float z = (float) (pos.z + camPos.z);
        renderLineBox(vertexConsumer, poseStack,
                x + deflate, y + deflate, z + deflate,
                1 + x - deflate, 1 + y - deflate, 1 + z - deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private void renderBlockOutline(
            VertexConsumer vertexConsumer, PoseStack poseStack,
            Bounds bounds, Vec3 camPos, float deflate,
            int color,
            float alpha
    ) {
        renderLineBox(
                vertexConsumer, poseStack,
                (float) (bounds.min.x + camPos.x) + deflate,
                (float) (bounds.min.y + camPos.y) + deflate,
                (float) (bounds.min.z + camPos.z) + deflate,
                (float) (1 + bounds.max.x + camPos.x) - deflate,
                (float) (1 + bounds.max.y + camPos.y) - deflate,
                (float) (1 + bounds.max.z + camPos.z) - deflate,
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private static void renderLineBox(
            VertexConsumer vertexConsumer, PoseStack poseStack,
            float lx, float ly, float lz,
            float hx, float hy, float hz,
            int r, int g, int b, int a
    ) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
    }


    private void renderBlock(
            BufferBuilder builder, PoseStack poseStack,
            Float3 pos, Vec3 camPos, float deflate,
            int color, float alpha
    ) {
        renderBlock(
                builder, poseStack, pos, camPos, deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private void renderBlock(
            BufferBuilder builder,
            PoseStack poseStack,
            Float3 pos, Vec3 camPos, float deflate,
            int r, int g, int b, int a
    ) {
        Matrix4f m = poseStack.last().pose();
        Matrix3f rotation = poseStack.last().normal();
        float lx = (float) (pos.x + camPos.x);
        float ly = (float) (pos.y + camPos.y);
        float lz = (float) (pos.z + camPos.z);
        float hx = lx + 1 - deflate;
        float hy = ly + 1 - deflate;
        float hz = lz + 1 - deflate;
        lx += deflate;
        ly += deflate;
        lz += deflate;
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
            VertexConsumer vertexConsumer,
            PoseStack poseStack,
            Float3 pos, Vec3 camPos,
            float r, float g, float b, float a
    ) {
        LevelRenderer.renderLineBox(
                poseStack, vertexConsumer,
                (int) pos.x + camPos.x + 0.2, (int) pos.y + camPos.y + 0.2, (int) pos.z + camPos.z + 0.2,
                (int) pos.x + camPos.x + 1 - 0.2, (int) pos.y + camPos.y + 1 - 0.2, (int) pos.z + camPos.z + 1 - 0.2,
                r, g, b, a
        );
    }

    private void renderPositionOutlines(VertexConsumer vertexConsumer, PoseStack poseStack, Vec3 camPos) {
        for (RenderInfo pos : positions) {
            if (pos.outlineAlpha > 0) {
                renderBlockOutline(
                        vertexConsumer, poseStack,
                        pos.pos, camPos, pos.deflate - 0.0001f,
                        pos.outlineColor, pos.outlineAlpha
                );
            }
        }
    }

    private void renderPositions(PoseStack poseStack, Vec3 camPos) {
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
        for (RenderInfo p : positions) {
            renderBlock(bufferBuilder, poseStack, p.pos, camPos, p.deflate, p.color, p.alpha);
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        BufferUploader.drawWithShader(bufferBuilder.end());


        //render to depth Buffer
        bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (RenderInfo p : positions) {
            renderBlock(bufferBuilder, poseStack, p.pos, camPos, p.deflate, 0, 0, 0, 1);
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

    @NotNull
    private BlockPos getTargetedBlock(Entity cameraEntity, int reach, int emptyDist) {
//        HitResult hitResult = cameraEntity.pick(reach, 0.0f, false);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();
//
//        hitResult = cameraEntity.pick(reach, 0.0f, true);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();

        return Float3.toBlockPos(cameraEntity.getEyePosition()
                                             .add(cameraEntity.getViewVector(1.0F).scale(emptyDist)));
    }
}
