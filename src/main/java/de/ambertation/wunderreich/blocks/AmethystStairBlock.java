package de.ambertation.wunderreich.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystStairBlock extends StairBlock {
    public static final MapCodec<AmethystStairBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                                        BlockState.CODEC.fieldOf("base_state")
                                                        .forGetter(stairBlock -> stairBlock.baseState), propertiesCodec()
                                )
                                .apply(instance, AmethystStairBlock::new)
    );

    protected AmethystStairBlock(BlockState blockState, Properties properties) {
        super(blockState, properties);
    }

    public AmethystStairBlock(
            Block baseBlock,
            ResourceKey<Block> key
    ) {
        super(baseBlock, key);
    }

    @Override
    public MapCodec<? extends net.minecraft.world.level.block.StairBlock> codec() {
        return CODEC;
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
