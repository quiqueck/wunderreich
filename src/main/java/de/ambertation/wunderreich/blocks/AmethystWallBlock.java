package de.ambertation.wunderreich.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystWallBlock extends WallBlock {
    public static final MapCodec<AmethystWallBlock> CODEC = simpleCodec(AmethystWallBlock::new);

    protected AmethystWallBlock(Properties properties) {
        super(properties);
    }

    public AmethystWallBlock(
            Block baseBlock,
            ResourceKey<Block> key
    ) {
        super(baseBlock, key);
    }

    @Override
    public MapCodec<net.minecraft.world.level.block.WallBlock> codec() {
        return (MapCodec<net.minecraft.world.level.block.WallBlock>) (Object) CODEC;
    }

    @Override
    protected void onProjectileHit(
            Level level,
            BlockState blockState,
            BlockHitResult blockHitResult,
            Projectile projectile
    ) {
        if (!level.isClientSide) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            level.playSound(
                    null,
                    blockPos,
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.5F + level.random.nextFloat() * 1.2F
            );
        }
    }
}
