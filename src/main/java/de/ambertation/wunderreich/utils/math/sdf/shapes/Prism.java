package de.ambertation.wunderreich.utils.math.sdf.shapes;

import de.ambertation.wunderreich.utils.math.Float2;
import de.ambertation.wunderreich.utils.math.Float3;
import de.ambertation.wunderreich.utils.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Prism extends BaseShape {
    public static final Codec<Prism> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").forGetter(b -> b.getCenter()),
                    Codec.DOUBLE.fieldOf("sx").forGetter(b -> b.size.x),
                    Codec.DOUBLE.fieldOf("sy").forGetter(b -> b.size.y)
            )
            .apply(instance, Prism::new)
    );

    public static final KeyDispatchDataCodec<Prism> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    private Float2 size;

    public Prism(Float3 center, double sx, double sy) {
        super(center);
        this.size = Float2.of(sx, sy);
    }

    @Override
    public double dist(Float3 p) {
        Float3 q = p.sub(getCenter()).abs();
        return Math.max(q.z - size.y, Math.max(q.x * 0.866025 + p.y * 0.5, -p.y) - size.x * 0.5);
    }

    public Float2 getSize() {
        return size;
    }

    public void setSize(Float2 size) {
        this.size = size;
        this.emitChangeEvent();
    }
}
