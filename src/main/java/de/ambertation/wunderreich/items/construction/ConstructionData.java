package de.ambertation.wunderreich.items.construction;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.wunderreich.gui.construction.RulerContainer;
import de.ambertation.wunderreich.network.ChangedTargetBlockMessage;
import de.ambertation.wunderreich.utils.nbt.CachedNBTValue;
import de.ambertation.wunderreich.utils.nbt.NbtTagHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

public class ConstructionData {
    private static final String SDF_TAG = "sdf";
    private static final String CENTER_TAG = "c";
    private static final String SELECTED_CORNER_TAG = "sc";
    private static final String MATERIAL_TAG = "material";
    private static final String ACTIVE_SLOT_TAG = "a";
    private static final int VALID_RADIUS_SQUARE = 64 * 64;
    private static final String CONSTRUCTION_DATA_TAG = "construction";

    public final CachedNBTValue<Float3, CompoundTag> CENTER;
    public final CachedNBTValue<Bounds.Interpolate, ByteTag> SELECTED_CORNER;

    public final CachedNBTValue<SDF, Tag> SDF_DATA;
    public final CachedNBTValue<Integer, IntTag> ACTIVE_SLOT;

    public final CachedNBTValue<RulerContainer, ListTag> MATERIAL_DATA;
    @ApiStatus.Internal
    private static BlockPos lastTarget;


    public ConstructionData(CompoundTag baseTag) {

        CENTER = new CachedNBTValue<>(
                baseTag,
                CENTER_TAG,
                Float3.ZERO,
                NbtTagHelper::readPos,
                NbtTagHelper::writePos
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

        ACTIVE_SLOT = new CachedNBTValue<>(
                baseTag,
                ACTIVE_SLOT_TAG,
                0,
                IntTag::getAsInt,
                IntTag::valueOf
        );
    }

    public static BlockPos getLastTarget() {
        return lastTarget;
    }

    public static void setLastTargetOnClient(BlockPos newTarget) {
        if (lastTarget == newTarget) return;

        if (lastTarget == null || newTarget == null || lastTarget.getX() != newTarget.getX() || lastTarget.getY() != newTarget.getY() || lastTarget.getZ() != newTarget.getZ()) {
            lastTarget = newTarget;
            ChangedTargetBlockMessage.INSTANCE.send(newTarget);
        }
    }

    public static void setLastTargetOnServer(BlockPos lastTarget) {
        ConstructionData.lastTarget = lastTarget;
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

    public SDF getRootSDF() {
        SDF s = SDF_DATA.get();
        return s;
    }

    public SDF getActiveSDF() {
        SDF s = SDF_DATA.get();
        if (s == null) return null;

        s = s.getChildWithGraphIndex(ACTIVE_SLOT.get());
        return s;
    }

    public Bounds.Interpolate getSelectedCorner() {
        return SELECTED_CORNER.get();
    }

    public void setSelectedCorner(Bounds.Interpolate bb) {
        SELECTED_CORNER.set(bb);
    }

    public Bounds getBoundingBox() {
        return getBoundingBox(getRootSDF());
    }

    public Bounds getActiveBoundingBox() {
        return getBoundingBox(getActiveSDF());
    }

    public Bounds getBoundingBox(SDF sdf) {
        if (sdf != null) {
            Float3 offset = CENTER.get();
            Bounds box = sdf.getBoundingBox();
            if (offset != null) box = box.move(offset);
            return box;
        }
        return Bounds.EMPTY;
    }

    public Bounds getNewBoundsForSelectedCorner() {
        Bounds.Interpolate selectedCorner = getSelectedCorner();
        if (Objects.equals(selectedCorner.idx, Bounds.Interpolate.CENTER.idx)) {
            System.out.println("New Center:" + Float3.of(ConstructionData.getLastTarget()));
            return getBoundingBox().moveToCenter(Float3.of(ConstructionData.getLastTarget()));
        }

        Bounds.Interpolate oppositeCorner = selectedCorner.opposite();
        return Bounds.of(getBoundingBox().get(oppositeCorner), Float3.of(ConstructionData.getLastTarget()));
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
