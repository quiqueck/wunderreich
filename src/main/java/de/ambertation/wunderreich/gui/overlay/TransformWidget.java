package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Matrix4;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.math.sdf.interfaces.Transformable;
import de.ambertation.lib.ui.ColorHelper;

import net.minecraft.nbt.CompoundTag;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransformWidget {
    @NotNull
    private Transformable source;

    @Nullable
    private Transform changedTransform;
    @Nullable
    private Bounds.Interpolate hoveredCorner;
    @Nullable
    private Bounds.Interpolate selectedCorner;

    @NotNull
    private Float3 cursorPos = Float3.ZERO;

    public TransformWidget(@NotNull Transformable source) {
        this.source = source;
    }

    @Environment(EnvType.CLIENT)
    public void render(@NotNull RenderContext ctx, float phase) {
        Float3 corner;
        Float3[] corners = getCornersAndCenterInWorldSpace();

        if (selectedCorner != null) {
            LinePrimitives.renderCorners(ctx, corners, OverlayRenderer.COLOR_FIERY_ROSE, 1);
            Float3[] corners2 = new Float3[corners.length];
            for (int i = 0; i < corners.length; i++) {
                corners2[i] = source.getLocalTransform()
                                    .asInvertedMatrix()
                                    .mul(source.getParentTransformMatrix().inverted())
                                    .transform(corners[i]);
            }
            LinePrimitives.renderCorners(ctx, corners2, 0xFFFFFFFF, 1);
            corner = corners[selectedCorner.idx];
            TextRenderer.render(
                    cursorPos.add(0, -0.2, 0),
                    source.getLocalTransform(),
                    ColorHelper.WHITE
            );

            TextRenderer.render(
                    cursorPos.add(0, -0.4, 0),
                    changedTransform,
                    ColorHelper.WHITE
            );
            TextRenderer.render(
                    cursorPos.add(0, 0.2, 0),
                    corners[selectedCorner.opposite().idx],
                    ColorHelper.WHITE
            );
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
        if (changedTransform == null) return source.getLocalTransform();
        return changedTransform;
    }

    private void updateChangedTransform(Float3 selectedCornerPos) {
        if (selectedCorner != null && selectedCornerPos != null) {
            if (selectedCorner.idx == Bounds.Interpolate.CENTER.idx) {
                final Matrix4 toLocal = source.getLocalTransform()
                                              .asInvertedMatrix()
                                              .mul(source.getParentTransformMatrix().inverted());
                final Float3 oldCenter = toLocal.transform(source.getCornerInWorldSpace(
                        Bounds.Interpolate.CENTER,
                        false
                ));
                Float3 newCenter = toLocal.transform(selectedCornerPos);
                Float3 cc = newCenter.sub(oldCenter);
                cc = source.getLocalTransform().transform(cc);
                changedTransform = Transform.of(
                        cc,
                        source.getLocalTransform().size,
                        source.getLocalTransform().rotation
                );
            } else if (source.isOperation()) {
                final Matrix4 toLocal = source.getLocalTransform()
                                              .asInvertedMatrix()
                                              .mul(source.getParentTransformMatrix().inverted());
                final Float3 A = toLocal.transform(selectedCornerPos);
                final Float3 B = toLocal.transform(source.getCornerInWorldSpace(selectedCorner.opposite(), false));

                final Float3 oldCenter = toLocal.transform(source.getCornerInWorldSpace(
                        Bounds.Interpolate.CENTER,
                        false
                ));
                final Float3 oldA = toLocal.transform(source.getCornerInWorldSpace(selectedCorner, false));

                final Float3 newSize = A.sub(B);
                final Float3 oldSize = oldA.sub(B);

                Float3 tSize = newSize.div(oldSize);
                final Float3 newCenter = B.add(newSize.div(2));
                //final Float3 cc = source.getLocalTransform().rotation.rotate(newCenter.sub(oldCenter.mul(tSize)));
                //
                Float3 cc = newCenter.sub(oldCenter.mul(tSize));
                //cc = cc.add(source.getLocalTransform().center);
                cc = source.getLocalTransform().transform(cc);
                //tSize = source.getLocalTransform().size.mul(tSize);
                tSize = source.getLocalTransform().size.mul(tSize);
                changedTransform = Transform.of(cc, tSize, source.getLocalTransform().rotation);
            } else {
                //this version has a better numerical stability, but it may only get used
                //for geometry SDFs (non Operations)
                Float3 A = source.getParentTransformMatrix()
                                 .inverted()
                                 .transform(selectedCornerPos);

                Float3 B = source.getParentTransformMatrix()
                                 .inverted()
                                 .transform(source.getCornerInWorldSpace(selectedCorner.opposite(), false));

                Float3 newSize = A.sub(B);
                Float3 newCenter = B.add(newSize.div(2));
                newSize = newSize.unRotate(source.getLocalTransform().rotation).abs();
                changedTransform = Transform.of(newCenter, newSize, source.getLocalTransform().rotation);
            }

            source.setLocalTransform(changedTransform);
        }
    }

    private Float3[] getCornersAndCenterInWorldSpace() {
        if (selectedCorner != null && cursorPos != null && changedTransform != null) {
            return source.getCornersAndCenterInWorldSpace(false, changedTransform);
        }
        return source.getCornersAndCenterInWorldSpace(false);
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
        Float3[] corners = getCornersAndCenterInWorldSpace();
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
