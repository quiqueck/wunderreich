package de.ambertation.wunderreich.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import java.util.function.BiFunction;

public class WunderreichMenuTypes {
    public static final MenuType<WhispererMenu> WHISPERER = ScreenHandlerRegistry.registerSimple(
            Wunderreich.ID("whisperer"),
            WhispererMenu::new
    );

    public static void ensureStaticallyLoaded(){

    }
}
