package de.ambertation.wunderreich.utils.math.sdf.shapes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

public class Sphere extends BaseShape {
    public static final Codec<Sphere> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Pos.CODEC.fieldOf("center").forGetter(b -> b.center),
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

    public final double radius;

    public Sphere(Pos center, double radius) {
        super(center);
        this.radius = radius;
    }

    @Override
    public double dist(Pos pos) {
        return pos.sub(center).length() - radius;
    }
}
