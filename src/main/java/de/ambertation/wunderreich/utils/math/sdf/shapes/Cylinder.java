package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float2;
import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Cylinder extends BaseShape {
    public static final Codec<Cylinder> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").forGetter(b -> b.getCenter()),
                    Codec.DOUBLE.fieldOf("height").forGetter(b -> b.getHeight()),
                    Codec.DOUBLE.fieldOf("radius").forGetter(b -> b.getRadius())
            )
            .apply(instance, Cylinder::new)
    );

    public static final KeyDispatchDataCodec<Cylinder> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    private Float2 size;

    public Cylinder(Float3 center, double height, double radius) {
        super(center);
        size = Float2.of(height, radius);
    }

    public double getHeight() {
        return size.x;
    }

    public void setHeight(double h) {
        size = Float2.of(h, size.y);
        this.emitChangeEvent();
    }

    public double getRadius() {
        return size.y;
    }

    public void setRadius(double r) {
        size = Float2.of(size.x, r);
        this.emitChangeEvent();
    }

    @Override
    public double dist(Float3 p) {
        Float2 d = Float2.of(p.xz().length(), p.y).abs().sub(size);
        return Math.min(d.maxComp(), 0.0) + d.max(0.0).length();
    }

}