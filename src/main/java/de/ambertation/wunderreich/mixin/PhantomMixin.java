package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.config.Configs;

import net.minecraft.world.entity.monster.Phantom;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Phantom.class)
public class PhantomMixin {
    @Inject(method = "shouldDespawnInPeaceful", at = @At("HEAD"), cancellable = true)
    protected void wunder_shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (Configs.MAIN.doNotDespawnWithNameTag.get()) {
            Phantom m = (Phantom) (Object) this;
            if (m.hasCustomName()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
