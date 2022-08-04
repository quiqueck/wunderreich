package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

public class Sphere extends BaseShape {
    public static final Codec<Sphere> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").forGetter(b -> b.getCenter()),
                    Codec.FLOAT.fieldOf("radius").forGetter(b -> (float) b.radius)
            )
            .apply(instance, Sphere::new)
    );

    public static final KeyDispatchDataCodec<Sphere> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------

    private double radius;

    public Sphere(Float3 center, double radius) {
        super(center);
        this.radius = radius;
    }

    @Override
    public double dist(Float3 pos) {
        return pos.sub(getCenter()).length() - radius;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        this.emitChangeEvent();
    }
}
