package de.ambertation.wunderreich.utils.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public class Float2 {
    public static final Codec<Float2> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.FLOAT.fieldOf("x").forGetter(o -> (float) o.x),
                    Codec.FLOAT.fieldOf("y").forGetter(o -> (float) o.y)
            )
            .apply(instance, Float2::new)
    );
    public final double x;
    public final double y;

    public Float2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Float2 of(double x, double y) {
        return new Float2(x, y);
    }

    public static Float2 of(double xy) {
        return new Float2(xy, xy);
    }

    public static Float2 blockAligned(double x, double y) {
        return new Float2(Float3.toBlockPos(x), Float3.toBlockPos(y));
    }

    public Float2 div(Float2 p) {
        return new Float2(x / p.x, y / p.y);
    }

    public Float2 mul(Float2 p) {
        return new Float2(x * p.x, y * p.y);
    }

    public Float2 square() {
        return new Float2(x * x, y * y);
    }

    public Float2 add(Float2 p) {
        return new Float2(x + p.x, y + p.y);
    }

    public Float2 add(double d) {
        return new Float2(x + d, y + d);
    }

    public Float2 sub(double d) {
        return new Float2(x - d, y - d);
    }

    public Float2 abs() {
        return new Float2(Math.abs(x), Math.abs(y));
    }

    public Float2 max(double d) {
        return new Float2(Math.max(x, d), Math.max(y, d));
    }

    public Float2 min(double d) {
        return new Float2(Math.min(x, d), Math.min(y, d));
    }

    public double maxComp() {
        return Math.max(x, y);
    }

    public double minComp() {
        return Math.min(x, y);
    }

    public Float2 sub(Float2 p) {
        return new Float2(x - p.x, y - p.y);
    }

    public Float2 mul(double d) {
        return new Float2(x * d, y * d);
    }

    public Float2 div(double d) {
        return new Float2(x / d, y / d);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double lengthSquare() {
        return x * x + y * y;
    }

    public Float2 normalized() {
        double d = length();
        return new Float2(x / d, y / d);
    }

    public double distSquare(Float2 b) {
        return Math.pow(x - b.x, 2) +
                Math.pow(y - b.y, 2);
    }

    public double distSquare(BlockPos b) {
        return Math.pow(x - b.getX(), 2) +
                Math.pow(y - b.getY(), 2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Float2 pos = (Float2) o;
        return Double.compare(pos.x, x) == 0
                && Double.compare(pos.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    //-------------------------------------------- SWIVELS (@formatter:off) --------------------------------------------
    //Scalar -----------
    public double x() { return x; }
    public double y() { return y; }


    //2D -----------
    public Float2 xx() { return new Float2(x, x); }
    public Float2 xy() { return new Float2(x, y); }
    public Float2 yx() { return new Float2(y, x); }
    public Float2 yy() { return new Float2(y, y); }


    //3D -----------
    public Float3 xxx() { return Float3.of(x, x, x); }
    public Float3 xxy() { return Float3.of(x, x, y); }
    public Float3 xyx() { return Float3.of(x, y, x); }
    public Float3 xyy() { return Float3.of(x, y, y); }
    public Float3 yxx() { return Float3.of(y, x, x); }
    public Float3 yxy() { return Float3.of(y, x, y); }
    public Float3 yyx() { return Float3.of(y, y, x); }
    public Float3 yyy() { return Float3.of(y, y, y); }
    //-------------------------------------------- SWIVELS  (@formatter:on) --------------------------------------------
}