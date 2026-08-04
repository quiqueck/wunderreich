package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void onEjectItems(
            Level level,
            BlockPos pos,
            HopperBlockEntity hopper,
            CallbackInfoReturnable<Boolean> cir
    ) {
        for (int i = 0; i < hopper.getContainerSize(); ++i) {
            ItemStack stack = hopper.getItem(i);
            if (!stack.isEmpty()) {
                if (SuctionTubeBlockEntity.isItemTargetedByAdjacentSuctionTube(level, pos, stack)) {
                    cir.setReturnValue(false);
                    return;
                }
                break;
            }
        }
    }
}
