package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.construction.RulerContainerMenu;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public class WunderreichMenuTypes {
    public static final MenuType<WhispererMenu> WHISPERER = registerSimple(
            Wunderreich.ID("whisperer"),
            WhispererMenu::new
    );
    
    public static final MenuType<RulerContainerMenu> RULER = registerExtended(
            Wunderreich.ID("ruler"),
            RulerContainerMenu::new
    );

    static <T extends AbstractContainerMenu> MenuType<T> registerExtended(
            ResourceLocation id,
            ExtendedScreenHandlerType.ExtendedFactory<T> factory
    ) {
        MenuType<T> type = new ExtendedScreenHandlerType<>(factory);
        return registerType(id, type);
    }


    static <T extends AbstractContainerMenu> MenuType<T> registerSimple(
            ResourceLocation id,
            BiFunction<Integer, Inventory, T> factory
    ) {
        final MenuType.MenuSupplier<T> supplier = (syncId, inventory) -> factory.apply(syncId, inventory);
        MenuType<T> type = new MenuType<>(supplier, FeatureFlagSet.of());
        return registerType(id, type);
    }

    @NotNull
    private static <T extends AbstractContainerMenu> MenuType<T> registerType(ResourceLocation id, MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, id, type);
    }

    public static void ensureStaticallyLoaded() {

    }
}
