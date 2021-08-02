package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.inventory.BoxOfEirContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
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
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BoxOfEirBlock extends AbstractChestBlock {
	public static final DirectionProperty FACING;
	public static final BooleanProperty WATERLOGGED;
	protected static final VoxelShape SHAPE;
	private static final Component CONTAINER_TITLE;
	
	public BoxOfEirBlock(Properties properties) {
		super(properties, () -> {
			return Wunderreich.BLOCK_ENTITY_BOX_OF_EIR;
		});
		this.registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(WATERLOGGED, false));
	}
	
	public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
		return DoubleBlockCombiner.Combiner::acceptNone;
	}
	
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}
	
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel()
												 .getFluidState(blockPlaceContext.getClickedPos());
		return (BlockState) ((BlockState) this.defaultBlockState()
											  .setValue(FACING, blockPlaceContext.getHorizontalDirection()
																				 .getOpposite())).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}
	
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
	}
	
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}
	
	public FluidState getFluidState(BlockState blockState) {
		return (Boolean) blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}
	
	public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
		if ((Boolean) blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks()
						 .scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}
		
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}
	
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
	
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		if (blockEntity instanceof BoxOfEirBlockEntity) {
			((BoxOfEirBlockEntity) blockEntity).recheckOpen();
		}
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? createTickerHelper(blockEntityType, Wunderreich.BLOCK_ENTITY_BOX_OF_EIR, BoxOfEirBlockEntity::lidAnimateTick) : null;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BoxOfEirBlockEntity(blockPos, blockState);
	}
	
	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		BoxOfEirContainer boxOfEirContainer = BoxOfEirContainer.getInstance();
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (boxOfEirContainer != null && blockEntity instanceof BoxOfEirBlockEntity) {
			BlockPos blockPos2 = blockPos.above();
			if (level.getBlockState(blockPos2)
					 .isRedstoneConductor(level, blockPos2)) {
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
			else if (level.isClientSide) {
				return InteractionResult.SUCCESS;
			}
			else {
				BoxOfEirBlockEntity boxOfEirBlockEntity = (BoxOfEirBlockEntity) blockEntity;
				boxOfEirContainer.setActiveChest(boxOfEirBlockEntity);
				player.openMenu(new SimpleMenuProvider((i, inventory, playerx) -> {
					return ChestMenu.threeRows(i, inventory, boxOfEirContainer);
				}, CONTAINER_TITLE));
				//player.awardStat(Stats.OPEN_ENDERCHEST);
				PiglinAi.angerNearbyPiglins(player, true);
				return InteractionResult.CONSUME;
			}
		}
		else {
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}
	
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		for (int i = 0; i < 3; ++i) {
			int j = random.nextInt(2) * 2 - 1;
			int k = random.nextInt(2) * 2 - 1;
			double d = (double) blockPos.getX() + 0.5D + 0.25D * (double) j;
			double e = (float) blockPos.getY() + random.nextFloat();
			double f = (double) blockPos.getZ() + 0.5D + 0.25D * (double) k;
			double g = random.nextFloat() * (float) j;
			double h = ((double) random.nextFloat() - 0.5D) * 0.125D;
			double l = random.nextFloat() * (float) k;
			level.addParticle(ParticleTypes.SNOWFLAKE, d, e, f, g, h, l);
		}
		
	}
	
	//custom code
	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		BoxOfEirContainer boxOfEirContainer = BoxOfEirContainer.getInstance();
		return AbstractContainerMenu.getRedstoneSignalFromContainer(boxOfEirContainer);
	}
	
	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}
	
	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return Mth.clamp((int) ChestBlockEntity.getOpenCount(blockGetter, blockPos), (int) 0, (int) 15);
	}
	
	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return direction == Direction.UP ? blockState.getSignal(blockGetter, blockPos, direction) : 0;
	}
	
	static {
		FACING = HorizontalDirectionalBlock.FACING;
		WATERLOGGED = BlockStateProperties.WATERLOGGED;
		SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
		CONTAINER_TITLE = new TranslatableComponent("container.wunderreich.box_of_eir");
	}
}
