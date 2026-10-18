package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.noise.OpenSimplex2;
import de.ambertation.wunderreich.utils.RandomList;

import net.minecraft.core.BlockPos;

/**
 * The block pattern the diamond builder's trowel places: loose patches of each block, without the
 * rings and stripes of {@link TrowelPattern}.
 * <p>
 * Cellular (Voronoi) noise: space is cut into cells around jittered feature points, and every block
 * takes the hotbar entry of the cell whose feature point is nearest. Each cell picks its entry from a
 * seeded hash of its coordinates, which is uniform, so {@link RandomList} weighs the stack counts
 * exactly as it does for random picks, and neighbouring cells are unrelated - there is no order in
 * which the block types follow each other. The block position is warped a little by OpenSimplex
 * first, so cell borders are not straight lines.
 * <p>
 * Neighbouring cells pick the same entry with probability {@code sum(share²)}, and then merge into
 * one patch. With nine equal entries that rarely happens, with two it happens every other time, so
 * the cells get smaller the more likely a merge is. That keeps the patches at about six blocks for
 * three to nine entries (two entries: about twelve, as two types always form winding, connected
 * areas).
 * <p>
 * The seed is derived from the world seed, so the pattern is the same after a restart. A sample
 * looks at the 27 cells around the position and three noise values - nothing is cached.
 */
public final class CellularTrowelPattern {
    // Mixed into the world seed so the pattern does not line up with world generation noise or with
    // the wooden trowel's pattern.
    private static final long SALT = 0x43454C4C53545257L;
    // cell spacing in blocks: BASE_SPACING * (1 - sum(share²))^SPACING_EXPONENT, at least MIN_SPACING
    private static final double BASE_SPACING = 3.2;
    private static final double SPACING_EXPONENT = 1.5;
    // below this, cells are about one block and the pattern turns into random picks
    private static final double MIN_SPACING = 1.4;
    // how far (in blocks) and how smoothly the position is pushed around before it is sorted into a cell
    private static final double WARP = 0.8;
    private static final double WARP_FREQUENCY = 0.12;

    private final long seed;

    private CellularTrowelPattern(long seed) {
        this.seed = seed;
    }

    public static CellularTrowelPattern forWorldSeed(long worldSeed) {
        return new CellularTrowelPattern(worldSeed ^ SALT);
    }

    /** The cell spacing in blocks for a hotbar with these weights. */
    public static double spacing(RandomList<?> list) {
        float total = 0;
        for (RandomList.Entry<?> e : list) total += e.weight;
        if (total <= 0) return MIN_SPACING;
        double same = 0;
        for (RandomList.Entry<?> e : list) {
            final double share = e.weight / total;
            same += share * share;
        }
        return Math.max(MIN_SPACING, BASE_SPACING * Math.pow(1 - same, SPACING_EXPONENT));
    }

    /**
     * The value of the cell that {@code pos} belongs to, uniform in [0, 1). Every block in a cell
     * gets the same value.
     */
    public float sample(BlockPos pos, double spacing) {
        final double px = pos.getX() + 0.5, py = pos.getY() + 0.5, pz = pos.getZ() + 0.5;
        final double nx = px * WARP_FREQUENCY, ny = py * WARP_FREQUENCY, nz = pz * WARP_FREQUENCY;
        final double cx = (px + WARP * OpenSimplex2.noise3_ImproveXZ(seed + 1, nx, ny, nz)) / spacing;
        final double cy = (py + WARP * OpenSimplex2.noise3_ImproveXZ(seed + 2, nx, ny, nz)) / spacing;
        final double cz = (pz + WARP * OpenSimplex2.noise3_ImproveXZ(seed + 3, nx, ny, nz)) / spacing;
        final int ix = (int) Math.floor(cx), iy = (int) Math.floor(cy), iz = (int) Math.floor(cz);

        // every feature point lies inside its own cell, so the nearest one is in a neighbouring cell
        double best = Double.MAX_VALUE;
        long bestHash = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    final long h = hash(ix + dx, iy + dy, iz + dz);
                    final double fx = ix + dx + unit21(h) - cx;
                    final double fy = iy + dy + unit21(h >>> 21) - cy;
                    final double fz = iz + dz + unit21(h >>> 42) - cz;
                    final double d = fx * fx + fy * fy + fz * fz;
                    if (d < best) {
                        best = d;
                        bestHash = h;
                    }
                }
            }
        }
        // the feature point used 63 bits of the hash; the cell's value comes from a fresh mix
        return (mix(bestHash + 0x632BE59BD9B4E019L) >>> 40) / (float) (1 << 24);
    }

    /** The weighted entry the pattern picks for {@code pos}. */
    public <T> T pick(RandomList<T> list, BlockPos pos) {
        final double spacing = spacing(list);
        return list.getRandom(() -> sample(pos, spacing));
    }

    private long hash(int x, int y, int z) {
        return mix(seed ^ x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L);
    }

    // SplitMix64's finaliser
    private static long mix(long h) {
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private static double unit21(long bits) {
        return (bits & 0x1FFFFF) / (double) (1 << 21);
    }
}
