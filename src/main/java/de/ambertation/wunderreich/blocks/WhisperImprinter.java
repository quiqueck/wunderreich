package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ru.bclib.api.TagAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.TagProvider;

import java.util.List;
import java.util.Random;

public class WhisperImprinter extends Block implements TagProvider, RenderLayerProvider {
    /**
     * Creates a new Block
     */
    public WhisperImprinter() {
        super(WunderreichBlocks.makeStoneBlockSettings()
                               .mapColor(MaterialColor.LAPIS)
                               .strength(5.0f, 1200.0f)
                               .luminance(8)
                               .breakByTool(FabricToolTags.PICKAXES)
                               .requiresTool()
                               .nonOpaque()
                               .sound(SoundType.AMETHYST)
        );
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        player.openMenu(blockState.getMenuProvider(level, blockPos));


        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((i, inventory, player) -> new WhispererMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), new TextComponent("Hello"));
    }
    
    @Override
    public void addTags(List<Named<Block>> blockTags, List<Named<Item>> itemTags) {
        blockTags.add(TagAPI.MINEABLE_PICKAXE);
    }
    
    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0f, 0.5f + level.random.nextFloat() * 1.2f);
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.5f + level.random.nextFloat() * 1.2f);
        }
    }
    
    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        super.animateTick(blockState, level, blockPos, random);

//        if (random.nextInt(8) != 0) {
//
//            Player player = Minecraft.getInstance().player;
//            level.addParticle(
//                    WunderreichParticles.IMPRINT_PARTICLES,
//                    blockPos.getX() + 0.5,
//                    blockPos.getY() + 2,
//                    blockPos.getZ() + 0.5,
//                    player.getX() + random.nextFloat() - 0.5,
//                    player.getY(),
//                    player.getZ()+ random.nextFloat() - 0.5
//            );
//        }

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i==0 && j==0) continue;
                if (random.nextInt(20) != 0) continue;
                for (int k = 1; k <= 4; ++k) {
                    level.addParticle(
                        WunderreichParticles.IMPRINT_PARTICLES,
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 2,
                        blockPos.getZ() + 0.5,
                        i + random.nextFloat() - 0.5,
                        k - random.nextFloat() - 1.0f,
                        j + random.nextFloat() - 0.5
                    );
                }
            }
        }
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }
}
