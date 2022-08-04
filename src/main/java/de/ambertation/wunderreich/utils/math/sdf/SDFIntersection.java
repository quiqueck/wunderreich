package de.ambertation.wunderreich.utils.math.sdf;

import de.ambertation.wunderreich.utils.math.Float3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

public class SDFIntersection extends SDFBinaryOperation {
    public static final Codec<SDFIntersection> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf_a").forGetter(b -> b.getFirst()),
                    SDF.CODEC.fieldOf("sdf_b").forGetter(b -> b.getSecond())
            )
            .apply(instance, SDFIntersection::new)
    );

    public static final KeyDispatchDataCodec<SDFIntersection> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public SDFIntersection(SDF a, SDF b) {
        super(a, b);
    }

    @Override
    public double dist(Float3 pos) {
        return Math.max(getFirst().dist(pos), getSecond().dist(pos));
    }

    @Override
    public String toString() {
        return "(" + getFirst() + " & " + getSecond() + ")";
    }
}
