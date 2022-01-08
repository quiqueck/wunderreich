package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.MainConfig;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public class MonsterMixin {
    @Inject(method = "shouldDespawnInPeaceful", at = @At("HEAD"), cancellable = true)
    protected void wunder_shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (Configs.MAIN.get(MainConfig.DO_NOT_DESPANW_WITH_NAMETAG)) {
            Monster m = (Monster) (Object) this;
            if (m.hasCustomName()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
