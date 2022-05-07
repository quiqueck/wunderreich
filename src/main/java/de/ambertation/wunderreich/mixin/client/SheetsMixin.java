package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.client.WunderreichClient;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(Sheets.class)
public abstract class SheetsMixin {
    @Inject(cancellable = true, method = "chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;", at = @At("HEAD"))
    private static void wunderreich_choose(BlockEntity blockEntity,
                                           ChestType chestType,
                                           boolean bl,
                                           CallbackInfoReturnable<Material> cir) {
        if (blockEntity instanceof WunderKisteBlockEntity) {
            cir.setReturnValue(WunderreichClient.WUNDER_KISTE_LOCATION);
            cir.cancel();
        }
    }

    @Inject(method = "getAllMaterials", at = @At("TAIL"))
    private static void wunderreich_getAll(Consumer<Material> consumer, CallbackInfo ci) {
        consumer.accept(WunderreichClient.WUNDER_KISTE_TOP_LOCATION);
        consumer.accept(WunderreichClient.WUNDER_KISTE_MONOCHROME_TOP_LOCATION);
        WunderreichClient.getAllWunderkisteMaterials(consumer);
    }

}
