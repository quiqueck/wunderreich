package de.ambertation.wunderreich.utils.math.sdf.shapes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

//based on https://iquilezles.org/articles/ellipsoids/
public class Ellipsoid extends BaseShape {
    public static final Codec<Ellipsoid> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Pos.CODEC.fieldOf("center").forGetter(b -> b.center),
                    Pos.CODEC.fieldOf("size").forGetter(b -> b.size)
            )
            .apply(instance, Ellipsoid::new)
    );

    public static final KeyDispatchDataCodec<Ellipsoid> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public final Pos size;

    public Ellipsoid(Pos center, Pos size) {
        super(center);
        this.size = size;
    }

    @Override
    public double dist(Pos pos) {
        pos = pos.sub(center);
        double k1 = pos.div(size).length();
        double k2 = pos.div(size.square()).length();

        return k1 * (k1 - 1.0) / k2;
    }
}

