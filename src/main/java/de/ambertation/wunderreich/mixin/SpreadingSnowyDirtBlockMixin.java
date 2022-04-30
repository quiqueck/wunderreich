package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.blocks.SpreadableSnowyDirtSlab;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockMixin {


    @Inject(method = "randomTick", at = @At("TAIL"))
    void wunderreich_randomTick(BlockState blockState,
                                ServerLevel serverLevel,
                                BlockPos blockPos,
                                Random random,
                                CallbackInfo ci) {
        SpreadableSnowyDirtSlab.spreadingTick((SpreadingSnowyDirtBlock) (Object) this,
                blockState,
                serverLevel,
                blockPos,
                random);
    }
}
