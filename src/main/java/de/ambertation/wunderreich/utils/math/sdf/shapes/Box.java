package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape {
    public static final Codec<Box> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").forGetter(b -> b.getCenter()),
                    Float3.CODEC.fieldOf("size").forGetter(b -> b.size)
            )
            .apply(instance, Box::new)
    );

    public static final KeyDispatchDataCodec<Box> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    private Float3 size;

    public Box(Float3 center, Float3 size) {
        super(center);
        this.size = size;
    }

    @Override
    public double dist(Float3 pos) {
        Float3 q = pos.sub(getCenter()).abs().sub(size);
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

    public Float3 getSize() {
        return size;
    }

    public void setSize(Float3 size) {
        this.size = size;
        this.emitChangeEvent();
    }
}
