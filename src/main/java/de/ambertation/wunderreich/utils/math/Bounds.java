package de.ambertation.wunderreich.utils.math;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import de.ambertation.wunderreich.utils.math.sdf.shapes.Box;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Ellipsoid;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Sphere;

public class Bounds {
    public static class Interpolate {
        public static final Interpolate MIN_MIN_MIN = new Interpolate((byte) 0, 0, 0, 0);
        public static final Interpolate MIN_MIN_MAX = new Interpolate((byte) 1, 0, 0, 1);
        public static final Interpolate MIN_MAX_MIN = new Interpolate((byte) 2, 0, 1, 0);
        public static final Interpolate MIN_MAX_MAX = new Interpolate((byte) 3, 0, 1, 1);
        public static final Interpolate MAX_MAX_MAX = new Interpolate((byte) 4, 1, 1, 1);
        public static final Interpolate MAX_MAX_MIN = new Interpolate((byte) 5, 1, 1, 0);
        public static final Interpolate MAX_MIN_MAX = new Interpolate((byte) 6, 1, 0, 1);
        public static final Interpolate MAX_MIN_MIN = new Interpolate((byte) 7, 1, 0, 0);
        public static final Interpolate[] CORNERS = {
                MIN_MIN_MIN, MIN_MIN_MAX, MIN_MAX_MIN, MIN_MAX_MAX,
                MAX_MAX_MAX, MAX_MAX_MIN, MAX_MIN_MAX, MAX_MIN_MIN
        };
        public static final Interpolate CENTER = new Interpolate((byte) 8, 0.5f, 0.5f, 0.5f);
        public static final Interpolate[] CORNERS_AND_CENTER = {
                MIN_MIN_MIN, MIN_MIN_MAX, MIN_MAX_MIN, MIN_MAX_MAX,
                MAX_MAX_MAX, MAX_MAX_MIN, MAX_MIN_MAX, MAX_MIN_MIN, CENTER
        };

        private final Float3 t;
        public final Byte idx;

        private Interpolate(byte idx, float tx, float ty, float tz) {
            t = Float3.of(tx, ty, tz);
            this.idx = idx;
        }

        public Interpolate(float tx, float ty, float tz) {
            this((byte) -1, tx, ty, tz);
        }

        public Interpolate opposite() {
            if (idx >= 0 && idx < CORNERS.length) return CORNERS[(idx + 4) % CORNERS.length];
            if (idx == CENTER.idx) return CENTER;
            return new Interpolate((byte) -1, (float) (1 - t.x), (float) (1 - t.y), (float) (1 - t.z));
        }

        public Float3 lerp(Float3 min, Float3 max) {
            return lerp(t, min, max);
        }

        public Float3 blockAlignedLerp(Float3 min, Float3 max) {
            return blockAlignedLerp(t, min, max);
        }

        public static Float3 lerp(Float3 t, Float3 min, Float3 max) {
            return Float3.of(
                    lerp(t.x, min.x, max.x),
                    lerp(t.y, min.y, max.y),
                    lerp(t.z, min.z, max.z)
            );
        }

        public static Float3 blockAlignedLerp(Float3 t, Float3 min, Float3 max) {
            return Float3.blockAligned(
                    lerp(t.x, min.x, max.x),
                    lerp(t.y, min.y, max.y),
                    lerp(t.z, min.z, max.z)
            );
        }

        public static Float3 lerp(double t, Float3 min, Float3 max) {
            return Float3.of(
                    lerp(t, min.x, max.x),
                    lerp(t, min.y, max.y),
                    lerp(t, min.z, max.z)
            );
        }

        public static double lerp(double t, double min, double max) {
            return t * max + (1 - t) * min;
        }
    }

    public final Float3 min;
    public final Float3 max;

    public Bounds(BoundingBox box) {
        this(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    public Bounds(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public Bounds(Float3 min, Float3 max) {
        this(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public Bounds(double lx, double ly, double lz, double hx, double hy, double hz) {
        this.min = Float3.of(Math.min(lx, hx), Math.min(ly, hy), Math.min(lz, hz));
        this.max = Float3.of(Math.max(lx, hx), Math.max(ly, hy), Math.max(lz, hz));
    }

    public Float3 getSize() {
        return Float3.of(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
    }

    public boolean isInside(Float3 p) {
        return p.x >= this.min.x && p.x <= this.max.x && p.z >= this.min.z && p.z <= this.max.z && p.y >= this.min.y && p.y <= this.max.y;
    }

    public boolean isInside(BlockPos p) {
        return p.getX() >= this.min.x && p.getX() <= this.max.x && p.getZ() >= this.min.z && p.getZ() <= this.max.z && p.getY() >= this.min.y && p.getY() <= this.max.y;
    }

    public Bounds encapsulate(Bounds bounds) {
        return new Bounds(
                Math.min(this.min.x, bounds.min.x),
                Math.min(this.min.y, bounds.min.y),
                Math.min(this.min.z, bounds.min.z),
                Math.max(this.max.x, bounds.max.x),
                Math.max(this.max.y, bounds.max.y),
                Math.max(this.max.z, bounds.max.z)
        );
    }

    public Bounds encapsulate(BlockPos blockPos) {
        return new Bounds(
                Math.min(this.min.x, blockPos.getX()),
                Math.min(this.min.y, blockPos.getY()),
                Math.min(this.min.z, blockPos.getZ()),
                Math.max(this.max.x, blockPos.getX()),
                Math.max(this.max.y, blockPos.getY()),
                Math.max(this.max.z, blockPos.getZ())
        );
    }

    public Bounds encapsulate(Float3 p) {
        return new Bounds(
                Math.min(this.min.x, p.x),
                Math.min(this.min.y, p.y),
                Math.min(this.min.z, p.z),
                Math.max(this.max.x, p.x),
                Math.max(this.max.y, p.y),
                Math.max(this.max.z, p.z)
        );
    }

    public Float3 get(Interpolate p) {
        return p.lerp(min, max);
    }

    public Float3 getBlockAligned(Interpolate p) {
        return p.blockAlignedLerp(min, max);
    }

    public Float3 getCenter() {
        return get(Interpolate.CENTER);
    }

    public Bounds moveToCenter(Float3 newCenter) {
        newCenter = newCenter.sub(getCenter());
        return new Bounds(min.add(newCenter), max.add(newCenter));
    }

    public Interpolate isCorner(Float3 p) {
        for (Interpolate i : Interpolate.CORNERS) {
            if (getBlockAligned(i).equals(p)) return i;
        }
        return null;
    }

    public Interpolate isCornerOrCenter(Float3 p) {
        for (Interpolate i : Interpolate.CORNERS_AND_CENTER) {
            if (getBlockAligned(i).equals(p)) return i;
        }
        return null;
    }

    public double minExtension() {
        return Math.min(Math.min(max.x - min.x, max.y - min.y), max.z - min.z);
    }

    public double maxExtension() {
        return Math.max(Math.max(max.x - min.x, max.y - min.y), max.z - min.z);
    }

    public Sphere innerSphere() {
        return new Sphere(getCenter(), minExtension() / 2 + 0.5);
    }

    public Sphere outerSphere() {
        return new Sphere(getCenter(), getSize().length() / 2);
    }

    public Box innerBox() {
        return new Box(getCenter(), getSize().div(2));
    }

    public Ellipsoid innerEllipsoid() {
        return new Ellipsoid(getCenter(), getSize().div(2));
    }

    public BoundingBox toBoundingBox() {
        return new BoundingBox((int) min.x, (int) min.y, (int) min.z, (int) max.x, (int) max.y, (int) max.z);
    }

    public AABB toAABB() {
        return new AABB(
                min.x, min.y, min.z,
                max.x + 1, max.y + 1, max.z + 1
        );
    }

    public Bounds shrink(BlockPos pos) {
        if (!isInside(pos)) {
            return null;
        }
        double minX = min.x;
        double maxX = max.x;
        double minY = min.y;
        double maxY = max.z;
        double minZ = min.z;
        double maxZ = max.z;

        if (Math.abs(pos.getX() - minX) < Math.abs(maxX - pos.getX())) {
            minX = pos.getX();
        } else {
            maxX = pos.getX();
        }

        if (Math.abs(pos.getY() - minY) < Math.abs(maxY - pos.getY())) {
            minY = pos.getY();
        } else {
            maxY = pos.getY();
        }

        if (Math.abs(pos.getZ() - minZ) < Math.abs(maxZ - pos.getZ())) {
            minZ = pos.getZ();
        } else {
            maxZ = pos.getZ();
        }

        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
