package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.noise.OpenSimplex2;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;

/**
 * The block pattern the iron builder's trowel places: bands along a noise field, so the block types
 * always follow each other in hotbar order and bigger areas show stripes and rings, like the contour
 * lines of a map (the diamond trowel's {@link CellularTrowelPattern} places loose patches instead).
 * The noise seed is derived from the world
 * seed, so the same position in the same world always gets the same block from the same hotbar,
 * also after a server restart.
 * <p>
 * The noise frequency is divided by the number of hotbar entries: {@link RandomList} cuts the noise
 * range into {@code entries * CYCLES} bands, so a fixed frequency would give big blotches for two
 * block types and single-block speckles for nine. Scaled like this, the same-block patches are a
 * few blocks across (about 5-7 blocks on a wall, 7-11 on a floor) for any number of entries.
 * <p>
 * {@link OpenSimplex2} is stateless (the seed is a plain argument), so an instance is just the
 * derived seed and is cheap to create; there is nothing to cache.
 */
public final class TrowelPattern {
    // Mixed into the world seed so the trowel pattern does not line up with world generation noise
    // that uses the raw seed.
    private static final long SALT = 0x57524F57454C4C31L;
    // noise frequency times the number of hotbar entries (0.06 / 0.08 for four block types)
    private static final double HORIZONTAL_FREQUENCY = 0.24;
    private static final double VERTICAL_FREQUENCY = 0.32;

    private final long seed;

    private TrowelPattern(long seed) {
        this.seed = seed;
    }

    public static TrowelPattern forWorldSeed(long worldSeed) {
        return new TrowelPattern(worldSeed ^ SALT);
    }

    /** The pattern value at {@code pos} for a hotbar with {@code entries} block entries, in [0, 1]. */
    public float sample(BlockPos pos, int entries) {
        final double scale = 1.0 / Math.max(1, entries);
        return (1 + OpenSimplex2.noise3_ImproveXZ(
                seed,
                pos.getX() * HORIZONTAL_FREQUENCY * scale,
                pos.getY() * VERTICAL_FREQUENCY * scale,
                pos.getZ() * HORIZONTAL_FREQUENCY * scale
        )) / 2;
    }

    /** The weighted entry the pattern picks for {@code pos}. */
    public <T> T pick(RandomList<T> list, BlockPos pos) {
        return list.getRandom(() -> sample(pos, list.size()));
    }
}
