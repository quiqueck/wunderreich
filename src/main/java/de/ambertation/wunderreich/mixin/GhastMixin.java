package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ghast.class)
public class GhastMixin {
    @Inject(method = "shouldDespawnInPeaceful", at = @At("HEAD"), cancellable = true)
    protected void wunder_shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (WunderreichRules.doNotDespawnWithNameTag()) {
            Ghast m = (Ghast) (Object) this;
            if (m.hasCustomName()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
