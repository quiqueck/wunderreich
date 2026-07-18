package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;
import de.ambertation.wunderreich.registries.WunderreichRules;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuctionTube extends BaseEntityBlock implements CanDropLoot, BlockTagSupplier {
    public static final MapCodec<SuctionTube> CODEC = simpleCodec(SuctionTube::new);

    private static double part(int i) {
        return i / 16.0D;
    }

    final VoxelShape SHAPE = Shapes.or(
            Shapes.box(part(0), part(0), part(0), part(16), part(2), part(2)),
            Shapes.box(part(0), part(0), part(14), part(16), part(2), part(16)),
            Shapes.box(part(0), part(0), part(2), part(2), part(2), part(14)),
            Shapes.box(part(14), part(0), part(2), part(16), part(2), part(14)),

            Shapes.box(part(0), part(2), part(0), part(2), part(8), part(2)),
            Shapes.box(part(14), part(2), part(0), part(16), part(8), part(2)),
            Shapes.box(part(0), part(2), part(14), part(2), part(8), part(16)),
            Shapes.box(part(14), part(2), part(14), part(16), part(8), part(16)),

            Shapes.box(part(0), part(8), part(0), part(16), part(10), part(2)),
            Shapes.box(part(0), part(8), part(14), part(16), part(10), part(16)),
            Shapes.box(part(0), part(8), part(2), part(2), part(10), part(14)),
            Shapes.box(part(14), part(8), part(2), part(16), part(10), part(14)),

            Shapes.box(part(2), part(10), part(2), part(14), part(12), part(4)),
            Shapes.box(part(2), part(10), part(12), part(14), part(12), part(14)),
            Shapes.box(part(2), part(10), part(4), part(4), part(12), part(12)),
            Shapes.box(part(12), part(10), part(4), part(14), part(12), part(12)),

            Shapes.box(part(4), part(12), part(4), part(12), part(14), part(6)),
            Shapes.box(part(4), part(12), part(10), part(12), part(14), part(12)),
            Shapes.box(part(4), part(12), part(6), part(6), part(14), part(10)),
            Shapes.box(part(10), part(12), part(6), part(12), part(14), part(10)),

            Shapes.box(part(6), part(14), part(6), part(10), part(16), part(7)),
            Shapes.box(part(6), part(14), part(9), part(10), part(16), part(10)),
            Shapes.box(part(6), part(14), part(7), part(7), part(16), part(9)),
            Shapes.box(part(9), part(14), part(7), part(10), part(16), part(9))
    );

    public SuctionTube(Properties properties) {
        super(properties);
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
        return true;
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
        if (!WunderreichRules.Wunderkiste.redstonePowerWhenOpened()) return 0;

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

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(blockState, builder);

        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            // Add all filter items from all directions to drops
            for (Direction direction : SuctionTubeBlockEntity.DIRECTIONS) {
                ItemStack[] filterItems = suctionTube.getFilterItems(direction);
                for (ItemStack filterItem : filterItems) {
                    if (!filterItem.isEmpty()) {
                        drops.add(filterItem.copy());
                    }
                }
            }
        }

        return drops;
    }

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