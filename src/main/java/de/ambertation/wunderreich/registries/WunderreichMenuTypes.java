package de.ambertation.wunderreich.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import java.util.function.BiFunction;

public class WunderreichMenuTypes {
    public static final MenuType<WhispererMenu> WHISPERER = registerSimple(
            Wunderreich.ID("whisperer"),
            WhispererMenu::new
    );

    static <T extends AbstractContainerMenu> MenuType<T> registerSimple(
            ResourceLocation id,
            BiFunction<Integer, Inventory, T> factory
    ) {
        MenuType<T> type = new MenuType<>((syncId, inventory) -> factory.apply(syncId, inventory));
        return Registry.register(Registry.MENU, id, type);
    }

    public static void ensureStaticallyLoaded(){

    }
}
