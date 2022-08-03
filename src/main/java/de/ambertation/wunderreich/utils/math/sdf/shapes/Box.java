package de.ambertation.wunderreich.utils.math.sdf.shapes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape {
    public static final Codec<Box> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Pos.CODEC.fieldOf("center").forGetter(b -> b.center),
                    Pos.CODEC.fieldOf("size").forGetter(b -> b.size)
            )
            .apply(instance, Box::new)
    );

    public static final KeyDispatchDataCodec<Box> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public final Pos size;

    public Box(Pos center, Pos size) {
        super(center);
        this.size = size;
    }

    @Override
    public double dist(Pos pos) {
        Pos q = pos.sub(center).abs().sub(size);
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

}
