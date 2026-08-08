package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blocks.DirtSlabBlock;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockTransformerMappings;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.3 removed ShovelItem: turning dirt into a dirt path is now driven by the BLOCK_TRANSFORMER
// data component, so we hook the shovel transformer instead of the (no longer existing) item class.
@Mixin(BlockTransformer.class)
public class ShovelItemMixin {
    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    public void wunderreich_transformBlock(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this != BlockTransformerMappings.SHOVEL) return;
        if (useOnContext.getClickedFace() != Direction.DOWN) {
            if (!Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.DIRT_PATH_SLAB))
                return;
            final Level level = useOnContext.getLevel();
            final BlockPos pos = useOnContext.getClickedPos();
            final BlockState currentState = level.getBlockState(pos);
            if (
                    (
                            Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.DIRT_SLAB)
                                    && WunderreichSlabBlocks.DIRT_SLAB.equals(currentState.getBlock())
                    )
                            || (
                            Configs.BLOCK_CONFIG.isEnabled(WunderreichSlabBlocks.GRASS_SLAB)
                                    && WunderreichSlabBlocks.GRASS_SLAB.equals(currentState.getBlock())
                    )
            ) {

                final BlockState newState = DirtSlabBlock.createStateFrom(
                        WunderreichSlabBlocks.DIRT_PATH_SLAB,
                        currentState
                );
                final Player player = useOnContext.getPlayer();
                if (!level.isClientSide()) {
                    level.setBlock(pos, newState, 11);
                    if (player != null) {
                        useOnContext
                                .getItemInHand()
                                .hurtAndBreak(1, player, useOnContext.getHand());
                    }
                }

                cir.setReturnValue(InteractionResult.SUCCESS);
                cir.cancel();
            }
        }
    }
}
