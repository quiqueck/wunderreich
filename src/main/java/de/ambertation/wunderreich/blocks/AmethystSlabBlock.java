package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

public class AmethystSlabBlock extends DirtSlabBlock implements BlockTagSupplier, CanDropLoot {
    public static final MapCodec<AmethystSlabBlock> CODEC = simpleCodec(AmethystSlabBlock::new);

    protected AmethystSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public AmethystSlabBlock(
            Block baseBlock,
            ResourceKey<Block> key
    ) {
        super(baseBlock, key);
    }

    @Override
    public void supplyTags(Consumer<TagKey<Block>> blockTags, Consumer<TagKey<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    @Override
    public MapCodec<? extends SlabBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onProjectileHit(
            Level level,
            BlockState blockState,
            BlockHitResult blockHitResult,
            Projectile projectile
    ) {
        if (!level.isClientSide()) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            level.playSound(
                    null,
                    blockPos,
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.5F + level.getRandom().nextFloat() * 1.2F
            );
        }
    }
}
