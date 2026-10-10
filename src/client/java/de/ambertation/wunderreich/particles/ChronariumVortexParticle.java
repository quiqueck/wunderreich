package de.ambertation.wunderreich.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The amethyst mote that swirls inside a working {@code Chronarium}'s basin.
 * <p>
 * Unlike the vanilla particles this mod reuses elsewhere ({@code EirParticle} /
 * {@code ImprintParticle}), this one does not move by velocity at all: {@link #tick()} places it
 * analytically on a circle around a fixed vertical axis, so the orbit is exactly circular and never
 * drifts. The axis is whatever position the particle was spawned at - {@code Chronarium.animateTick}
 * passes the centre of the basin floor - and every mote picks its own radius, phase, angular speed
 * and lifetime from {@link #random}, so a handful of them together read as one continuous vortex.
 * <p>
 * <b>Intended look:</b> motes wink in near the basin wall just above the floor, sweep around the
 * floating input item roughly two full turns while spiralling inwards and rising, and dissolve as
 * they reach the rim. The motion should be read as <em>circular</em> first and rising second: the
 * tangential speed is an order of magnitude larger than the climb.
 * <p>
 * All of the numbers below are named on purpose - particles cannot be checked without running the
 * game, so this is meant to be tuned by editing these constants and nothing else.
 * <p>
 * Ported from the 26.2 version, which extended {@code SingleQuadParticle} and used
 * {@code LightCoordsUtil.addSmoothBlockEmission} to fold a fractional glow into the render-graph's
 * float light coordinates. Neither exists on 1.21.6: the quad particle base class here is
 * {@link TextureSheetParticle}, and the block entity light API only deals in integer 0-15
 * block/sky levels via {@link LightTexture}, so {@link #getLightColor} instead floors the packed
 * block-light component at {@link #GLOW} of full brightness - a coarser but visually equivalent
 * "never goes fully dark" glow.
 */
@Environment(EnvType.CLIENT)
public class ChronariumVortexParticle extends TextureSheetParticle {
    /**
     * Radius the mote starts at, in blocks from the vortex axis. The basin cavity spans model
     * coordinates 3..13, i.e. 0.3125 blocks from the centre to the inner wall; the max here leaves
     * about a pixel for the quad's own half width so the sprite does not poke through the wall.
     */
    private static final double RADIUS_START_MIN = 0.18;
    private static final double RADIUS_START_MAX = 0.26;
    /**
     * Radius at the end of the mote's life. Smaller than the start radius, which turns the orbit
     * into a funnel that closes over the item floating at (0.5, ~0.8, 0.5).
     */
    private static final double RADIUS_END = 0.11;
    /**
     * How far the mote climbs over its lifetime. Spawned at the basin floor (0.655) this tops out
     * just at the rim (1.0), so nothing escapes the bowl.
     */
    private static final double RISE_HEIGHT = 0.34;
    /**
     * Angular speed in radians per tick. 0.20..0.26 rad/t is one full turn every 24..31 ticks, i.e.
     * a bit over a second - fast enough to read as a vortex, slow enough not to strobe. The range is
     * what keeps the motes from locking into a rigid ring.
     */
    private static final double ANGULAR_SPEED_MIN = 0.20;
    private static final double ANGULAR_SPEED_MAX = 0.26;
    /**
     * Lifetime in ticks. 50..70 ticks at the speeds above is ~1.7..2.6 revolutions per mote.
     */
    private static final int LIFETIME_MIN = 50;
    private static final int LIFETIME_SPREAD = 21;
    /**
     * Fraction of the lifetime spent fading in / out. The rest of the life is fully opaque.
     */
    private static final float FADE_IN = 0.15f;
    private static final float FADE_OUT = 0.30f;
    /**
     * Base quad size, jittered per mote by +-30%. Deliberately small: the item in the basin is the
     * subject, the motes are the frame around it.
     */
    private static final float QUAD_SIZE = 0.055f;
    /**
     * Minimum block-light level (of 15) the mote is rendered at, regardless of the actual light in
     * the basin. The block already emits light 12 while working; this only keeps the motes from
     * going grey in a dark room.
     */
    private static final int GLOW_BLOCK_LIGHT = 7;

    /**
     * The amethyst family used by the block's textures (see {@code chronarium_tex_gen.py}), from
     * {@code mid} to {@code xhi}. Weighted towards the bright end so the motes stay visible against
     * the dark basin.
     */
    private static final float[][] PALETTE = {
            {0.4784f, 0.3569f, 0.7098f}, // AM mid  0x7A5BB5
            {0.5529f, 0.4157f, 0.8000f}, // AM lite 0x8D6ACC
            {0.6510f, 0.4706f, 0.9451f}, // AM hi   0xA678F1
            {0.6510f, 0.4706f, 0.9451f}, // AM hi   (twice: bias towards the bright end)
            {0.7843f, 0.5647f, 0.9412f}, // AM xhi  0xC890F0
    };

    private final double axisX;
    private final double baseY;
    private final double axisZ;
    private final double startRadius;
    private final double startAngle;
    private final double angularSpeed;

    protected ChronariumVortexParticle(
            ClientLevel clientLevel,
            double x,
            double y,
            double z
    ) {
        super(clientLevel, x, y, z);

        // tick() never calls move(), but keep the physics fields inert in case a mixin ever does.
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.friction = 1.0f;

        this.axisX = x;
        this.baseY = y;
        this.axisZ = z;

        this.startRadius = Mth.lerp(this.random.nextDouble(), RADIUS_START_MIN, RADIUS_START_MAX);
        this.startAngle = this.random.nextDouble() * Mth.TWO_PI;
        // Always the same sign, so every mote turns the same way and the swarm reads as one vortex.
        this.angularSpeed = Mth.lerp(this.random.nextDouble(), ANGULAR_SPEED_MIN, ANGULAR_SPEED_MAX);

        this.lifetime = LIFETIME_MIN + this.random.nextInt(LIFETIME_SPREAD);
        this.quadSize = QUAD_SIZE * (0.7f + this.random.nextFloat() * 0.6f);

        float[] color = PALETTE[this.random.nextInt(PALETTE.length)];
        this.setColor(color[0], color[1], color[2]);
        this.setAlpha(0.0f);

        // Put the mote on its orbit immediately; without this the first frame renders on the axis.
        this.setPos(
                this.axisX + Math.cos(this.startAngle) * this.startRadius,
                this.baseY,
                this.axisZ + Math.sin(this.startAngle) * this.startRadius
        );
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Translucent so the fade in/out below actually does something.
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        final int packed = super.getLightColor(partialTick);
        final int blockLight = Math.max(LightTexture.block(packed), GLOW_BLOCK_LIGHT);
        return LightTexture.pack(blockLight, LightTexture.sky(packed));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        final float t = (float) this.age / (float) this.lifetime;

        // Squared t on the radius: the mote hugs the wall for most of its life and only tightens up
        // towards the axis at the end, which looks like it is being pulled in rather than gliding.
        final double radius = Mth.lerp(t * t, this.startRadius, RADIUS_END);
        final double angle = this.startAngle + this.angularSpeed * this.age;

        this.setPos(
                this.axisX + Math.cos(angle) * radius,
                this.baseY + RISE_HEIGHT * t,
                this.axisZ + Math.sin(angle) * radius
        );

        this.setAlpha(fade(t));
    }

    private static float fade(float t) {
        if (t < FADE_IN) return t / FADE_IN;
        if (t > 1.0f - FADE_OUT) return (1.0f - t) / FADE_OUT;
        return 1.0f;
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
                double deltaZ
        ) {
            // The deltas are unused: this particle is driven by its own orbit, not by a velocity.
            ChronariumVortexParticle particle = new ChronariumVortexParticle(clientLevel, x, y, z);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
