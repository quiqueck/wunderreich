package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Matrix4;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.ui.ColorHelper;

import net.minecraft.nbt.CompoundTag;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransformWidget {
    @NotNull
    private Transform transform;
    @NotNull
    private Matrix4 toWorldMatrix;

    @Nullable
    private Transform changedTransform;
    @Nullable
    private Bounds.Interpolate hoveredCorner;
    @Nullable
    private Bounds.Interpolate selectedCorner;

    @NotNull
    private Float3 cursorPos = Float3.ZERO;

    public TransformWidget(@NotNull Transform transform) {
        this(transform, Matrix4.IDENTITY);
    }

    public TransformWidget(@NotNull Transform transform, @NotNull Matrix4 toWorldMatrix) {
        this.transform = transform;
        this.toWorldMatrix = toWorldMatrix;
    }

    @Environment(EnvType.CLIENT)
    public void render(@NotNull RenderContext ctx, float phase) {
        Float3 corner;
        Float3[] corners = getCornersInWorldSpace();

        if (selectedCorner != null) {
            LinePrimitives.renderCorners(ctx, corners, OverlayRenderer.COLOR_FIERY_ROSE, 1);
            corner = corners[selectedCorner.idx];
            OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                    corner, ctx.camToWorldSpace, 0.1f,
                    ColorHelper.blendColors(
                            phase,
                            OverlayRenderer.COLOR_FIERY_ROSE,
                            OverlayRenderer.COLOR_SELECTION
                    ), 0.8f,
                    OverlayRenderer.COLOR_FIERY_ROSE, phase
            ));
            LinePrimitives.renderSingleBlock(ctx, corner, -0.05f, OverlayRenderer.COLOR_FIERY_ROSE, 1);
            TextRenderer.render(corner, OverlayRenderer.COLOR_FIERY_ROSE);
        } else {
            LinePrimitives.renderCorners(ctx, corners, OverlayRenderer.COLOR_BOUNDING_BOX, 1);
            for (int idx = 0; idx < corners.length; idx++) {
                corner = corners[idx];
                if (hoveredCorner != null && hoveredCorner.idx == idx) {
                    OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                            corner, ctx.camToWorldSpace, 0.1f,
                            OverlayRenderer.COLOR_FIERY_ROSE, 0.5f,
                            OverlayRenderer.COLOR_FIERY_ROSE, 0.8f
                    ));
                    LinePrimitives.renderSingleBlock(ctx, corner, 0.1f, OverlayRenderer.COLOR_FIERY_ROSE, 1);
                    TextRenderer.render(corner, OverlayRenderer.COLOR_FIERY_ROSE);
                } else {
                    OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                            corner, ctx.camToWorldSpace, 0.1f,
                            ColorHelper.blendColors(
                                    phase,
                                    OverlayRenderer.COLOR_BOUNDING_BOX,
                                    OverlayRenderer.COLOR_SELECTION
                            ), 0.8f,
                            OverlayRenderer.COLOR_SELECTION, phase
                    ));
                    LinePrimitives.renderSingleBlock(ctx,
                            corner, 0.1f,
                            ColorHelper.blendColors(
                                    phase,
                                    OverlayRenderer.COLOR_BOUNDING_BOX,
                                    OverlayRenderer.COLOR_SELECTION
                            ),
                            1
                    );
                }
            }
        }
    }

    public Transform getChangedTransform() {
        if (changedTransform == null) return transform;
        return changedTransform;
    }

    private void updateChangedTransform(Float3 selectedCornerPos) {
        if (selectedCorner != null && selectedCornerPos != null) {
            Float3[] corners = transform.getCornersInWorldSpace(false, toWorldMatrix);

            corners[selectedCorner.idx] = selectedCornerPos;
            Float3 newSize = corners[selectedCorner.idx].sub(corners[selectedCorner.opposite().idx]);
            Float3 newCenter = corners[selectedCorner.opposite().idx].add(newSize.div(2));
            newSize = newSize.unRotate(transform.rotation).abs();
            changedTransform = Transform.of(newCenter, newSize.abs(), transform.rotation);
        }
    }

    private Float3[] getCornersInWorldSpace() {
        if (selectedCorner != null && cursorPos != null && changedTransform != null) {
            return changedTransform.getCornersInWorldSpace(false, toWorldMatrix);
        }
        return transform.getCornersInWorldSpace(false, toWorldMatrix);
    }

    public boolean click() {
        if (selectedCorner != null) { //deselect
            selectedCorner = null;
            writeState();
            return true;
        } else if (hoveredCorner != null) { //select hovered
            selectedCorner = hoveredCorner;
            writeState();
            return true;
        }
        return false;
    }

    public boolean cursorTick(Float3 mousePos) {
        if (hasSelection()) {
            cursorPos = mousePos;
            updateChangedTransform(cursorPos);
        }

        boolean res = cursorOver(mousePos);
        return res;
    }

    private boolean cursorOver(Float3 mousePos) {
        //System.out.println("Selected:" + (selectedCorner == null ? "-1" : selectedCorner.idx));
        Float3[] corners = getCornersInWorldSpace();
        for (int idx = 0; idx < corners.length; idx++) {
            if (corners[idx].sub(mousePos).length() <= 0.5) {
                hoveredCorner = Bounds.Interpolate.CORNERS_AND_CENTER[idx];
                return true;
            }
        }

        hoveredCorner = null;
        return false;
    }

    private static final String SELECTED_CORNER_STATE = "s";
    private CompoundTag dataTag;

    @ApiStatus.Internal
    public void setDataTag(CompoundTag c) {
        dataTag = c;
    }

    public void writeState() {
        if (dataTag == null) return;
        dataTag.putByte(SELECTED_CORNER_STATE, (byte) (selectedCorner == null ? -1 : selectedCorner.idx));
    }

    public void readState() {
        if (dataTag == null) return;

        if (dataTag.contains(SELECTED_CORNER_STATE)) {
            byte idx = dataTag.getByte(SELECTED_CORNER_STATE);
            if (idx < 0) selectedCorner = null;
            else selectedCorner = Bounds.Interpolate.CORNERS_AND_CENTER[idx];
        }
    }

    public boolean hasSelection() {
        return selectedCorner != null;
    }
}
