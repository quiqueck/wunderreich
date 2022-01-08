package de.ambertation.wunderreich.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class EirParticle extends PortalParticle {
    protected EirParticle(ClientLevel clientLevel, double x, double y, double z, double deltaX, double deltaY, double deltaZ) {
        super(clientLevel, x, y, z, deltaX, deltaY, deltaZ);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.3F);

        float intensity = this.random.nextFloat() * 0.4F + 0.6F;
        this.rCol = intensity * 0.92F;
        this.gCol = intensity * 0.9F;
        this.bCol = intensity;

        this.lifetime = (int) (Math.random() * 30.0D) + 40;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double deltaX, double deltaY, double deltaZ) {
            EirParticle portalParticle = new EirParticle(clientLevel, x, y, z, deltaX, deltaY, deltaZ);
            portalParticle.pickSprite(this.sprite);
            return portalParticle;
        }
    }
}

