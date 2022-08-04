package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

//based on https://iquilezles.org/articles/ellipsoids/
public class Ellipsoid extends BaseShape {
    public static final Codec<Ellipsoid> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").forGetter(b -> b.getCenter()),
                    Float3.CODEC.fieldOf("size").forGetter(b -> b.size)
            )
            .apply(instance, Ellipsoid::new)
    );

    public static final KeyDispatchDataCodec<Ellipsoid> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    private Float3 size;

    public Ellipsoid(Float3 center, Float3 size) {
        super(center);
        this.size = size;
    }

    @Override
    public double dist(Float3 pos) {
        pos = pos.sub(getCenter());
        double k1 = pos.div(size).length();
        double k2 = pos.div(size.square()).length();

        return k1 * (k1 - 1.0) / k2;
    }

    public Float3 getSize() {
        return size;
    }

    public void setSize(Float3 size) {
        this.size = size;
        this.emitChangeEvent();
    }
}

