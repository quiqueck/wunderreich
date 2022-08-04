package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

public class ConstructionData {
    private static final String BOUNDING_BOX_TAG = "bb";
    private static final String SELECTED_CORNER_TAG = "sc";
    private static final int VALID_RADIUS_SQUARE = 64 * 64;
    private static final String CONSTRUCTION_DATA_TAG = "construction";

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

    public static ConstructionData getConstructionData(ItemStack itemStack) {
        if (itemStack.getItem() instanceof Ruler) {

            CompoundTag tag = itemStack.getOrCreateTag();

            if (!tag.contains(CONSTRUCTION_DATA_TAG)) {
                tag.put(CONSTRUCTION_DATA_TAG, new CompoundTag());
            }
            return new ConstructionData(tag.getCompound(CONSTRUCTION_DATA_TAG));
        }
        return null;
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
            return getBoundingBox().moveToCenter(Float3.of(ConstructionData.lastTarget));
        }

        Bounds.Interpolate oppositeCorner = selectedCorner.opposite();
        return new Bounds(getBoundingBox().get(oppositeCorner), Float3.of(ConstructionData.lastTarget));
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

    public double distToCenterSquare(Float3 pos) {
        Bounds bb = getBoundingBox();
        if (bb == null) return Double.MAX_VALUE;
        return bb.getCenter().distSquare(pos);
    }

    public boolean inReach(Float3 pos) {
        return distToCenterSquare(pos) < VALID_RADIUS_SQUARE;
    }
}
