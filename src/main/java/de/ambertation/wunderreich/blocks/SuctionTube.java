package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichRules;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuctionTube extends BaseEntityBlock implements CanDropLoot, BlockTagSupplier {
    public static final MapCodec<SuctionTube> CODEC = simpleCodec(SuctionTube::new);

    /**
     * One flag per horizontal intake, driving the redstone torch that sits on the top plate above
     * that wall. Set when the intake has a container to pull from <em>and</em> at least one filter
     * item configured - i.e. when the side is set up, not when it happens to be moving an item this
     * tick. An intake that is only half configured stays dark, which is the whole point: the torches
     * are what tells you, without opening the menu, which of the four sides is actually doing
     * anything.
     * <p>
     * These are {@link BlockStateProperties}' own {@code north}/{@code east}/{@code south}/{@code
     * west} booleans, so no client-side sync is needed for the torches - the block state carries
     * them. Only the filter items on the rim need the block entity to be synced.
     * <p>
     * DOWN has no flag: there is no wall over the bottom intake to put a torch on.
     */
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    /**
     * Ring around the bottom intake nozzle - the fifth intake, now that it has a wall-less
     * equivalent of the torch nub to sit on. Same "is this intake configured" meaning as
     * {@link #NORTH}/{@link #EAST}/{@link #SOUTH}/{@link #WEST}.
     */
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    /**
     * Ring around the output spout. Unlike the four intake indicators, this does not mean "is
     * configured" - the tube has no filter to configure on the output side - it means "is a
     * valid container currently sitting above", i.e. whether pushing here can ever succeed. See
     * {@link de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity#getContainerAt}.
     */
    public static final BooleanProperty UP = BlockStateProperties.UP;

    private static double part(double i) {
        return i / 16.0D;
    }

    /**
     * The body, the top plate oversailing it, the spout standing on that, and the five intake
     * nozzles reaching out to the block faces - four sideways and one hanging below, where the body
     * is held two pixels clear of the ground. The four torch nubs are left out because a 2x1.5x2
     * step is not worth catching a player on, and so are the filter items: they are drawn by
     * {@code SuctionTubeRenderer} over the plate's rim and under the base, and nothing the renderer
     * draws should be something a player can stand on.
     * <p>
     * The two sideways nozzle boxes say y 4..8 because that is where the model puts them. They said
     * 5..9 for a while, which nothing collided with differently but which drew the highlight box a
     * pixel above the nozzle a player was pointing at.
     */
    final VoxelShape SHAPE = Shapes.or(
            Shapes.box(part(2), part(2), part(2), part(14), part(10), part(14)),
            Shapes.box(part(1), part(10), part(1), part(15), part(13), part(15)),
            Shapes.box(part(5), part(13), part(5), part(11), part(16), part(11)),
            Shapes.box(part(6), part(4), part(0), part(10), part(8), part(16)),
            Shapes.box(part(0), part(4), part(6), part(16), part(8), part(10)),
            Shapes.box(part(6), part(0), part(6), part(10), part(2), part(10))
    );

    /**
     * The intake-configured flag for a given intake direction, or {@code null} for
     * {@link Direction#UP}, which is the output side and uses {@link #UP} with different
     * semantics instead - see {@link #UP}.
     */
    @Nullable
    public static BooleanProperty activeProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case DOWN -> DOWN;
            default -> null;
        };
    }

    public SuctionTube(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                                                      .setValue(NORTH, false)
                                                      .setValue(EAST, false)
                                                      .setValue(SOUTH, false)
                                                      .setValue(WEST, false)
                                                      .setValue(DOWN, false)
                                                      .setValue(UP, false));
    }

    public SuctionTube(ResourceKey<Block> key) {
        this(Properties.of()
                       .mapColor(MapColor.STONE)
                       .requiresCorrectToolForDrops()
                       .strength(3.0F, 4.8F)
                       .sound(SoundType.METAL)
                       .setId(key));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, DOWN, UP);
    }

    /**
     * Centre of each torch nub in block space. The nubs are 2x2 at x/z 7..9 and 12..14 / 2..4, and
     * their heads sit on top at y 14.5, so the dust is spawned just clear of that.
     */
    private static final double TORCH_Y = 14.7 / 16.0;
    private static final double TORCH_NEAR = 3.0 / 16.0;
    private static final double TORCH_FAR = 13.0 / 16.0;

    /** Same idea as {@link #TORCH_Y}/{@link #TORCH_NEAR}/{@link #TORCH_FAR}, for the two rings. */
    private static final double RING_UP_Y = 13.45 / 16.0;
    private static final double RING_UP_NEAR = 5.0 / 16.0;
    private static final double RING_UP_FAR = 11.0 / 16.0;
    private static final double RING_DOWN_Y = 1.55 / 16.0;
    private static final double RING_DOWN_NEAR = 6.0 / 16.0;
    private static final double RING_DOWN_FAR = 10.0 / 16.0;

    /**
     * One puff of redstone dust per lit torch, the way {@code RedstoneTorchBlock} does it - same
     * particle, same still velocity, same idea that a burning torch is visibly burning. The two
     * rings reuse the same per-wall math, one puff per side of the square each tick they are lit.
     * <p>
     * The spread is a third of vanilla's: a redstone torch is a whole block tall and can afford
     * +-0.1, while these heads are 2px across and dust scattered that far would read as coming off
     * the top plate rather than off the torch.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        final boolean ringUp = state.getValue(UP);
        final boolean ringDown = state.getValue(DOWN);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            final BooleanProperty property = activeProperty(direction);
            if (property != null && state.getValue(property)) {
                spawnDust(level, pos, random, direction, TORCH_Y, TORCH_NEAR, TORCH_FAR);
            }
            if (ringUp) spawnDust(level, pos, random, direction, RING_UP_Y, RING_UP_NEAR, RING_UP_FAR);
            if (ringDown) spawnDust(level, pos, random, direction, RING_DOWN_Y, RING_DOWN_NEAR, RING_DOWN_FAR);
        }
    }

    private static void spawnDust(
            Level level, BlockPos pos, RandomSource random, Direction wallDirection,
            double y, double near, double far
    ) {
        final double offset = wallDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? near
                : far;
        final double x = pos.getX() + (wallDirection.getAxis() == Direction.Axis.X ? offset : 0.5)
                + (random.nextDouble() - 0.5) * 0.07;
        final double py = pos.getY() + y + random.nextDouble() * 0.07;
        final double z = pos.getZ() + (wallDirection.getAxis() == Direction.Axis.Z ? offset : 0.5)
                + (random.nextDouble() - 0.5) * 0.07;
        level.addParticle(DustParticleOptions.REDSTONE, x, py, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SuctionTubeBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

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
                        WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE,
                        SuctionTubeBlockEntity::serverTick
                );
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            CollisionContext collisionContext
    ) {
        return SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getShape(
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            CollisionContext collisionContext
    ) {
        // Return the same shape as collision for visual boundaries
        return SHAPE;
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        // The body is a solid box with nothing cut through it, so skylight has to stop here. The
        // old stepped-pyramid model really was open to the sky and this returned true to match it.
        return false;
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
            if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
                suctionTube.openMenu(serverPlayer);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    // Redstone signal output methods
    @Override
    protected boolean isSignalSource(BlockState state) {
        return WunderreichRules.Wunderkiste.redstonePowerWhenSucking();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return WunderreichRules.Wunderkiste.analogRedstoneOutputOnSuction();
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        if (!WunderreichRules.Wunderkiste.analogRedstoneOutputOnSuction()) return 0;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            return suctionTube.getRedstoneSignal();
        }
        return 0;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!WunderreichRules.Wunderkiste.redstonePowerWhenSucking()) return 0;

        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            return suctionTube.getRedstoneSignal() > 0 ? 15 : 0;
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            Direction direction
    ) {
        return getSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            suctionTube.neighborChanged(level, blockPos);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Block block,
            @Nullable Orientation orientation,
            boolean bl
    ) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            suctionTube.neighborChanged(level, blockPos);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    // NOTE: Filter items are pure ghost-slot templates (see SuctionTubeMenu.FilterSlot) - no
    // real item is ever consumed to set one, so none must ever be dropped when the tube
    // breaks. getDrops() is intentionally not overridden here anymore; the default
    // BaseEntityBlock/CanDropLoot behavior (dropping the tube itself) is all that applies.

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
    }


    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState blockState,
            ServerLevel serverLevel,
            BlockPos blockPos,
            boolean bl
    ) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }
}