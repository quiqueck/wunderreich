package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;

public class Empty extends SDF {
    public static final Empty INSTANCE = new Empty();
    public static final Codec<Empty> DIRECT_CODEC = Codec.unit(() -> INSTANCE);
    public static final KeyDispatchDataCodec<Empty> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    @Override
    public double dist(Float3 pos) {
        return Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "Empty";
    }
}
