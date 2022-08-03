package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;

public class SDFInvert extends SDFOperation {
    public static final Codec<SDFInvert> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf").forGetter(b -> b.a)
            )
            .apply(instance, SDFInvert::new)
    );

    public static final KeyDispatchDataCodec<SDFInvert> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    protected SDFInvert(SDF sdf) {
        super(sdf);
    }

    @Override
    public double dist(Pos pos) {
        return -a.dist(pos);
    }
}
