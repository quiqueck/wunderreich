package de.ambertation.wunderreich.blockentities.renderer;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.blocks.Chronarium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

/**
 * Draws the two visible item slots of a {@link ChronariumBlockEntity}:
 * <ul>
 *     <li>the <b>input</b> hovering above the basin, bobbing and slowly turning about Y,</li>
 *     <li>the <b>catalyst</b> mounted flat behind the glass window in the base, like an item frame.</li>
 * </ul>
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
public class ChronariumRenderer
        implements BlockEntityRenderer<ChronariumBlockEntity, ChronariumRenderer.ChronariumRenderState> {

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

    public ChronariumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ChronariumRenderState createRenderState() {
        return new ChronariumRenderState();
    }

    @Override
    public void extractRenderState(
            ChronariumBlockEntity blockEntity,
            ChronariumRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);

        final BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState != null && blockState.hasProperty(Chronarium.FACING)
                ? blockState.getValue(Chronarium.FACING)
                : Direction.NORTH;

        final Level level = blockEntity.getLevel();
        final BlockPos pos = blockEntity.getBlockPos();

        if (level == null) {
            // Inventory/preview rendering: no level means no item models and no lighting.
            state.input.clear();
            state.catalyst.clear();
            state.spinDegrees = 0;
            state.hoverY = HOVER_Y;
            state.aboveLightCoords = state.lightCoords;
            return;
        }

        // updateForTopItem() clears the render state first, so the two instances that live on the
        // (pooled) render state can simply be refilled every frame - no per-frame allocation.
        final int seed = (int) pos.asLong();
        this.itemModelResolver.updateForTopItem(
                state.input, blockEntity.getInputStack(), ItemDisplayContext.FIXED, level, null, seed
        );
        this.itemModelResolver.updateForTopItem(
                state.catalyst, blockEntity.getCatalystStack(), ItemDisplayContext.FIXED, level, null, seed + 1
        );

        // The floating item sits above the block, so it must not inherit the (dim) light of the
        // block itself.
        state.aboveLightCoords = LightCoordsUtil.getLightCoords(level, pos.above());

        final float time = (float) (level.getGameTime() % TIME_MODULUS) + partialTick;
        // A per-position phase keeps neighbouring Chronaria from moving in lockstep.
        final float phase = phaseOf(pos);

        state.spinDegrees = (time / SPIN_PERIOD + phase) * 360.0f;
        state.hoverY = HOVER_Y
                + Mth.sin((time / BOB_PERIOD + phase) * Mth.TWO_PI) * BOB_AMPLITUDE
                + blockEntity.getProgressFraction() * PROGRESS_LIFT;
    }

    /**
     * A stable pseudo random offset in {@code [0, 1)} derived from the block position.
     */
    private static float phaseOf(BlockPos pos) {
        final int h = pos.getX() * 7 + pos.getY() * 11 + pos.getZ() * 13;
        return (h & 0xFF) / 256.0f;
    }

    @Override
    public void submit(
            ChronariumRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (!state.input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, state.hoverY, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.spinDegrees));
            poseStack.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
            state.input.submit(
                    poseStack,
                    submitNodeCollector,
                    state.aboveLightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }

        if (!state.catalyst.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5f, CATALYST_Y, 0.5f);
            // Same convention as ItemFrameRenderer: after this rotation local -Z points along the
            // block's facing, and ItemDisplayContext.FIXED turns the item to look that way.
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.facing.toYRot()));
            poseStack.translate(0.0f, 0.0f, -CATALYST_DEPTH);
            poseStack.scale(CATALYST_SCALE, CATALYST_SCALE, CATALYST_SCALE);
            state.catalyst.submit(
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ChronariumRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState input = new ItemStackRenderState();
        public final ItemStackRenderState catalyst = new ItemStackRenderState();
        public Direction facing = Direction.NORTH;
        public float spinDegrees;
        public float hoverY = HOVER_Y;
        public int aboveLightCoords;
    }
}
