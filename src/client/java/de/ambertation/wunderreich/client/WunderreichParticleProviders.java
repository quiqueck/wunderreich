package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.particles.EirParticle;
import de.ambertation.wunderreich.particles.ImprintParticle;
import de.ambertation.wunderreich.registries.WunderreichParticles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public class WunderreichParticleProviders {
    public static void register() {
        ParticleProviderRegistry.getInstance().register(WunderreichParticles.EIR_PARTICLES, EirParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(WunderreichParticles.IMPRINT_PARTICLES, ImprintParticle.Provider::new);
    }
}
