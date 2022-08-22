package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.ui.ColorHelper;

import net.minecraft.nbt.CompoundTag;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class TransformWidget {
    @NotNull
    private Transform transform;
    private Bounds.Interpolate hoveredCorner;
    private Bounds.Interpolate selectedCorner;

    public TransformWidget(@NotNull Transform transform) {
        this.transform = transform;
    }

    @Environment(EnvType.CLIENT)
    public void render(@NotNull RenderContext ctx, float phase) {
        LinePrimitives.renderTransform(ctx, transform, OverlayRenderer.COLOR_BOUNDING_BOX, 1);
        Float3 corner;
        Float3[] corners = transform.getCornersInWorldSpace(false);

        if (selectedCorner != null) {
            corner = corners[selectedCorner.idx];
            OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                    corner, ctx.camPosWorldSpace, 0.1f,
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
            for (int idx = 0; idx < corners.length; idx++) {
                corner = corners[idx];
                if (hoveredCorner != null && hoveredCorner.idx == idx) {
                    OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                            corner, ctx.camPosWorldSpace, 0.1f,
                            OverlayRenderer.COLOR_FIERY_ROSE, 0.5f,
                            OverlayRenderer.COLOR_FIERY_ROSE, 0.8f
                    ));
                    LinePrimitives.renderSingleBlock(ctx, corner, 0.1f, OverlayRenderer.COLOR_FIERY_ROSE, 1);

                    TextRenderer.render(corner, OverlayRenderer.COLOR_FIERY_ROSE);
                } else {
                    OverlayRenderer.INSTANCE.positions.add(BlockInfo.withCamPos(
                            corner, ctx.camPosWorldSpace, 0.1f,
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

    public boolean click() {
        if (selectedCorner != null) { //deselect
            selectedCorner = null;
            writeState();
            System.out.println("Click:" + (hoveredCorner == null
                    ? "-1"
                    : hoveredCorner.idx) + ", " + (selectedCorner == null ? "-1" : selectedCorner.idx));
            return true;
        } else if (hoveredCorner != null) { //select hovered
            selectedCorner = hoveredCorner;
            writeState();
            System.out.println("Click:" + (hoveredCorner == null
                    ? "-1"
                    : hoveredCorner.idx) + ", " + (selectedCorner == null ? "-1" : selectedCorner.idx));
            return true;
        }
        System.out.println("Click:" + (hoveredCorner == null
                ? "-1"
                : hoveredCorner.idx) + ", " + (selectedCorner == null ? "-1" : selectedCorner.idx));
        return false;
    }

    public boolean cursorOver(Float3 mousePos) {
        //System.out.println("Selected:" + (selectedCorner == null ? "-1" : selectedCorner.idx));
        Float3[] corners = transform.getCornersInWorldSpace(false);
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
