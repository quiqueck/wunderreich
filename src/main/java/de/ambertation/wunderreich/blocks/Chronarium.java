package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.ChronariumBlockEntity;
import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Chronarium: a mystical fast-aging device. An input item and a (never consumed) catalyst go
 * in, time runs fast for the input and it ages into the recipe result. From the player's point of
 * view it behaves like a furnace.
 * <p>
 * The blockstate has two properties. {@link BlockStateProperties#HORIZONTAL_FACING} orients the
 * block: the hand written {@code assets/wunderreich/blockstates/chronarium.json} maps north&rarr;y0,
 * east&rarr;y90, south&rarr;y180, west&rarr;y270 and the model's glass window sits on the north
 * face, so {@link #getStateForPlacement(BlockPlaceContext)} stores the <em>opposite</em> of the
 * player's horizontal direction, exactly like a furnace.
 * <p>
 * {@link #WORKING} is the furnace's {@code lit} in all but name: it is the only thing the outside of
 * the block can say about what is happening inside it. It picks the {@code chronarium_working} model
 * (identical except that the three clock dials are swapped for an animated time vortex) and it
 * raises the light level from {@link #LIGHT_IDLE} to {@link #LIGHT_WORKING}. Nothing on the server
 * reads it - it is written by {@link ChronariumBlockEntity} and read by the renderer and the light
 * engine only.
 *
 * @see ChronariumBlockEntity
 */
public class Chronarium extends BaseEntityBlock implements CanDropLoot, BlockTagSupplier, ChangeRenderLayer {
    public static final MapCodec<Chronarium> CODEC = simpleCodec(Chronarium::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /**
     * Whether a recipe is currently running. Set by {@link ChronariumBlockEntity} on transitions
     * only, see {@code ChronariumBlockEntity#updateWorkingState}.
     */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    /**
     * Idle: dark, exactly like an unlit furnace. Nothing the block shows on the outside is emissive
     * while it sits empty - not the side dials, not the sigil in the basin - so it must not light its
     * neighbours either. A block that casts light 4 with no lit pixel on it reads as a bug in the
     * shading rather than as a glow.
     */
    public static final int LIGHT_IDLE = 0;
    /**
     * Working: brighter than a redstone torch (7) and just under a lit furnace-lit room, so a
     * running Chronarium visibly lifts the light around it without turning into a lamp.
     */
    public static final int LIGHT_WORKING = 12;

    /**
     * House rule: a block constructor never mutates the {@link Properties} it is handed. All
     * property tweaks live at the registration site, i.e. in the {@link ResourceKey} constructor
     * below.
     */
    public Chronarium(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                                                      .setValue(FACING, Direction.NORTH)
                                                      .setValue(WORKING, false));
    }

    public Chronarium(ResourceKey<Block> key) {
        this(WunderreichBlocks.makeStoneBlockSettings()
                              .mapColor(MapColor.COLOR_CYAN)
                              .requiresCorrectToolForDrops()
                              .strength(3.5F, 6.0F)
                              // Evaluated per state when the state cache is built, i.e. after
                              // createBlockStateDefinition has run, so WORKING is safe to read here.
                              .lightLevel(state -> state.getValue(WORKING) ? LIGHT_WORKING : LIGHT_IDLE)
                              .noOcclusion()
                              .sound(SoundType.COPPER)
                              .setId(key));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        // The glass window is modelled on the north face, so the block has to look at the player.
        // WORKING stays at the default state's false: a freshly placed Chronarium is empty.
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChronariumBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    /**
     * Recipe lookup only works on a {@link ServerLevel} (see
     * {@code CatalystRecipe#find}), so the Chronarium deliberately has no client ticker.
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        blockEntityType,
                        WunderreichBlockEntities.BLOCK_ENTITY_CHRONARIUM,
                        ChronariumBlockEntity::serverTick
                );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChronariumBlockEntity chronarium) {
                serverPlayer.openMenu(chronarium);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The three slots are dropped by the default
     * {@link BlockEntity#preRemoveSideEffects(BlockPos, BlockState)}, which empties every block
     * entity that implements {@link net.minecraft.world.Container}. This only has to tell the
     * neighbours (comparators) that the container is gone.
     */
    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState blockState,
            ServerLevel serverLevel,
            BlockPos blockPos,
            boolean movedByPiston
    ) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ChronariumBlockEntity chronarium) {
            return chronarium.getRedstoneSignal();
        }
        return 0;
    }

    /**
     * Height of the basin floor in block space. The model's cavity spans x/z 3..13 with its floor at
     * y=10, so this sits a hair above the actual floor to keep the motes off the texture.
     */
    private static final double BASIN_FLOOR_Y = 0.655;
    /**
     * Motes spawned per {@code animateTick} call. This is <em>not</em> per tick: the client only
     * calls {@code animateTick} on randomly sampled positions, which works out at roughly 2-3 calls
     * per second for a block a few metres from the player, so this averages ~5 motes/s per block.
     * With the ~3 s particle lifetime that is a swarm of ~13, which is enough to read as a
     * continuous ring without ever being expensive.
     */
    private static final int VORTEX_MOTES_MIN = 1;
    private static final int VORTEX_MOTES_SPREAD = 3;

    /**
     * The third and last piece of "it is running" feedback, next to the animated dials of the
     * {@code chronarium_working} model and the raised light level: purple amethyst motes orbiting
     * inside the basin. Gated purely on {@link #WORKING}, so this costs nothing on the server and
     * needs no block entity access - see {@code ChronariumVortexParticle} for the motion itself.
     */
    @Override
    public void animateTick(
            @NotNull BlockState blockState,
            @NotNull Level level,
            @NotNull BlockPos blockPos,
            @NotNull RandomSource random
    ) {
        if (!blockState.getValue(WORKING)) return;

        final double x = blockPos.getX() + 0.5;
        final double y = blockPos.getY() + BASIN_FLOOR_Y;
        final double z = blockPos.getZ() + 0.5;

        final int count = VORTEX_MOTES_MIN + random.nextInt(VORTEX_MOTES_SPREAD);
        for (int i = 0; i < count; i++) {
            // The position is the vortex axis, not the spawn point: the particle picks its own
            // radius and phase around it. The deltas are unused.
            level.addParticle(WunderreichParticles.CHRONARIUM_VORTEX_PARTICLES, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
        blockTags.accept(BlockTags.NEEDS_STONE_TOOL);
    }

    /**
     * Only relevant for datagen; the hand written model JSON already declares
     * {@code "render_type": "minecraft:cutout"} for the glass window.
     */
    @Override
    public ChangeRenderLayer.RenderLayer getRenderType() {
        return ChangeRenderLayer.RenderLayer.CUTOUT;
    }
}
