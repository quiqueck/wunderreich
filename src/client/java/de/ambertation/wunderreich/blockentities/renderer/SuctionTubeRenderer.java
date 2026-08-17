package de.ambertation.wunderreich.blockentities.renderer;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a {@link SuctionTubeBlockEntity}'s filter templates on the outside of the block: a row of
 * four standing on the top plate on each of the four walls, bobbing, in front of that wall's torch
 * nub, and four more under the corners of the base plate for the bottom intake.
 *
 * <h3>Why they are not in the block any more</h3>
 * They used to sit in cubbies - 3px recesses cut into each wall, lapis-lined, behind a glass hint -
 * and at that size, in that shadow, behind that glass, a player could not tell what a side was
 * filtering for without opening the menu, which is the one thing the items on the block are there
 * to save them. Now they are mounted like the picture in a picture frame: full size, against a
 * plain backdrop, out at the block boundary where nothing on the tube can get in front of them.
 * The cubbies, the mullions and the panes went with them.
 *
 * <h3>Where a slot lands</h3>
 * The menu lays a direction's four filter slots out in a row, and so does the rim: slot 0 is the
 * leftmost as seen by somebody standing in front of that wall, slot 3 the rightmost. The four
 * {@link #ROW_OFFSETS} tile the plate's 14px width around the torch nub in the middle, and
 * {@link #RIM_Y} puts them in the band above the plate - see there, it is the reason the row is
 * where it is, and the reason it is the size it is.
 * <p>
 * The bottom face has no such viewer and no row to lay out, so its four keep the reading order they
 * have in the menu - left to right, then top to bottom - mapped onto the four corners of the base
 * plate in the local frame the item ends up in after it has been turned to face down: its "top" row
 * is the one towards north and its "left" column the one towards east. Each corner has a seat drawn
 * under it on {@code suction_tube_bottom}, sized to {@link #FLOOR_SPREAD} and {@link #SCALE}.
 *
 * <h3>Why the items are squashed</h3>
 * A rim item stands in a gap one pixel deep, between the top plate's edge and the block boundary,
 * and a floor item hangs in the two pixels of ground clearance under the base. A flat template is a
 * plane and fits either happily; a block template is a cube, and {@link ItemDisplayContext#FIXED}
 * renders those at half scale the way an item frame does, which still leaves it half again as deep
 * as the gap it is standing in - deep enough to come out through the block boundary and into
 * whatever is standing next to the tube. {@link #DEPTH_SQUASH} flattens it along the axis nobody
 * can see it along: it changes nothing at all for the overwhelming majority of templates, which are
 * flat, and makes a block one read as a plaque rather than as a cube stuck to the wall.
 */
@Environment(EnvType.CLIENT)
public class SuctionTubeRenderer
        implements BlockEntityRenderer<SuctionTubeBlockEntity, SuctionTubeRenderer.SuctionTubeRenderState> {

    /** Filter slots per intake, and intakes - mirrors {@code SuctionTubeMenu.SLOTS_PER_DIRECTION}. */
    private static final int SLOTS_PER_DIRECTION = 4;
    private static final Direction[] DIRECTIONS = SuctionTubeBlockEntity.DIRECTIONS;
    private static final int SLOT_COUNT = DIRECTIONS.length * SLOTS_PER_DIRECTION;

    /**
     * Where the four items of a wall sit along its rim, as an offset from the middle of the face
     * towards the viewer's left. The plate is 14px wide and the torch nub takes the two pixels in
     * the middle of it: centres at 2.5, 5.5, 10.5 and 13.5 put two items either side of the nub and
     * leave it showing between the second and the third, which is the one thing on the rim that has
     * to stay findable. Slot 0 is the leftmost - local +X is the left hand of somebody facing the
     * wall.
     */
    private static final float[] ROW_OFFSETS = {
            5.5f / 16.0f, 2.5f / 16.0f, -2.5f / 16.0f, -5.5f / 16.0f
    };
    /**
     * Centre height of the rim row: the middle of the band between the top plate at y=13 and the
     * top of the block. A {@link #SCALE} item centred here stands on the plate's top face without
     * touching it and reaches the block boundary without crossing it.
     * <p>
     * Everything about the row follows from that band. The tube is built to be walled in by the
     * containers it pulls from, and a chest lid tops out at y=14, so anything the block shows below
     * that line is behind the first chest a player parks against that side. Above it there is only
     * as far as y=16: a template that crossed the block boundary would poke into whatever the tube
     * is pushing into, and pushing upwards is what it does.
     * <p>
     * Three pixels of band is not what the block started with. The top plate, the spout, the torch
     * nubs and the output ring were all dropped a pixel to make it, because at the two the plate
     * left over an item was smaller here than it had been in the cubbies this whole redesign was
     * meant to get it out of.
     */
    private static final float RIM_Y = 14.5f / 16.0f;
    /**
     * Distance from the block centre out to an item on a wall. The plate oversails the body to
     * 1/16 from the block boundary, which leaves a pixel of air in front of its face at 7/16; the
     * item's middle sits 0.7px into that, so the deepest a block template can reach is the boundary
     * itself and the shallowest is a fraction in front of the plate's face. Above y=13 there is
     * nothing in front of it at all - it is out over the rim, a pixel and a bit nearer the viewer
     * than the torch nub behind it.
     */
    private static final float RIM_DEPTH = 7.3f / 16.0f;

    /**
     * Half the gap between the two columns and the two rows of items under the base plate: their
     * centres sit at 4.5/16 and 11.5/16, which puts a 3px item exactly on the seat drawn for it and
     * exactly clear of the bottom nozzle's 4x4 footprint.
     */
    private static final float FLOOR_SPREAD = 3.5f / 16.0f;
    /**
     * Height of an item under the base plate. The body is held two pixels clear of the ground, so
     * the plate's underside is at 2/16 and the item hangs just below it - close enough to read as
     * fixed to the plate, far enough that a block template does not sink into it.
     */
    private static final float FLOOR_Y = 1.75f / 16.0f;

    /**
     * 3px: the whole of the air between the top plate and the top of the block, and the size of a
     * seat drawn on {@code suction_tube_bottom} for the four under the base. The rim is the tighter
     * of the two and the one that decides this - see {@link #RIM_Y}.
     */
    private static final float SCALE = 3.0f / 16.0f;
    /** See the class javadoc. */
    private static final float DEPTH_SQUASH = 0.35f;

    /**
     * Game time is folded into this range before it is turned into a float, so every animation
     * period below has to divide it or the bob jumps once a day.
     */
    private static final long TIME_MODULUS = 24000L;
    /** Ticks for one up-and-down cycle (60 ticks = 3 s). */
    private static final float BOB_PERIOD = 60.0f;
    /**
     * A sixth of a pixel each way. An item fills its band exactly, so the bob has nowhere to go
     * that is not through the plate under it or the block boundary over it: this is small enough
     * that either overshoot is a fraction of a pixel and lands inside geometry that hides it
     * anyway. It stays because it is the only thing moving on the block, and an item standing in
     * the air over the rim has to look like it is standing there rather than bolted on.
     */
    private static final float BOB_AMPLITUDE = 0.15f / 16.0f;
    /**
     * Phase pushed between consecutive slots so the twenty items drift rather than pulse in unison.
     * Deliberately not a neat fraction: at 1/4 the four items on a face would sit at the four
     * corners of the cycle forever and read as a rotating pattern instead of as twenty loose things.
     */
    private static final float SLOT_PHASE = 0.11f;

    /**
     * Twenty items is a lot to submit for one block, so they stop being drawn well before the
     * default 64. At 24 blocks a 3px item is about a pixel on screen at any normal FOV.
     */
    private static final int VIEW_DISTANCE = 24;

    // ------------------------------------------------------------------------- transfer flashes
    /**
     * One flash slot per {@link SuctionTubeBlockEntity#DIRECTIONS} entry, plus
     * {@link SuctionTubeBlockEntity#UP_FLASH_INDEX} for the output. Driven by
     * {@link SuctionTubeBlockEntity#getFlashStartTick}, which is itself only ever written by
     * {@link SuctionTubeBlockEntity#triggerEvent} - see there for why this rides a block-event
     * instead of the block state the steady rings use.
     */
    private static final int FLASH_COUNT = SuctionTubeBlockEntity.UP_FLASH_INDEX + 1;
    /** How long a flash takes to fade to nothing, in ticks. */
    private static final float FLASH_DURATION = 8.0f;
    private static final Identifier FLASH_TEXTURE = Wunderreich.ID("textures/block/suction_tube_torch_lit.png");
    private static final RenderType FLASH_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(FLASH_TEXTURE);
    /** Mirrors {@code SuctionTube.TORCH_Y/TORCH_NEAR/TORCH_FAR} for the four wall intakes. */
    private static final float FLASH_TORCH_Y = 14.7f / 16.0f;
    private static final float FLASH_TORCH_NEAR = 3.0f / 16.0f;
    private static final float FLASH_TORCH_FAR = 13.0f / 16.0f;
    /** Mirrors the two ring elements' band centres - see {@code suction_tube.json}. */
    private static final float FLASH_RING_UP_Y = 13.2f / 16.0f;
    private static final float FLASH_RING_DOWN_Y = 1.8f / 16.0f;
    private static final float FLASH_SIZE = 0.16f;

    private final ItemModelResolver itemModelResolver;

    public SuctionTubeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public SuctionTubeRenderState createRenderState() {
        return new SuctionTubeRenderState();
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public void extractRenderState(
            SuctionTubeBlockEntity blockEntity,
            SuctionTubeRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);

        final Level level = blockEntity.getLevel();
        if (level == null) {
            // Inventory/preview rendering: no level means no item models and no clock.
            for (ItemStackRenderState slot : state.filters) slot.clear();
            Arrays.fill(state.bob, 0.0f);
            Arrays.fill(state.flashAge, Float.MAX_VALUE);
            return;
        }

        final BlockPos pos = blockEntity.getBlockPos();
        final int seed = (int) pos.asLong();

        // The block entity only syncs when a filter changes, so there is no progress value to drive
        // an animation from - the bob comes off the game clock, with a per-position phase so two
        // tubes side by side do not move together.
        final float time = (float) (level.getGameTime() % TIME_MODULUS) + partialTick;
        final float phase = phaseOf(pos);
        for (int i = 0; i < SLOT_COUNT; i++) {
            state.bob[i] = Mth.sin((time / BOB_PERIOD + phase + i * SLOT_PHASE) * Mth.TWO_PI)
                    * BOB_AMPLITUDE;
        }

        for (int d = 0; d < DIRECTIONS.length; d++) {
            final ItemStack[] filters = blockEntity.getFilterItems(DIRECTIONS[d]);
            for (int slot = 0; slot < SLOTS_PER_DIRECTION; slot++) {
                final ItemStackRenderState target = state.filters[d * SLOTS_PER_DIRECTION + slot];
                final ItemStack stack = slot < filters.length ? filters[slot] : ItemStack.EMPTY;
                if (stack.isEmpty()) {
                    target.clear();
                } else {
                    // updateForTopItem() clears the state first, so the twenty instances that live
                    // on the (pooled) render state are simply refilled - no per-frame allocation.
                    this.itemModelResolver.updateForTopItem(
                            target, stack, ItemDisplayContext.FIXED, level, null,
                            seed + d * SLOTS_PER_DIRECTION + slot
                    );
                }
            }
        }

        final double now = level.getGameTime() + partialTick;
        for (int i = 0; i < FLASH_COUNT; i++) {
            final long start = blockEntity.getFlashStartTick(i);
            state.flashAge[i] = start == Long.MIN_VALUE ? Float.MAX_VALUE : (float) (now - start);
        }
    }

    @Override
    public void submit(
            SuctionTubeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        for (int d = 0; d < DIRECTIONS.length; d++) {
            final Direction direction = DIRECTIONS[d];
            for (int slot = 0; slot < SLOTS_PER_DIRECTION; slot++) {
                final ItemStackRenderState item = state.filters[d * SLOTS_PER_DIRECTION + slot];
                if (item.isEmpty()) continue;

                poseStack.pushPose();
                if (direction == Direction.DOWN) {
                    // Slot 0/1 are the row towards north, 0/2 the column towards east - see the
                    // class javadoc. No bob down here: "up and down" under the base plate means in
                    // and out of the plate, and there is nowhere for it to go that is not through
                    // either the plate above or the block boundary below.
                    final boolean northRow = slot < 2;
                    final float east = (slot % 2 == 0) ? FLOOR_SPREAD : -FLOOR_SPREAD;
                    poseStack.translate(
                            0.5f + east, FLOOR_Y, 0.5f + (northRow ? -FLOOR_SPREAD : FLOOR_SPREAD)
                    );
                    // -90 rather than 90: it is the one that turns the item's face (local -Z, the
                    // side ItemDisplayContext.FIXED presents) downwards rather than up.
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                } else {
                    // The bob goes on before the rotation, so it is world up-and-down on all four
                    // walls rather than up-and-down in each wall's own frame.
                    final float y = RIM_Y + state.bob[d * SLOTS_PER_DIRECTION + slot];
                    poseStack.translate(0.5f, y, 0.5f);
                    // Same convention as ItemFrameRenderer and the Chronarium's catalyst: after
                    // this rotation local -Z points out through the wall and local +X is the left
                    // hand of somebody looking at it.
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - direction.toYRot()));
                    poseStack.translate(ROW_OFFSETS[slot], 0.0f, -RIM_DEPTH);
                }
                poseStack.scale(SCALE, SCALE, SCALE * DEPTH_SQUASH);
                item.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0
                );
                poseStack.popPose();
            }
        }

        for (int i = 0; i < FLASH_COUNT; i++) {
            final float age = state.flashAge[i];
            if (age < 0.0f || age >= FLASH_DURATION) continue;

            final float brightness = 1.0f - age / FLASH_DURATION;
            final int alpha = Mth.clamp((int) (brightness * 255.0f), 0, 255);
            final Vec3 center = flashCenter(i);

            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            poseStack.mulPose(camera.orientation);
            poseStack.scale(FLASH_SIZE, FLASH_SIZE, FLASH_SIZE);
            submitNodeCollector.submitCustomGeometry(poseStack, FLASH_RENDER_TYPE, (pose, buffer) -> {
                flashVertex(buffer, pose, -0.5f, -0.5f, alpha, 0.0f, 1.0f);
                flashVertex(buffer, pose, 0.5f, -0.5f, alpha, 1.0f, 1.0f);
                flashVertex(buffer, pose, 0.5f, 0.5f, alpha, 1.0f, 0.0f);
                flashVertex(buffer, pose, -0.5f, 0.5f, alpha, 0.0f, 0.0f);
            });
            poseStack.popPose();
        }
    }

    /**
     * One camera-facing quad of the flash texture, full-bright (the render type is emissive, so
     * light coords do not matter) and fading via alpha alone.
     */
    private static void flashVertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, int alpha, float u, float v
    ) {
        buffer.addVertex(pose, x, y, 0.0f)
              .setColor(255, 255, 255, alpha)
              .setUv(u, v)
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(0xF000F0)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    /**
     * Local block-space centre of the nozzle for {@link SuctionTubeBlockEntity#DIRECTIONS}
     * index {@code flashIndex}, or the output ring for
     * {@link SuctionTubeBlockEntity#UP_FLASH_INDEX}. Mirrors the positions baked into
     * {@code suction_tube.json} for the torch nubs and the two rings, but does not read them -
     * see {@link #FLASH_TORCH_Y} and friends.
     */
    private static Vec3 flashCenter(int flashIndex) {
        if (flashIndex == SuctionTubeBlockEntity.UP_FLASH_INDEX) {
            return new Vec3(0.5, FLASH_RING_UP_Y, 0.5);
        }
        final Direction direction = DIRECTIONS[flashIndex];
        if (direction == Direction.DOWN) {
            return new Vec3(0.5, FLASH_RING_DOWN_Y, 0.5);
        }
        final double offset = direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? FLASH_TORCH_NEAR
                : FLASH_TORCH_FAR;
        final double x = direction.getAxis() == Direction.Axis.X ? offset : 0.5;
        final double z = direction.getAxis() == Direction.Axis.Z ? offset : 0.5;
        return new Vec3(x, FLASH_TORCH_Y, z);
    }

    /** A stable pseudo random offset in {@code [0, 1)} derived from the block position. */
    private static float phaseOf(BlockPos pos) {
        final int h = pos.getX() * 7 + pos.getY() * 11 + pos.getZ() * 13;
        return (h & 0xFF) / 256.0f;
    }

    @Environment(EnvType.CLIENT)
    public static class SuctionTubeRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState[] filters = new ItemStackRenderState[SLOT_COUNT];
        /** Vertical offset per slot, in blocks. Only the four walls use it - see {@link #submit}. */
        public final float[] bob = new float[SLOT_COUNT];
        /** Ticks (fractional) since each nozzle last flashed - {@code Float.MAX_VALUE} if never. */
        public final float[] flashAge = new float[FLASH_COUNT];

        public SuctionTubeRenderState() {
            for (int i = 0; i < SLOT_COUNT; i++) {
                this.filters[i] = new ItemStackRenderState();
            }
            Arrays.fill(flashAge, Float.MAX_VALUE);
        }
    }
}
