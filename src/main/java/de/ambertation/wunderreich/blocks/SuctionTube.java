package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlockEntities;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class SuctionTube extends BaseEntityBlock implements CanDropLoot {
    public static final MapCodec<SuctionTube> CODEC = simpleCodec(SuctionTube::new);

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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SuctionTubeBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return level.isClientSide
                ? null
                : createTickerHelper(
                        blockEntityType,
                        WunderreichBlockEntities.BLOCK_ENTITY_SUCTION_TUBE,
                        SuctionTubeBlockEntity::serverTick
                );
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            CollisionContext collisionContext
    ) {
        return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.99D, 1.0D);
    }

    // Redstone signal output methods
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof SuctionTubeBlockEntity suctionTube) {
            return suctionTube.getRedstoneSignal(direction);
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return getSignal(blockState, blockGetter, blockPos, direction);
    }
}