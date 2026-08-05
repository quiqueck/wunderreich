package de.ambertation.wunderreich.mixin.despawn;

import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobMixin {
    // In 26.1 the dedicated `shouldDespawnInPeaceful` hook was removed and the peaceful
    // instant-despawn was folded into Mob#checkDespawn. For a mob that carries a custom
    // name the only thing checkDespawn still does is that peaceful discard (persistent
    // named mobs already skip the far-away despawn), so cancelling at HEAD faithfully keeps
    // name-tagged mobs alive - matching the previous per-monster behaviour.
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    protected void wunder_checkDespawn(CallbackInfo ci) {
        if (WunderreichRules.doNotDespawnWithNameTag()) {
            Mob m = (Mob) (Object) this;
            if (m.hasCustomName()) {
                ci.cancel();
            }
        }
    }
}
