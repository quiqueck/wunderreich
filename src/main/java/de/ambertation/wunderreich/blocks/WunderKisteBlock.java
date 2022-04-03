package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.interfaces.*;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.loot.LootTableJsonBuilder;
import de.ambertation.wunderreich.network.AddRemoveWunderKisteMessage;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.*;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class WunderKisteBlock extends AbstractChestBlock implements WorldlyContainerHolder, BlockTagSupplier, BlockEntityProvider<WunderKisteBlockEntity>, CanDropLoot {
    public static final DirectionProperty FACING;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape SHAPE;
    private static final Component CONTAINER_TITLE;
    public static final Set<LiveBlock> liveBlocks = ConcurrentHashMap.newKeySet(8);
    private static boolean hasAnyOpenInstance = false;

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        CONTAINER_TITLE = new TranslatableComponent("container.wunderreich.wunder_kiste");
    }

    EnderChestBlock chestBlock;

    public WunderKisteBlock() {
        super(WunderreichBlocks.makeStoneBlockSettings()
                               .luminance(7)
                               .requiresTool()
                               .strength(12.5F, 800.0F)
                , () -> {
                    return WunderreichBlockEntities.BLOCK_ENTITY_WUNDER_KISTE;
                });
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING,
                                                                      Direction.NORTH)
                                                      .setValue(WATERLOGGED, false));
    }

    public static void updateAllBoxes(MinecraftServer server, boolean withOpenState, boolean withFillrate) {
        WunderKisteContainer container = getContainer(server);
        WunderKisteBlock.updateAllBoxes(container, withOpenState, withFillrate);
    }

    private static void updateAllBoxes(WunderKisteContainer container, boolean withOpenState, boolean withFillrate) {
        boolean[] anyOpen = {false};
        if (container != null) {
            //check if any box was opened
            if (withOpenState) {
                liveBlocks.forEach((liveBlock) -> {
                    BlockEntity be = liveBlock.level.getBlockEntity(liveBlock.pos);
                    if (be instanceof WunderKisteBlockEntity) {
                        WunderKisteBlockEntity entity = (WunderKisteBlockEntity) be;
                        anyOpen[0] |= entity.isOpen();
                    }
                });
                hasAnyOpenInstance = anyOpen[0];
            }

            //send update message
            liveBlocks.forEach((liveBlock) -> updateNeighbours(liveBlock.level, liveBlock.pos));
        }

    }

    public static void updateNeighbours(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state != null) {
            Block block = state.getBlock();
            if (block instanceof WunderKisteBlock) {
                updateNeighbours(level, pos, state, block);
            }
        }
    }

    public static void updateNeighbours(Level level, BlockPos pos, BlockState state, Block box) {
        Direction facing = state.getValue(FACING);
        level.updateNeighbourForOutputSignal(pos, box);
        level.updateNeighborsAt(pos, box);
        level.updateNeighborsAt(pos.relative(facing), box);
    }

    public static WunderKisteContainer getContainer(Level level) {
        return level != null ? getContainer(level.getServer()) : null;
    }

    public static WunderKisteContainer getContainer(MinecraftServer server) {
        if (server != null && server instanceof WunderKisteContainerProvider) {
            return ((WunderKisteContainerProvider) server).getWunderKisteContainer();
        }
        return null;
    }

    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockState,
                                                                                         Level level,
                                                                                         BlockPos blockPos,
                                                                                         boolean bl) {
        return DoubleBlockCombiner.Combiner::acceptNone;
    }

    public VoxelShape getShape(BlockState blockState,
                               BlockGetter blockGetter,
                               BlockPos blockPos,
                               CollisionContext collisionContext) {
        return SHAPE;
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel()
                                                 .getFluidState(blockPlaceContext.getClickedPos());
        return this.defaultBlockState()
                   .setValue(FACING, blockPlaceContext.getHorizontalDirection()
                                                      .getOpposite()).setValue(WATERLOGGED,
                                                                               fluidState.getType() == Fluids.WATER);
    }

    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public boolean isPathfindable(BlockState blockState,
                                  BlockGetter blockGetter,
                                  BlockPos blockPos,
                                  PathComputationType pathComputationType) {
        return false;
    }

    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof WunderKisteBlockEntity) {
            ((WunderKisteBlockEntity) blockEntity).recheckOpen();
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState blockState,
                                                                  BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType,
                                                       WunderreichBlockEntities.BLOCK_ENTITY_WUNDER_KISTE,
                                                       WunderKisteBlockEntity::lidAnimateTick) : null;
    }

    @Override
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        final WunderKisteContainer wunderKisteContainer = getContainer(level);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (wunderKisteContainer != null && blockEntity instanceof WunderKisteBlockEntity) {
            BlockPos blockPos2 = blockPos.above();
            if (level.getBlockState(blockPos2)
                     .isRedstoneConductor(level, blockPos2)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                WunderKisteBlockEntity wunderKisteBlockEntity = (WunderKisteBlockEntity) blockEntity;

                ((ActiveChestStorage) player).setActiveWunderKiste(wunderKisteBlockEntity);

                player.openMenu(new SimpleMenuProvider((i, inventory, playerx) -> {
                    return ChestMenu.threeRows(i, inventory, wunderKisteContainer);
                }, CONTAINER_TITLE));
                //player.awardStat(Stats.OPEN_ENDERCHEST);
                PiglinAi.angerNearbyPiglins(player, true);
                return InteractionResult.CONSUME;
            }
        } else {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        for (int i = 0; i < 3; ++i) {
            int xFactor = random.nextInt(2) * 2 - 1;
            int zFactor = random.nextInt(2) * 2 - 1;

            double x0 = blockPos.getX() + 0.5D + 0.25D * xFactor;
            double y0 = blockPos.getY() + random.nextFloat();
            double z0 = blockPos.getZ() + 0.5 + 0.25 * zFactor;

            double xd = random.nextFloat() * xFactor;
            double yd = (random.nextFloat() - 0.5) * 0.125;
            double zd = random.nextFloat() * zFactor;

            level.addParticle(WunderreichParticles.EIR_PARTICLES, x0, y0, z0, xd, yd, zd);
        }

    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        //liveBlocks.add(blockPos);
        AddRemoveWunderKisteMessage.INSTANCE.send(true, blockPos);
        return new WunderKisteBlockEntity(blockPos, blockState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        WunderKisteContainer wunderKisteContainer = getContainer(level);
        if (wunderKisteContainer != null) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(wunderKisteContainer);
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        Direction facing = blockState.getValue(FACING);
        return /*direction==facing &&*/ hasAnyOpenInstance ? 15 : 0;
    }

//	@Override
//	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
//		Direction facing = (Direction)blockState.getValue(FACING);
//		return direction==facing?(hasAnyOpenInstance?15:5):0; //direction == Direction.UP ? 15/*blockState.getSignal(blockGetter, blockPos, direction)*/: 5;
//	}

    @Deprecated
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
    }

    @Deprecated
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onRemove(blockState, level, blockPos, blockState2, bl);

        if (blockState.hasBlockEntity() && !blockState.is(blockState2.getBlock())) {
            //liveBlocks.remove(blockPos);
            AddRemoveWunderKisteMessage.INSTANCE.send(false, blockPos);
        }
    }

    @Override
    public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        return getContainer(levelAccessor.getServer());
    }

    @Override
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    @Override
    public BlockEntityType getBlockEntityType() {
        return WunderreichBlockEntities.BLOCK_ENTITY_WUNDER_KISTE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockEntityRendererProvider getBlockEntityRenderProvider() {
        return ChestRenderer::new;
    }

    //custom code
    public static class LiveBlock {
        public final BlockPos pos;
        public final Level level;

        public LiveBlock(BlockPos pos, Level level) {
            this.pos = pos;
            this.level = level;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LiveBlock liveBlock = (LiveBlock) o;
            return pos.equals(liveBlock.pos) && level.dimension().equals(liveBlock.level.dimension());
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }

    @Override
    public LootTableJsonBuilder buildLootTable() {
        LootTableJsonBuilder b = LootTableJsonBuilder.create(this)
                                                     .startPool(1.0, 0.0, poolBuilder -> poolBuilder
                                                                        .startAlternatives(altBuilder -> altBuilder
                                                                                                   .startSelfEntry(builder -> builder
                                                                                                                           .silkTouch()
                                                                                                                  )
                                                                                                   .startItemEntry(Items.NETHERITE_SCRAP,
                                                                                                                   builder -> builder
                                                                                                                           .setCount(4, false)
                                                                                                                           .explosionDecay()
                                                                                                                  )
                                                                                          )
                                                               );

        return b;

    }
}
