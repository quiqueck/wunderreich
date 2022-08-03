package de.ambertation.wunderreich.items.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;

import de.ambertation.wunderreich.utils.math.Bounds;
import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import org.jetbrains.annotations.ApiStatus;

public class ConstructionData {
    private static final String BOUNDING_BOX_TAG = "bb";
    private static final String SELECTED_CORNER_TAG = "sc";
    private static final int VALID_RADIUS_SQUARE = 32 * 32;

    public final CachedNBTValue<Bounds, CompoundTag> BOUNDING_BOX;
    public final CachedNBTValue<Bounds.Interpolate, ByteTag> SELECTED_CORNER;


    @ApiStatus.Internal
    public static BlockPos lastTarget;


    public ConstructionData(CompoundTag baseTag) {
        BOUNDING_BOX = new CachedNBTValue<>(
                baseTag,
                BOUNDING_BOX_TAG,
                NbtTagHelper::readBounds,
                NbtTagHelper::writeBounds
        );
        SELECTED_CORNER = new CachedNBTValue<>(
                baseTag,
                SELECTED_CORNER_TAG,
                NbtTagHelper::readInterpolated,
                NbtTagHelper::writeInterpolated
        );
    }

    public Bounds.Interpolate getSelectedCorner() {
        return SELECTED_CORNER.get();
    }

    public void setSelectedCorner(Bounds.Interpolate bb) {
        SELECTED_CORNER.set(bb);
    }

    public Bounds getBoundingBox() {
        return BOUNDING_BOX.get();
    }

    public void setBoundingBox(Bounds bb) {
        BOUNDING_BOX.set(bb);
    }

    public Bounds getNewBoundsForSelectedCorner() {
        Bounds.Interpolate selectedCorner = getSelectedCorner();
        if (selectedCorner.idx == Bounds.Interpolate.CENTER.idx) {
            return getBoundingBox().moveToCenter(new Pos(ConstructionData.lastTarget));
        }

        Bounds.Interpolate oppositeCorner = selectedCorner.opposite();
        return new Bounds(getBoundingBox().get(oppositeCorner), new Pos(ConstructionData.lastTarget));
    }

    public Bounds addToBounds(BlockPos pos) {
        Bounds bb = getBoundingBox();
        if (bb == null) {
            bb = new Bounds(pos);
        } else {
            bb = bb.encapsulate(pos);
        }
        setBoundingBox(bb);
        return bb;
    }

    public Bounds shrink(BlockPos pos) {
        Bounds bb = getBoundingBox().shrink(pos);
        setBoundingBox(bb);
        return bb;
    }

    public double distToCenterSquare(Pos pos) {
        Bounds bb = getBoundingBox();
        if (bb == null) return Double.MAX_VALUE;
        return bb.getCenter().distSquare(pos);
    }

    public boolean inReach(Pos pos) {
        return distToCenterSquare(pos) < VALID_RADIUS_SQUARE;
    }
}
