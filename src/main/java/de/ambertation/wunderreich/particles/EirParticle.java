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
	
	protected EirParticle(ClientLevel clientLevel, double x, double y, double z, double deltaX, double deltaY, double deltaZ) {
		super(clientLevel, x, y, z);
		this.xd = deltaX;
		this.yd = deltaY;
		this.zd = deltaZ;
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.xStart = this.x;
		this.yStart = this.y;
		this.zStart = this.z;
		
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.3F);
		
		float intensity = this.random.nextFloat() * 0.4F + 0.6F;
		this.rCol = intensity * 0.92F;
		this.gCol = intensity * 0.9F;
		this.bCol = intensity;
		
		this.lifetime = (int) (Math.random() * 30.0D) + 40;
	}
	
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
	
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox()
								.move(d, e, f));
		this.setLocationFromBoundingbox();
	}
	
	public float getQuadSize(float f) {
		float g = ((float) this.age + f) / (float) this.lifetime;
		g = 1.0F - g;
		g *= g;
		g = 1.0F - g;
		return this.quadSize * g;
	}
	
	public int getLightColor(float f) {
		final int light = super.getLightColor(f);
		float alpha = (float) this.age / (float) this.lifetime;
		alpha *= alpha;
		alpha *= alpha;
		final int b = light & 255;
		int r = light >> 16 & 255;
		r += (int) (alpha * 15.0F * 16.0F);
		if (r > 240) {
			r = 240;
		}
		
		return b | r << 16;
	}
	
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		}
		else {
			float f = (float) this.age / (float) this.lifetime;
			float g = f;
			f = -f + f * f * 2.0F;
			f = 1.0F - f;
			this.x = this.xStart + this.xd * (double) f;
			this.y = this.yStart + this.yd * (double) f + (double) (1.0F - g);
			this.z = this.zStart + this.zd * (double) f;
		}
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

