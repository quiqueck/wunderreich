package de.ambertation.wunderreich.utils.sdf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Pos {
    public final double x;
    public final double y;
    public final double z;

    public Pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Pos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Pos(Vec3 pos) {
        this.x = pos.x();
        this.y = pos.y();
        this.z = pos.z();
    }

    public Pos div(Pos p) {
        return new Pos(x / p.x, y / p.y, z / p.z);
    }

    public Pos mul(Pos p) {
        return new Pos(x * p.x, y * p.y, z * p.z);
    }

    public Pos square() {
        return new Pos(x * x, y * y, z * z);
    }

    public Pos add(Pos p) {
        return new Pos(x + p.x, y + p.y, z + p.z);
    }

    public Pos add(double d) {
        return new Pos(x + d, y + d, z + d);
    }

    public Pos sub(double d) {
        return new Pos(x - d, y - d, z - d);
    }

    public Pos abs() {
        return new Pos(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public Pos max(double d) {
        return new Pos(Math.max(x, d), Math.max(y, d), Math.max(z, d));
    }

    public Pos min(double d) {
        return new Pos(Math.min(x, d), Math.min(y, d), Math.min(z, d));
    }

    public Pos sub(Pos p) {
        return new Pos(x - p.x, y - p.y, z - p.z);
    }

    public Pos mul(double d) {
        return new Pos(x * d, y * d, z * d);
    }

    public Pos div(double d) {
        return new Pos(x / d, y / d, z / d);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSquare() {
        return x * x + y * y + z * z;
    }

    public Pos normalized() {
        double d = length();
        return new Pos(x / d, y / d, z / d);
    }

    public AABB toAABB() {
        return new AABB(x, y, z, x + 1, y + 1, z + 1);
    }

    public AABB toAABB(Vec3 offset) {
        return new AABB(x + offset.x, y + offset.y, z + offset.z, x + 1 + offset.x, y + 1 + offset.y, z + 1 + offset.z);
    }

    public BlockPos toBlockPos() {
        return toBlockPos(x, y, z);
    }

    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    public double distSquare(Pos b) {
        return Math.pow(x - b.x, 2) +
                Math.pow(y - b.y, 2) +
                Math.pow(z - b.z, 2);
    }

    public double distSquare(BlockPos b) {
        return Math.pow(x - b.getX(), 2) +
                Math.pow(y - b.getY(), 2) +
                Math.pow(z - b.getZ(), 2);
    }

    public static BlockPos toBlockPos(Vec3 vec) {
        return toBlockPos(vec.x, vec.y, vec.z);
    }

    public static BlockPos toBlockPos(double x, double y, double z) {
        return new BlockPos(
                (int) Math.round(x + 0.5) - 1,
                (int) Math.round(y + 0.5) - 1,
                (int) Math.round(z + 0.5) - 1
        );
    }
}
