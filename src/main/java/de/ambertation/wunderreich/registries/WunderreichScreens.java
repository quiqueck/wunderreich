package de.ambertation.wunderreich.registries;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.gui.whisperer.WhispererScreen;

import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public final class WunderreichScreens {
    public static void registerScreens() {
        MenuScreens.register(WHISPERER, WhispererScreen::new);
    }

    static <T extends AbstractContainerMenu> MenuType<T> registerSimple(
            ResourceLocation id,
            BiFunction<Integer, Inventory, T> factory
    ) {
        MenuType<T> type = new MenuType<>((syncId, inventory) -> factory.apply(syncId, inventory));
        return Registry.register(Registry.MENU, id, type);
    }


    public static final MenuType<WhispererMenu> WHISPERER = registerSimple(
            Wunderreich.ID("whisperer"),
            WhispererMenu::new
    );


}
