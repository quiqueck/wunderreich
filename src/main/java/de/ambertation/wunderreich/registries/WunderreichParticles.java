package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.particles.SimpleParticleType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Only registers the particle *types* (referenced by common Block code such as
 * {@code WunderKisteBlock}/{@code WhisperImprinter} to spawn them). The client-only rendering
 * providers are registered separately by {@code WunderreichParticleProviders}, which is the only
 * place allowed to reference the client-only particle/provider classes.
 */
public class WunderreichParticles {
    public static SimpleParticleType EIR_PARTICLES;
    public static SimpleParticleType IMPRINT_PARTICLES;

    private static SimpleParticleType register(String name) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Wunderreich.ID(name),
                new SimpleParticleType(false)
        );
    }

    public static void register() {
        EIR_PARTICLES = register("eir");
        IMPRINT_PARTICLES = register("imprint");
    }
}
