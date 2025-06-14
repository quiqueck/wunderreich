package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ItemCombinerMenuMixin {
    // We need to override this method, as the helper assumes that all inventory slots are render at
    // the same position, which is not the case for the ItemCombinerMenu.
    // However, we need to change the screen location for the WhispererMenu
    @Inject(method = "addStandardInventorySlots", at = @At("HEAD"), cancellable = true)
    private void wunderreich_addStandardInventorySlots(Container inventory, int i, int j, CallbackInfo ci) {
        if ((Object) this instanceof WhispererMenu wm) {
            wm.createCustomInventorySlots(inventory);
            ci.cancel();
        }
    }
}
