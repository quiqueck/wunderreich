package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.BoxOfEirBlockEntity;
import de.ambertation.wunderreich.client.WunderreichClient;
import de.ambertation.wunderreich.interfaces.ActiveChestStorage;
import de.ambertation.wunderreich.interfaces.BoxOfEirContainerProvider;
import de.ambertation.wunderreich.inventory.BoxOfEirContainer;
import de.ambertation.wunderreich.network.AddRemoveBoxOfEirMessage;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import io.netty.util.internal.ConcurrentSet;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.EnderChestBlock;
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
import ru.bclib.api.TagAPI;
import ru.bclib.interfaces.TagProvider;

import java.util.List;
import java.util.Random;

public class BoxOfEirBlock extends AbstractChestBlock implements WorldlyContainerHolder, TagProvider {
	public static final DirectionProperty FACING;
	public static final BooleanProperty WATERLOGGED;
	protected static final VoxelShape SHAPE;
	private static final Component CONTAINER_TITLE;
	EnderChestBlock chestBlock;
	
	public BoxOfEirBlock() {
		super(WunderreichBlocks.makeStoneBlockSettings()
							   .luminance(7)
							   //TODO: This needs to change to the TagAPI from BCLib!
							   .breakByTool(FabricToolTags.PICKAXES)
							   .requiresCorrectToolForDrops()
							   .strength(12.5F, 800.0F)
							   , () -> {
			return WunderreichBlockEntities.BLOCK_ENTITY_BOX_OF_EIR;
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
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
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
		return level.isClientSide ? createTickerHelper(blockEntityType, WunderreichBlockEntities.BLOCK_ENTITY_BOX_OF_EIR, BoxOfEirBlockEntity::lidAnimateTick) : null;
	}
	
	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		final BoxOfEirContainer boxOfEirContainer = getContainer(level);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (boxOfEirContainer!=null && blockEntity instanceof BoxOfEirBlockEntity) {
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

				((ActiveChestStorage)player).setActiveBoxOfEir(boxOfEirBlockEntity);

				player.openMenu(new SimpleMenuProvider((i, inventory, playerx) -> {
					return ChestMenu.threeRows(i, inventory, boxOfEirContainer);
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
			
			double x0 =  blockPos.getX() + 0.5D + 0.25D * xFactor;
			double y0 =  blockPos.getY() + random.nextFloat();
			double z0 =  blockPos.getZ() + 0.5 + 0.25 *  zFactor;
			
			double xd = random.nextFloat() *  xFactor;
			double yd = ( random.nextFloat() - 0.5) * 0.125;
			double zd = random.nextFloat() * zFactor;
			
			level.addParticle(WunderreichClient.EIR_PARTICLES, x0, y0, z0, xd, yd, zd);
		}
		
	}

	@Override
	public void addTags(List<Tag.Named<Block>> blockTags, List<Tag.Named<Item>> itemTags) {
		blockTags.add(TagAPI.MINEABLE_PICKAXE);
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
	public static ConcurrentSet<LiveBlock> liveBlocks = new ConcurrentSet<>();
	private static boolean hasAnyOpenInstance = false;
	public static void updateAllBoxes(MinecraftServer server, boolean withOpenState, boolean withFillrate) {
		BoxOfEirContainer container = getContainer(server);
		BoxOfEirBlock.updateAllBoxes(container, withOpenState, withFillrate);
	}
	
	private static void updateAllBoxes(BoxOfEirContainer container,  boolean withOpenState, boolean withFillrate) {
		boolean[] anyOpen = {false};
		if (container!=null){
			//check if any box was opened
			if (withOpenState) {
				liveBlocks.forEach((liveBlock) -> {
					BlockEntity be = liveBlock.level.getBlockEntity(liveBlock.pos);
					if (be instanceof BoxOfEirBlockEntity) {
						BoxOfEirBlockEntity entity = (BoxOfEirBlockEntity) be;
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
		if (state!=null) {
			Block block = state.getBlock();
			if (block instanceof  BoxOfEirBlock){
				updateNeighbours(level, pos, state, block);
			}
		}
	}
	
	public static void updateNeighbours(Level level, BlockPos pos, BlockState state, Block box) {
		Direction facing = (Direction) state.getValue(FACING);
		level.updateNeighbourForOutputSignal(pos, box);
		level.updateNeighborsAt(pos, box);
		level.updateNeighborsAt(pos.relative(facing), box);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		//liveBlocks.add(blockPos);
		AddRemoveBoxOfEirMessage.send(true, blockPos);
		return new BoxOfEirBlockEntity(blockPos, blockState);
	}
	
	public static BoxOfEirContainer getContainer(Level level){
		return level!=null ? getContainer(level.getServer()) : null;
	}
	
	public static BoxOfEirContainer getContainer(MinecraftServer server){
		if (server!=null && server instanceof BoxOfEirContainerProvider) {
			return ((BoxOfEirContainerProvider)server).getBoxOfEirContainer();
		}
		return null;
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		BoxOfEirContainer boxOfEirContainer = getContainer(level);
		if (boxOfEirContainer!=null) {
			 return AbstractContainerMenu.getRedstoneSignalFromContainer(boxOfEirContainer);
		}
		return 0;
	}
	
	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}
	
	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		Direction facing = (Direction)blockState.getValue(FACING);
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
			AddRemoveBoxOfEirMessage.send(false, blockPos);
		}
	}
	
	@Override
	public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		return getContainer(levelAccessor.getServer());
	}
	
	static {
		FACING = HorizontalDirectionalBlock.FACING;
		WATERLOGGED = BlockStateProperties.WATERLOGGED;
		SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
		CONTAINER_TITLE = new TranslatableComponent("container.wunderreich.box_of_eir");
	}
}
