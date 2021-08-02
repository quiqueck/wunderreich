package de.ambertation.wunderreich.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class EirParticle extends TextureSheetParticle {
	private final double xStart;
	private final double yStart;
	private final double zStart;
	
	protected EirParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.x = d;
		this.y = e;
		this.z = f;
		this.xStart = this.x;
		this.yStart = this.y;
		this.zStart = this.z;
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
		float j = this.random.nextFloat() * 0.4F + 0.6F;
		this.rCol = j * 0.92F;
		this.gCol = j * 0.9F;
		this.bCol = j;
		this.lifetime = (int)(Math.random() * 10.0D) + 40;
	}
	
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
	
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox().move(d, e, f));
		this.setLocationFromBoundingbox();
	}
	
	public float getQuadSize(float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		g = 1.0F - g;
		g *= g;
		g = 1.0F - g;
		return this.quadSize * g;
	}
	
	public int getLightColor(float f) {
		int i = super.getLightColor(f);
		float g = (float)this.age / (float)this.lifetime;
		g *= g;
		g *= g;
		int j = i & 255;
		int k = i >> 16 & 255;
		k += (int)(g * 15.0F * 16.0F);
		if (k > 240) {
			k = 240;
		}
		
		return j | k << 16;
	}
	
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			float f = (float)this.age / (float)this.lifetime;
			float g = f;
			f = -f + f * f * 2.0F;
			f = 1.0F - f;
			this.x = this.xStart + this.xd * (double)f;
			this.y = this.yStart + this.yd * (double)f + (double)(1.0F - g);
			this.z = this.zStart + this.zd * (double)f;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;
		
		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}
		
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			EirParticle portalParticle = new EirParticle(clientLevel, d, e, f, g, h, i);
			portalParticle.pickSprite(this.sprite);
			return portalParticle;
		}
	}
}

