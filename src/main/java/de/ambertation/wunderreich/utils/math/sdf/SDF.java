package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Box;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Ellipsoid;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Sphere;

import java.util.function.Function;

public abstract class SDF {
    public abstract double dist(Pos pos);
    public abstract KeyDispatchDataCodec<? extends SDF> codec();

    public static final ResourceKey<Registry<Codec<? extends SDF>>> SDF_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Wunderreich.ID("sdf"));
    public static final Registry<Codec<? extends SDF>> SDF_REGISTRY = Registry.registerSimple(
            SDF_REGISTRY_KEY,
            SDF::bootstrap
    );

    public static final Codec<SDF> CODEC = SDF_REGISTRY.byNameCodec()
                                                       .dispatch((sdf) -> sdf.codec().codec(), Function.identity());

    static Codec<? extends SDF> bootstrap(Registry<Codec<? extends SDF>> registry) {
        register("union", SDFUnion.CODEC);
        register("intersect", SDFIntersection.CODEC);
        register("dif", SDFDifference.CODEC);
        register("invert", SDFInvert.CODEC);

        register("box", Box.CODEC);
        register("ellipsoid", Ellipsoid.CODEC);
        return register("sphere", Sphere.CODEC);
    }

    static Codec<? extends SDF> register(String name, KeyDispatchDataCodec<? extends SDF> codec) {
        return Registry.register(SDF_REGISTRY, Wunderreich.ID(name), codec.codec());
    }

}
