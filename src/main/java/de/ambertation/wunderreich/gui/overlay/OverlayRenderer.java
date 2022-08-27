package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Matrix4;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.lib.math.sdf.shapes.Empty;
import de.ambertation.lib.ui.ColorHelper;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class OverlayRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final RenderContext ctx = new RenderContext();

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
    final List<BlockInfo> positions = new ArrayList<>(64);

    float time = 0;

    @ApiStatus.Internal
    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double x, double y, double z) {
        ctx.setPoseStack(poseStack);
        final Player player = Minecraft.getInstance().player;
        positions.clear();


        ItemStack ruler = player.getMainHandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) ruler = player.getOffhandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) return;


        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        SDF sdf = null;
        boolean showTargetBlock = false;
        if (camera.isInitialized()) {
            final Float3 cursorPos = getCursorPos(Minecraft.getInstance().getCameraEntity(), 8, 4);
            ConstructionData.setCursorPosOnClient(cursorPos);
            ctx.setCamera(camera);

            ctx.vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
            showTargetBlock = true;

            ConstructionData constructionData = ConstructionData.getConstructionData(ruler);
            if (constructionData != null) {

                SDF sdf_active = constructionData.getActiveSDF();
                if (sdf_active == null) return;
                sdf = sdf_active;


                if (sdf != null && !(sdf instanceof Empty)) {
                    TransformWidget widget = constructionData.getActiveTransformWidget();
                    sdf.setRootTransform(Matrix4.ofTranslation(constructionData.CENTER.get()));


                    TextRenderer.render(constructionData.CENTER.get(), COLOR_FIERY_ROSE);
                    TextRenderer.render(
                            constructionData.CENTER.get().add(0, -0.2, 0),
                            sdf.getLocalTransform().getBoundingBoxWorldSpace(),
                            COLOR_BOUNDING_BOX
                    );
                    TextRenderer.render(
                            constructionData.CENTER.get().add(0, -0.4, 0),
                            sdf.getBoundingBox(),
                            COLOR_BOUNDING_BOX
                    );


                    TextRenderer.render(
                            constructionData.CENTER.get().add(0, -0.6, 0),
                            sdf.getLocalTransform(),
                            ColorHelper.WHITE
                    );
                    LinePrimitives.renderBounds(ctx, sdf.getBoundingBox(), 0.1f, COLOR_BOUNDING_BOX, .25f);
                    time += Minecraft.getInstance().getDeltaFrameTime();
                    if (time > 10000) time -= 10000;
                    double scaledTime = time * 0.02;
                    float phase = (float) (Math.sin(Math.PI * 2 * (scaledTime - Math.floor(scaledTime))) + 1) / 2;

                    if (constructionData.getActiveTransformWidget() != null) {
                        LinePrimitives.renderSingleBlock(ctx, cursorPos, 0.4f, COLOR_SELECTION, 0.5f);
                        constructionData.getActiveTransformWidget().cursorTick(cursorPos);
                        constructionData.getActiveTransformWidget().render(ctx, phase);
                        showTargetBlock = !constructionData.getActiveTransformWidget().hasSelection();
                    }

                    renderSDF(ctx, sdf, sdf.getBoundingBox(), .2f, 0.8f, 1, false);
                }
            }

            positions.sort((a, b) -> {
                if (Math.abs(b.camDistSquare - a.camDistSquare) < 0.001) return 0;
                if (b.camDistSquare > a.camDistSquare) return 1;
                return -1;
            });

            renderPositionOutlines(ctx);
        } else {
            ConstructionData.setCursorPosOnClient(null);
        }

        if (showTargetBlock && ctx.vertexConsumer != null) {
//            LinePrimitives.renderSingleBlock(
//                    ctx,
//                    ConstructionData.getCursorPos().toBlockPos(),
//                    .01f,
//                    COLOR_SELECTION,
//                    1
//            );
//            TextRenderer.render(ConstructionData.getCursorPos().blockAligned(), COLOR_SELECTION);
//
//            if (sdf != null) {
//                for (Bounds.Interpolate i : Bounds.Interpolate.CORNERS_AND_CENTER) {
//                    printDist(sdf, i.t);
//                }
//            }
        }

        ctx.invalidate();
    }

    private void printDist(SDF sdf, Float3 oo) {
        oo = oo.sub(0.5);
        Float3 oa = ConstructionData.getCursorPos().blockAligned();
        Float3 op = oa.add(oo);
        Float3 ot = oa.add(oo.mul(1.3).sub(0.15));
        double dist = sdf.dist(op);
        dist = Math.round(dist * 4) / 4.0;
        DebugRenderer.renderFloatingText(Float3.toString(dist),
                (float) ot.x, (float) ot.y, (float) ot.z,
                dist < 0 ? FILL_COLORS[0] : FILL_COLORS[FILL_COLORS.length - 1]
        );
    }

    private void renderSDF(
            RenderContext ctx,
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

            positions.add(BlockInfo.withCamPos(
                    p,
                    ctx.camToWorldSpace,
                    deflate,
                    FILL_COLORS[mIdx % FILL_COLORS.length],
                    alpha,
                    OUTLINE_COLORS[mIdx % OUTLINE_COLORS.length],
                    lineAlpha
            ));
        }, debugDist ? (p, ed, didPlace) -> {
            DebugRenderer.renderFloatingText(
                    "" + (Math.round(4 * ed.dist()) / 4.0),
                    p.x, p.y, p.z,
                    (ed.dist() < 0 ? COLOR_FIERY_ROSE : COLOR_BLUE_JEANS)
            );
        } : null);
    }

    @ApiStatus.Internal
    public void renderPositionBlocks(PoseStack poseStack, Camera camera) {
        if (camera.isInitialized()) {
            ctx.setPoseStack(poseStack);
            ctx.worldToCamSpace = camera.getPosition().reverse();
            BlockInfo.renderTransparentPositions(ctx, positions);
        }
    }

    private void renderPositionOutlines(RenderContext ctx) {
        for (BlockInfo pos : positions) {
            if (pos.outlineAlpha > 0) {
                LinePrimitives.renderSingleBlock(
                        ctx,
                        pos.pos,
                        pos.deflate - 0.0001f,
                        pos.outlineColor,
                        pos.outlineAlpha
                );
            }
        }
    }

    private Float3 getCursorPos(Entity cameraEntity, int reach, int emptyDist) {
        return Float3.of(cameraEntity.getEyePosition()
                                     .add(cameraEntity.getViewVector(1.0F).scale(emptyDist)));
    }

    @NotNull
    private BlockPos getTargetedBlock(Entity cameraEntity, int reach, int emptyDist) {
//        HitResult hitResult = cameraEntity.pick(reach, 0.0f, false);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();
//
//        hitResult = cameraEntity.pick(reach, 0.0f, true);
//        if (hitResult.getType() == HitResult.Type.BLOCK) return ((BlockHitResult) hitResult).getBlockPos();

        return getCursorPos(cameraEntity, reach, emptyDist).toBlockPos();
    }
}
