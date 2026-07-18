package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.client.WunderKisteSpecialModelRenderer;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelRenderers.class)
public class SpecialModelRenderersMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void wunderreich_bootstrap(CallbackInfo ci) {
        ID_MAPPER.put(
                Wunderreich.ID("wunder_kiste"),
                WunderKisteSpecialModelRenderer.Unbaked.MAP_CODEC
        );
    }
}
