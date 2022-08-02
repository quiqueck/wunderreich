package de.ambertation.wunderreich.utils.sdf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class Bounds {
    public final Pos min;
    public final Pos max;

    public Bounds(BoundingBox box) {
        this(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    public Bounds(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public Bounds(Pos min, Pos max) {
        this(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public Bounds(double lx, double ly, double lz, double hx, double hy, double hz) {
        this.min = new Pos(Math.min(lx, hx), Math.min(ly, hy), Math.min(lz, hz));
        this.max = new Pos(Math.max(lx, hx), Math.max(ly, hy), Math.max(lz, hz));
    }

    public Pos getSize() {
        return new Pos(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
    }

    public boolean isInside(Pos p) {
        return p.x >= this.min.x && p.x <= this.max.x && p.z >= this.min.z && p.z <= this.max.z && p.y >= this.min.y && p.y <= this.max.y;
    }

    public boolean isInside(BlockPos p) {
        return p.getX() >= this.min.x && p.getX() <= this.max.x && p.getZ() >= this.min.z && p.getZ() <= this.max.z && p.getY() >= this.min.y && p.getY() <= this.max.y;
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

    public Bounds encapsulate(Pos p) {
        return new Bounds(
                Math.min(this.min.x, p.x),
                Math.min(this.min.y, p.y),
                Math.min(this.min.z, p.z),
                Math.max(this.max.x, p.x),
                Math.max(this.max.y, p.y),
                Math.max(this.max.z, p.z)
        );
    }

    public Pos getCenter() {
        return new Pos(
                this.min.x + (this.max.x - this.min.x) / 2,
                this.min.y + (this.max.y - this.min.y) / 2,
                this.min.z + (this.max.z - this.min.z) / 2
        );
    }

    public double minExtension() {
        return Math.min(Math.min(max.x - min.x, max.y - min.y), max.z - min.z);
    }

    public double maxExtension() {
        return Math.max(Math.max(max.x - min.x, max.y - min.y), max.z - min.z);
    }

    public Sphere innerSphere() {
        return new Sphere(getCenter(), minExtension() / 2);
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
