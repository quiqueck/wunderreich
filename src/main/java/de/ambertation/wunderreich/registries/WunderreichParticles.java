package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.particles.EirParticle;
import de.ambertation.wunderreich.particles.ImprintParticle;
import de.ambertation.wunderreich.particles.SimpleParticleType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider;

@Environment(EnvType.CLIENT)
public class WunderreichParticles {
    public static SimpleParticleType EIR_PARTICLES;
    public static SimpleParticleType IMPRINT_PARTICLES;

    private static SimpleParticleType register(
            String name,
            PendingParticleProvider<net.minecraft.core.particles.SimpleParticleType> constructor
    ) {
        SimpleParticleType particle = Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Wunderreich.ID(name),
                new SimpleParticleType(false)
        );
        ParticleProviderRegistry.getInstance().register(particle, constructor);

        return particle;
    }

    public static void register() {
        EIR_PARTICLES = register("eir", EirParticle.Provider::new);
        IMPRINT_PARTICLES = register("imprint", ImprintParticle.Provider::new);
    }
}
