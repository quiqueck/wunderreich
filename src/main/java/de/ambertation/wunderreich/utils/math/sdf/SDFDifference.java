package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;

public class SDFDifference extends SDFBinaryOperation {
    public static final Codec<SDFDifference> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf_a").forGetter(b -> b.a),
                    SDF.CODEC.fieldOf("sdf_b").forGetter(b -> b.b)
            )
            .apply(instance, SDFDifference::new)
    );

    public static final KeyDispatchDataCodec<SDFDifference> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public SDFDifference(SDF a, SDF b) {
        super(a, b);
    }

    @Override
    public double dist(Pos pos) {
        return Math.max(-a.dist(pos), b.dist(pos));
    }
}
