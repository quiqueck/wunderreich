package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.wunderreich.gui.construction.RulerContainer;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

public class ConstructionData {
    private static final String SDF_TAG = "sdf";
    private static final String BOUNDING_BOX_TAG = "bb";
    private static final String SELECTED_CORNER_TAG = "sc";
    private static final String MATERIAL_TAG = "material";
    private static final int VALID_RADIUS_SQUARE = 64 * 64;
    private static final String CONSTRUCTION_DATA_TAG = "construction";

    public final CachedNBTValue<Bounds, CompoundTag> BOUNDING_BOX;
    public final CachedNBTValue<Bounds.Interpolate, ByteTag> SELECTED_CORNER;

    public final CachedNBTValue<SDF, Tag> SDF_DATA;

    public final CachedNBTValue<RulerContainer, ListTag> MATERIAL_DATA;
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
        SDF_DATA = new CachedNBTValue<>(
                baseTag,
                SDF_TAG,
                NbtTagHelper::readSDF,
                NbtTagHelper::writeSDF,
                this::sdfObjectDidChange
        );
        MATERIAL_DATA = new CachedNBTValue<>(
                baseTag,
                MATERIAL_TAG,
                (tag) -> {
                    RulerContainer rc = new RulerContainer();
                    NbtTagHelper.readContainer(tag, rc);
                    return rc;
                },
                NbtTagHelper::writeContainer
        );
    }

    public void sdfObjectDidChange(SDF old, SDF fresh) {
//        if (old == fresh) return;
//
//        if (old != null) old.removeChangeListener(this::sdfContentDidChange);
//        if (fresh != null) fresh.addChangeListener(this::sdfContentDidChange);
    }

    public static ConstructionData getConstructionData(ItemStack itemStack) {
        if (itemStack.getItem() instanceof Ruler) {
            CompoundTag tag = itemStack.getOrCreateTag();
            return getConstructionData(tag);
        }
        return null;
    }

    public static ConstructionData getConstructionData(CompoundTag tag) {
        if (tag == null) return null;

        if (!tag.contains(CONSTRUCTION_DATA_TAG)) {
            tag.put(CONSTRUCTION_DATA_TAG, new CompoundTag());
        }
        return new ConstructionData(tag.getCompound(CONSTRUCTION_DATA_TAG));
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
