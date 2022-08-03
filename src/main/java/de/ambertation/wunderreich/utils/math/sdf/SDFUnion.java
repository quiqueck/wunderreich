package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;

public class SDFUnion extends SDFBinaryOperation {
    public static final Codec<SDFUnion> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf_a").forGetter(b -> b.a),
                    SDF.CODEC.fieldOf("sdf_b").forGetter(b -> b.b)
            )
            .apply(instance, SDFUnion::new)
    );

    public static final KeyDispatchDataCodec<SDFUnion> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public SDFUnion(SDF a, SDF b) {
        super(a, b);
    }

    @Override
    public double dist(Pos pos) {
        return Math.min(a.dist(pos), b.dist(pos));
    }
}
