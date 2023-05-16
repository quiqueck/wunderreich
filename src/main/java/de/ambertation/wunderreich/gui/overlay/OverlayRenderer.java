package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float2;
import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Matrix4;
import de.ambertation.wunderlib.math.sdf.SDF;
import de.ambertation.wunderlib.math.sdf.interfaces.MaterialProvider;
import de.ambertation.wunderlib.math.sdf.shapes.Empty;
import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderreich.items.construction.ConstructionData;
import de.ambertation.wunderreich.registries.WunderreichItems;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    private static Float3 refPlanePosition = null;

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
        ctx.setBufferSource(multiBufferSource);
        ctx.setPoseStack(poseStack);
        final Player player = Minecraft.getInstance().player;
        positions.clear();
        refPlanePosition = null;


        ItemStack ruler;
        if (InputManager.INSTANCE.inTransformMode()) ruler = InputManager.INSTANCE.getActiveRuler();
        else ruler = player.getMainHandItem();

        if (ruler == null || !ruler.is(WunderreichItems.RULER)) ruler = player.getOffhandItem();
        if (ruler == null || !ruler.is(WunderreichItems.RULER)) return;

        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        boolean showTargetBlock = false;
        if (camera.isInitialized()) {
            Float3 cursorPos = getCursorPos(Minecraft.getInstance().getCameraEntity(), 8, 4);
            cursorPos = cursorPos.mul(2).round().div(2);
            ConstructionData.setCursorPosOnClient(cursorPos);
            ctx.setCamera(camera);
            InputManager.INSTANCE.setCamera(camera);
            ctx.vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
            showTargetBlock = true;

            ConstructionData constructionData = ConstructionData.getConstructionData(ruler);

            if (constructionData != null) {
                SDF sdf = constructionData.getActiveSDF();
                if (sdf == null && !(sdf instanceof Empty)) return;

                TransformWidget widget = constructionData.getActiveTransformWidget();
                sdf.setRootTransform(Matrix4.ofTranslation(constructionData.CENTER.get()));

                TextRenderer.render(ctx, constructionData.CENTER.get(), COLOR_FIERY_ROSE);
                //LinePrimitives.renderBounds(ctx, sdf.getBoundingBox(), 0.1f, COLOR_BOUNDING_BOX, .25f);

                time += Minecraft.getInstance().getDeltaFrameTime();
                if (time > 10000) time -= 10000;
                double scaledTime = time * 0.02;
                float phase = (float) (Math.sin(Math.PI * 2 * (scaledTime - Math.floor(scaledTime))) + 1) / 2;

                if (widget != null) {
                    widget.cursorTick(cursorPos);
                    //LinePrimitives.renderSingleBlock(ctx, cursorPos, 0.45f, COLOR_SELECTION, 0.5f);
                    final Float3 bottom;
                    if (widget.hasHovered()) {
                        bottom = Float3.of(cursorPos.x, widget.hoveredCornerPos().y - 0.5, cursorPos.z);
                    } else {
                        bottom = Float3.of(cursorPos.x, Math.floor(cursorPos.y), cursorPos.z);
                        refPlanePosition = Float3.of(
                                Float3.toBlockPos(cursorPos.x),
                                Math.floor(cursorPos.y),
                                Float3.toBlockPos(cursorPos.z)
                        );
                        LinePrimitives.renderQuadXZ(ctx, refPlanePosition, Float2.IDENTITY, 0, .75f);
                    }
                    LinePrimitives.renderQuadXZ(ctx, cursorPos, Float2.of(.1, .1), COLOR_SELECTION, .75f);
                    LinePrimitives.renderLine(ctx, bottom, cursorPos, COLOR_SELECTION, 1);
                    LinePrimitives.renderQuadXZ(ctx, bottom, Float2.of(.1, .1), 0, .75f);

                    widget.render(ctx, phase);
                    showTargetBlock = !widget.hasSelection();
                }

                renderSDF(ctx, sdf, sdf.getBoundingBox(), .1f, 0.8f, 1, false);
                if (sdf.getParent() != null)
                    renderSDF(ctx, sdf.getRoot(), sdf.getRoot().getBoundingBox(), .1f, 0.3f, 0.3f, false);
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
        ctx.renderAllText();
        ctx.invalidate();
    }

    private void printDist(RenderContext ctx, SDF sdf, Float3 oo) {
        oo = oo.sub(0.5);
        Float3 oa = ConstructionData.getCursorPos().blockAligned();
        Float3 op = oa.add(oo);
        Float3 ot = oa.add(oo.mul(1.3).sub(0.15));
        double dist = sdf.dist(op);
        dist = Math.round(dist * 4) / 4.0;
        ctx.pushText(
                Float3.toString(dist),
                ot,
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
            ctx.pushText(
                    "" + (Math.round(4 * ed.dist()) / 4.0),
                    p,
                    (ed.dist() < 0 ? COLOR_FIERY_ROSE : COLOR_BLUE_JEANS)
            );
        } : null);
    }

    @ApiStatus.Internal
    public void renderPositionBlocks(PoseStack poseStack, Camera camera) {
        if (camera.isInitialized()) {
            ctx.setPoseStack(poseStack);
            ctx.worldToCamSpace = camera.getPosition().reverse();
            BlockInfo.renderTransparentPositions(ctx, positions, refPlanePosition);
        }
    }

    @ApiStatus.Internal
    public void renderHUD(GuiGraphics guiGraphics) {
        final Font font = Minecraft.getInstance().font;
        if (InputManager.INSTANCE.inTransformMode()) {
            Component c = null;
            if (InputManager.INSTANCE.getMode() == InputManager.Mode.NONE) {
                c = Component.literal(
                        "Transform Mode:\n  **t**: Move\n  **r**: Rotate\n  **z**: Scale\n  **ALT+t**,**ALT+r**,**ALT+z**: Reset");

            } else if (InputManager.INSTANCE.getMode() == InputManager.Mode.TRANSLATE) {
                c = Component.literal(
                        "Transform Mode - Move:\n  **x**, **y**, **z**: Transform along Axis\n **SHIFT**: Lock Axis\n **CTRL**: Round");

            } else if (InputManager.INSTANCE.getMode() == InputManager.Mode.ROTATE) {
                c = Component.literal(
                        "Transform Mode - Rotate:\n  **x**, **y**, **z**: Transform along Axis\n **SHIFT**: Lock Axis\n **CTRL**: Round");

            } else if (InputManager.INSTANCE.getMode() == InputManager.Mode.SCALE) {
                c = Component.literal(
                        "Transform Mode - Scale:\n  **x**, **y**, **z**: Transform along Axis\n **SHIFT**: Lock Axis\n **CTRL**: Round");

            }
            float y = 8;
            if (c != null) {
                y = TextRenderer.draw(
                        guiGraphics,
                        font,
                        Float2.of(8),
                        Minecraft.getInstance().getWindow().getWidth() - 16,
                        c,
                        ColorHelper.WHITE
                );
            }

            if (InputManager.INSTANCE.getMode() != InputManager.Mode.NONE) {
                String axis = null;
                String type = "axis";
                String space = "world";

                if ((InputManager.INSTANCE.getLockFlag() & InputManager.LOCK_X) != 0) axis = "x";
                if ((InputManager.INSTANCE.getLockFlag() & InputManager.LOCK_Y) != 0) axis = "y";
                if ((InputManager.INSTANCE.getLockFlag() & InputManager.LOCK_Z) != 0) axis = "z";
                if ((InputManager.INSTANCE.getLockFlag() & InputManager.LOCK_INVERT) != 0) type = "inverted";
                if ((InputManager.INSTANCE.getLockFlag() & InputManager.LOCK_LOCAL) != 0 && !InputManager.INSTANCE.willWriteAbsolute())
                    space = "local";

                if (axis != null) {
                    y = TextRenderer.draw(
                            guiGraphics,
                            font,
                            Float2.of(8, y),
                            Minecraft.getInstance().getWindow().getWidth() - 16,
                            Component.translatable("info.wunderreich." + space + "_" + axis + "_" + type)
                            ,
                            ColorHelper.YELLOW
                    );
                }
            }

            if (InputManager.INSTANCE.getMode() != InputManager.Mode.NONE) {
                String write = "change";
                if ((InputManager.INSTANCE.willWriteAbsolute())) write = "write";

                if (InputManager.INSTANCE.hasNumberString()) {
                    y = TextRenderer.draw(
                            guiGraphics,
                            font,
                            Float2.of(8, y),
                            Minecraft.getInstance().getWindow().getWidth() - 16,
                            Component.literal(InputManager.INSTANCE.getNumberString() + " [" + InputManager.INSTANCE.getDeltaString() + "]")
                                     .append(Component.translatable("info.wunderreich.transform_" + write)),
                            InputManager.INSTANCE.isValidNumberString() ? ColorHelper.YELLOW : ColorHelper.RED
                    );
                } else {
                    y = TextRenderer.draw(
                            guiGraphics,
                            font,
                            Float2.of(8, y),
                            Minecraft.getInstance().getWindow().getWidth() - 16,
                            Component.literal(InputManager.INSTANCE.getDeltaString())
                                     .append(Component.translatable("info.wunderreich.transform_" + write)),
                            ColorHelper.YELLOW
                    );
                }
                if (Minecraft.getInstance().getWindow() != null) {
                    final int widgetSize = 100;
                    final Rectangle r = new Rectangle(
                            8, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 8 - widgetSize,
                            widgetSize, widgetSize
                    );
                    final Float2 center = r.center();
                    final Float2 offset = center.add(InputManager.INSTANCE.getMouseDelta());

                    guiGraphics.fill(
                            (int) center.x - 2,
                            (int) center.y - 2,
                            (int) center.x + 2,
                            (int) center.y + 2,
                            0x77000000
                    );
                    guiGraphics.fill(
                            (int) center.x - 1,
                            (int) center.y - 1,
                            (int) center.x + 1,
                            (int) center.y + 1,
                            0x77FFFFFF
                    );

                    guiGraphics.fill(
                            (int) offset.x - 2,
                            (int) offset.y - 2,
                            (int) offset.x + 2,
                            (int) offset.y + 2,
                            ColorHelper.BLACK
                    );

                    guiGraphics.fill(
                            (int) offset.x - 1,
                            (int) offset.y - 1,
                            (int) offset.x + 1,
                            (int) offset.y + 1,
                            ColorHelper.YELLOW
                    );


                    RenderHelper.outline(guiGraphics, r.left, r.top, r.right(), r.bottom(), 0x77FFFFFF);
                }
            }


            ItemStack ruler = InputManager.INSTANCE.getActiveRuler();
            if (ruler != null) {
                ConstructionData cd = ConstructionData.getConstructionData(ruler);
                if (cd != null) {
                    SDF sdf = cd.getActiveSDF();
                    if (sdf != null) {
                        y += 8;
                        y = TextRenderer.draw(
                                guiGraphics,
                                font,
                                Float2.of(8, y),
                                Minecraft.getInstance().getWindow().getWidth() - 16,
                                Component.literal("c: " + sdf.getLocalTransform().center.toString()),
                                ColorHelper.YELLOW
                        );
                        y = TextRenderer.draw(
                                guiGraphics,
                                font,
                                Float2.of(8, y),
                                Minecraft.getInstance().getWindow().getWidth() - 16,
                                Component.literal("s: " + sdf.getLocalTransform().size.toString()),
                                ColorHelper.YELLOW
                        );
                        y = TextRenderer.draw(
                                guiGraphics,
                                font,
                                Float2.of(8, y),
                                Minecraft.getInstance().getWindow().getWidth() - 16,
                                Component.literal("r: " + sdf.getLocalTransform().rotation.toEuler().toString()),
                                ColorHelper.YELLOW
                        );
                    }
                }
            }
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
