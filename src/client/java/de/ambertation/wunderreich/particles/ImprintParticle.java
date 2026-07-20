package de.ambertation.wunderreich.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlyTowardsPositionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ImprintParticle extends FlyTowardsPositionParticle {
    ImprintParticle(
            ClientLevel clientLevel,
            double x,
            double y,
            double z,
            double deltaX,
            double deltaY,
            double deltaZ,
            TextureAtlasSprite sprite
    ) {
        super(clientLevel, x, y, z, deltaX, deltaY, deltaZ, sprite);

        float intensity = this.random.nextFloat() * 0.4F + 0.6F;
        this.rCol = intensity * 0.36F;
        this.gCol = intensity * 0.23F;
        this.bCol = intensity * 0.6F;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(
                SimpleParticleType simpleParticleType,
                ClientLevel clientLevel,
                double x,
                double y,
                double z,
                double deltaX,
                double deltaY,
                double deltaZ,
                RandomSource randomSource
        ) {
            return new ImprintParticle(clientLevel, x, y, z, deltaX, deltaY, deltaZ, this.sprite.get(randomSource));
        }
    }
}
