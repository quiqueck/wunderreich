package de.ambertation.wunderreich.blocks;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichParticles;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class WhisperImprinter extends Block implements BlockTagSupplier, ChangeRenderLayer, CanDropLoot {
    /**
     * Creates a new Block
     */
    public WhisperImprinter() {
        super(WunderreichBlocks.makeStoneBlockSettings()
                               .mapColor(MaterialColor.LAPIS)
                               .strength(5.0f, 1200.0f)
                               .luminance(8)
                               .requiresTool()
                               .nonOpaque()
                               .sound(SoundType.AMETHYST)
             );
    }

    @Override
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (Configs.MAIN.allowLibrarianSelection()) {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((i, inventory, player) -> new WhispererMenu(i,
                                                                                  inventory,
                                                                                  ContainerLevelAccess.create(level,
                                                                                                              blockPos)),
                                      new TextComponent("Hello"));
    }

    @Override
    public void onProjectileHit(Level level,
                                BlockState blockState,
                                BlockHitResult blockHitResult,
                                Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            level.playSound(null,
                            blockPos,
                            SoundEvents.AMETHYST_BLOCK_HIT,
                            SoundSource.BLOCKS,
                            1.0f,
                            0.5f + level.random.nextFloat() * 1.2f);
            level.playSound(null,
                            blockPos,
                            SoundEvents.AMETHYST_BLOCK_CHIME,
                            SoundSource.BLOCKS,
                            1.0f,
                            0.5f + level.random.nextFloat() * 1.2f);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        super.animateTick(blockState, level, blockPos, random);


//        if (random.nextInt(4) == 0 && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
//            Player player = Minecraft.getInstance().player;
//            final Vec3 bp = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//            Vec3 v = bp.subtract((float)player.getX(), (float)player.getY(), (float)player.getZ());
//            float len = Math.max(0, (float)v.length() - 0.707f);
//            if (len>0 && len<4) {
//                v = v.normalize().multiply(len, len, len);
//
//                level.addParticle(
//                        ParticleTypes.PORTAL,
//                        blockPos.getX() + 0.5,
//                        blockPos.getY() + 1,
//                        blockPos.getZ() + 0.5,
//                        -v.x + (random.nextFloat()-0.5)*0.5,
//                        -v.y - random.nextFloat()-0.5,
//                        -v.z + (random.nextFloat()-0.5)*0.5
//                );
//            }
//        }

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                if (random.nextInt(40) != 0) continue;
                for (int k = 2; k <= 5; ++k) {
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
    public void supplyTags(Consumer<Tag.Named<Block>> blockTags, Consumer<Tag.Named<Item>> itemTags) {
        blockTags.accept(BlockTags.MINEABLE_WITH_PICKAXE);
        blockTags.accept(BlockTags.NEEDS_STONE_TOOL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public RenderType getRenderType() {
        return RenderType.cutout();
    }
}
