package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;

public class SDFIntersection extends SDFBinaryOperation {
    public static final Codec<SDFIntersection> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf_a").forGetter(b -> b.a),
                    SDF.CODEC.fieldOf("sdf_b").forGetter(b -> b.b)
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
    public double dist(Pos pos) {
        return Math.max(a.dist(pos), b.dist(pos));
    }
}
