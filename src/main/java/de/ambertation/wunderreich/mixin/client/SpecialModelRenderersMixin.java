package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.client.WunderKisteSpecialModelRenderer;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.world.level.block.Block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SpecialModelRenderers.class)
public class SpecialModelRenderersMixin {

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void wunderreich_bootstrap(CallbackInfo ci) {
        SpecialModelRenderers.ID_MAPPER.put(
                Wunderreich.ID("wunder_kiste"),
                WunderKisteSpecialModelRenderer.Unbaked.MAP_CODEC
        );
    }


    @WrapOperation(
            method = "createBlockRenderers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private static Object wunderreich_createBlockRenderers(
            Map<Block, SpecialModelRenderer.Unbaked> map,
            Object block,
            Object unbaked,
            Operation<Object> original
    ) {
        // Call the original put
        Object result = original.call(map, block, unbaked);

        map.put(
                WunderreichBlocks.WUNDER_KISTE,
                new WunderKisteSpecialModelRenderer.Unbaked()
        );

        return result;
    }
}
