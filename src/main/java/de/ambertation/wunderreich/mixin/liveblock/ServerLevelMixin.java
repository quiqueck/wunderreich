package de.ambertation.wunderreich.mixin.liveblock;

import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    public abstract void resetEmptyTime();

    @Inject(method = "shouldTickBlocksAt", at = @At("HEAD"), cancellable = true)
    private void wunderreich_shouldTickBlocksAt(long l, CallbackInfoReturnable<Boolean> cir) {
        if (WunderKisteServerExtension.WUNDERKISTEN.shouldTick((ServerLevel) (Object) this, new ChunkPos(l))) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void wunderreich_shouldTickBlocksAt(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        if (WunderKisteServerExtension.WUNDERKISTEN.shouldTick((ServerLevel) (Object) this)) {
            resetEmptyTime();
        }
    }
}
