package de.ambertation.wunderreich.blockentities.renderer;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.blocks.Chronarium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Draws the two visible item slots of a {@link ChronariumBlockEntity}:
 * <ul>
 *     <li>the <b>input</b> hovering above the basin, bobbing and slowly turning about Y,</li>
 *     <li>the <b>catalyst</b> mounted flat behind the glass window in the base, like an item frame.</li>
 * </ul>
 * <p>
 * Ported from the 26.2 version, which split this into {@code extractRenderState}/{@code submit}
 * against a pooled {@code BlockEntityRenderState} and used {@code ItemStackRenderState#submit} into
 * a {@code SubmitNodeCollector}. 1.21.6 predates that render-graph rewrite: block entity renderers
 * here still draw immediately in {@link #render}, straight into a {@link PoseStack}/
 * {@link MultiBufferSource}, exactly like {@code PedestalItemRenderer} elsewhere in this project's
 * sibling mods. {@link ItemModelResolver} and {@link ItemStackRenderState} themselves are unchanged
 * between the two versions - only how their result reaches the screen differs.
 *
 * <h3>Why the animation is driven by the game time</h3>
 * The block entity only pushes a block update when an ingredient changes and every 20 ticks while a
 * recipe runs, so {@link ChronariumBlockEntity#getProgress()} is up to a second stale on the client
 * and completely unusable as an animation clock. Bob and spin are therefore derived from
 * {@code level.getGameTime() + partialTick}; the (stale, but monotone) progress fraction only feeds
 * the hover height, where a step of a few hundredths of a block is invisible.
 * <p>
 * Both cycles use a period that divides the 24000 tick modulus applied to the game time, so the
 * animation stays continuous when the clock wraps.
 *
 * <h3>Geometry</h3>
 * All constants are in block space and refer to the un-rotated model ({@code facing=north}, glass
 * window on -Z).
 */
@Environment(EnvType.CLIENT)
public class ChronariumRenderer implements BlockEntityRenderer<ChronariumBlockEntity> {

    /**
     * Game time is folded into this range before it is turned into a float. Every animation period
     * below has to divide it, otherwise the animation jumps once a day.
     */
    private static final long TIME_MODULUS = 24000L;

    /** Ticks for one full turn of the floating input item (240 ticks = 12 s). */
    private static final float SPIN_PERIOD = 240.0f;
    /** Ticks for one up/down cycle of the floating input item (60 ticks = 3 s). */
    private static final float BOB_PERIOD = 60.0f;

    /**
     * Height of the hovering input item above the block origin. The item deliberately clears the
     * basin rim by a small margin so it stays visible from the side: at the top of the bob with the
     * progress lift fully applied its highest point is 0.8 + 0.025 + 0.015 + 0.21875 = 1.05875, i.e.
     * about 1px proud of the block. That is far below a hopper's lowest geometry (y=1.25), so a
     * hopper on top still cannot intersect it.
     * <p>
     * The basin floor is at y=10/16, so ~90% of the item shows; the remainder is occluded by the
     * block's solid body. The bowl was deepened from 4px to 6px specifically so this works - at the
     * original depth less than half the item was visible and it read as half-sunk, not floating.
     */
    private static final float HOVER_Y = 0.8f;
    private static final float BOB_AMPLITUDE = 0.025f;
    /** Extra lift at 100% progress - the item "matures" upwards. Never lowers it. */
    private static final float PROGRESS_LIFT = 0.015f;
    /** Matches {@link #CATALYST_SCALE}, so the two displayed items read at the same size. */
    private static final float INPUT_SCALE = 0.4375f;

    /** Height of the centre of the glass compartment, 6/16. */
    private static final float CATALYST_Y = 0.375f;
    /**
     * Distance from the block centre towards the window face. The compartment is centred on z=2/16
     * in the un-rotated model, i.e. 0.5 - 0.125 in front of the centre.
     */
    private static final float CATALYST_DEPTH = 0.375f;
    /** Roughly 7/16, so a flat item covers 7 of the 10 clear pixels behind the bezel. */
    private static final float CATALYST_SCALE = 0.4375f;

    private final ItemModelResolver itemModelResolver;
    /**
     * The renderer instance is shared by every Chronarium block entity, but only one is ever being
     * drawn at a time, so these two are safely reused frame to frame instead of allocated per call -
     * the same pattern {@code PedestalItemRenderer} uses for its single slot.
     */
    private final ItemStackRenderState inputRenderState = new ItemStackRenderState();
    private final ItemStackRenderState catalystRenderState = new ItemStackRenderState();

    public ChronariumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void render(
            ChronariumBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            Vec3 cameraPos
    ) {
        final Level level = blockEntity.getLevel();
        if (level == null) return;

        final BlockState blockState = blockEntity.getBlockState();
        final Direction facing = blockState != null && blockState.hasProperty(Chronarium.FACING)
                ? blockState.getValue(Chronarium.FACING)
                : Direction.NORTH;

        final BlockPos pos = blockEntity.getBlockPos();
        final int seed = (int) pos.asLong();

        final ItemStack input = blockEntity.getInputStack();
        if (!input.isEmpty()) {
            this.itemModelResolver.updateForTopItem(
                    this.inputRenderState, input, ItemDisplayContext.FIXED, level, null, seed
            );

            // The floating item sits above the block, so it must not inherit the (dim) light of the
            // block itself.
            final int aboveLight = LevelRenderer.getLightColor(level, pos.above());

            final float time = (float) (level.getGameTime() % TIME_MODULUS) + partialTick;
            // A per-position phase keeps neighbouring Chronaria from moving in lockstep.
            final float phase = phaseOf(pos);

            final float spinDegrees = (time / SPIN_PERIOD + phase) * 360.0f;
            final float hoverY = HOVER_Y
                    + Mth.sin((time / BOB_PERIOD + phase) * Mth.TWO_PI) * BOB_AMPLITUDE
                    + blockEntity.getProgressFraction() * PROGRESS_LIFT;

            poseStack.pushPose();
            poseStack.translate(0.5f, hoverY, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees));
            poseStack.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
            this.inputRenderState.render(poseStack, bufferSource, aboveLight, packedOverlay);
            poseStack.popPose();
        }

        final ItemStack catalyst = blockEntity.getCatalystStack();
        if (!catalyst.isEmpty()) {
            this.itemModelResolver.updateForTopItem(
                    this.catalystRenderState, catalyst, ItemDisplayContext.FIXED, level, null, seed + 1
            );

            poseStack.pushPose();
            poseStack.translate(0.5f, CATALYST_Y, 0.5f);
            // Same convention as ItemFrameRenderer: after this rotation local -Z points along the
            // block's facing, and ItemDisplayContext.FIXED turns the item to look that way.
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - facing.toYRot()));
            poseStack.translate(0.0f, 0.0f, -CATALYST_DEPTH);
            poseStack.scale(CATALYST_SCALE, CATALYST_SCALE, CATALYST_SCALE);
            this.catalystRenderState.render(poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    /**
     * A stable pseudo random offset in {@code [0, 1)} derived from the block position.
     */
    private static float phaseOf(BlockPos pos) {
        final int h = pos.getX() * 7 + pos.getY() * 11 + pos.getZ() * 13;
        return (h & 0xFF) / 256.0f;
    }
}
