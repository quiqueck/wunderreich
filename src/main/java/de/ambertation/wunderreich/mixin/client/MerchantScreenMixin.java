package de.ambertation.wunderreich.mixin.client;


import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.config.MainConfig;
import de.ambertation.wunderreich.gui.CycleTradesButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow
    @Final
    private static int MERCHANT_MENU_PART_X;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void wunderreich_onInit(CallbackInfo ci) {
        if (!WunderreichConfigs.MAIN.get(MainConfig.ALLOW_TRADES_CYCLING)) return;

        MerchantScreen merchantScreen = (MerchantScreen) (Object) this;
        CycleTradesButton button = CycleTradesButton.getCycleTradesButton(this, this.imageWidth, this.imageHeight, merchantScreen, this.menu);

        this.addRenderableWidget(button);
    }


}
