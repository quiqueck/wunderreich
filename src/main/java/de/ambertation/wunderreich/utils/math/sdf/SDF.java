package de.ambertation.wunderreich.utils.math.sdf;

import com.mojang.serialization.Codec;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.math.Pos;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Box;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Ellipsoid;
import de.ambertation.wunderreich.utils.math.sdf.shapes.Sphere;

import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

public abstract class SDF {
    public abstract double dist(Pos pos);
    public abstract KeyDispatchDataCodec<? extends SDF> codec();


    public static final MappedRegistry<Codec<? extends SDF>> SDF_REGISTRY = FabricRegistryBuilder
            .<Codec<? extends SDF>>createSimple(null, Wunderreich.ID("sdf"))
            .attribute(RegistryAttribute.MODDED)
            .buildAndRegister();

    public static final Codec<SDF> CODEC = SDF_REGISTRY.byNameCodec()
                                                       .dispatch((sdf) -> sdf.codec().codec(), Function.identity());

    static Codec<? extends SDF> bootstrap(Registry<Codec<? extends SDF>> registry) {
        register(registry, "union", SDFUnion.CODEC);
        register(registry, "intersect", SDFIntersection.CODEC);
        register(registry, "dif", SDFDifference.CODEC);
        register(registry, "invert", SDFInvert.CODEC);

        register(registry, "box", Box.CODEC);
        register(registry, "ellipsoid", Ellipsoid.CODEC);
        return register(registry, "sphere", Sphere.CODEC);
    }

    static Codec<? extends SDF> register(
            Registry<Codec<? extends SDF>> registry,
            String name,
            KeyDispatchDataCodec<? extends SDF> codec
    ) {
        return Registry.register(registry, Wunderreich.ID(name), codec.codec());
    }

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        bootstrap(SDF_REGISTRY);
    }
}
