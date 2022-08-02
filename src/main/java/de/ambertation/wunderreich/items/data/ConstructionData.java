package de.ambertation.wunderreich.items.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import de.ambertation.wunderreich.utils.sdf.Bounds;
import de.ambertation.wunderreich.utils.sdf.Pos;

import org.jetbrains.annotations.ApiStatus;

public class ConstructionData {
    private static final String BOUNDING_BOX_TAG = "bb";
    private static final int VALID_RADIUS_SQUARE = 32 * 32;
    private final CompoundTag baseTag;
    private Bounds cacheBoundingBox;

    @ApiStatus.Internal
    public static BlockPos lastTarget;


    public ConstructionData(CompoundTag baseTag) {
        this.baseTag = baseTag;
    }

    public static BlockPos readBlockPos(CompoundTag tag) {
        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return null;
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public static CompoundTag writeBlockPos(BlockPos pos) {
        CompoundTag bp = new CompoundTag();
        bp.putInt("x", pos.getX());
        bp.putInt("y", pos.getY());
        bp.putInt("z", pos.getZ());
        return bp;
    }

    public static BoundingBox readBoundingBox(CompoundTag tag) {
        if (!tag.contains("lx") || !tag.contains("ly") || !tag.contains("lz") || !tag.contains("hx") || !tag.contains(
                "hy") || !tag.contains("hz")) return null;
        return new BoundingBox(
                tag.getInt("lx"),
                tag.getInt("ly"),
                tag.getInt("lz"),
                tag.getInt("hx"),
                tag.getInt("hy"),
                tag.getInt("hz")
        );
    }

    public static CompoundTag writeBoundingBox(BoundingBox bb) {
        CompoundTag bp = new CompoundTag();
        bp.putInt("lx", bb.minX());
        bp.putInt("ly", bb.minY());
        bp.putInt("lz", bb.minZ());
        bp.putInt("hx", bb.maxX());
        bp.putInt("hy", bb.maxY());
        bp.putInt("hz", bb.maxZ());
        return bp;
    }

    public static Bounds readBounds(CompoundTag tag) {
        if (!tag.contains("lx") || !tag.contains("ly") || !tag.contains("lz") || !tag.contains("hx") || !tag.contains(
                "hy") || !tag.contains("hz")) return null;
        return new Bounds(
                tag.getFloat("lx"),
                tag.getFloat("ly"),
                tag.getFloat("lz"),
                tag.getFloat("hx"),
                tag.getFloat("hy"),
                tag.getFloat("hz")
        );
    }

    public static CompoundTag writeBounds(Bounds bb) {
        CompoundTag bp = new CompoundTag();
        bp.putFloat("lx", (float) bb.min.x);
        bp.putFloat("ly", (float) bb.min.y);
        bp.putFloat("lz", (float) bb.min.z);
        bp.putFloat("hx", (float) bb.max.x);
        bp.putFloat("hy", (float) bb.max.y);
        bp.putFloat("hz", (float) bb.max.z);
        return bp;
    }

    public Bounds getBoundingBox() {
        if (cacheBoundingBox != null) return cacheBoundingBox;

        if (!baseTag.contains(BOUNDING_BOX_TAG)) {
            return null;
        } else {
            cacheBoundingBox = readBounds(baseTag.getCompound(BOUNDING_BOX_TAG));
            return cacheBoundingBox;
        }
    }

    public void setBoundingBox(Bounds bb) {
        cacheBoundingBox = bb;
        if (bb == null) {
            if (baseTag.contains(BOUNDING_BOX_TAG)) {
                baseTag.remove(BOUNDING_BOX_TAG);
            }
        } else {
            baseTag.put(BOUNDING_BOX_TAG, writeBounds(bb));
        }
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

    public static double distSquare(BlockPos a, BlockPos b) {
        return Math.pow(a.getX() - b.getX(), 2) +
                Math.pow(a.getY() - b.getY(), 2) +
                Math.pow(a.getZ() - b.getZ(), 2);
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
