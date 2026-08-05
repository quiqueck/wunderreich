package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhispererMenu;
import de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public class WunderreichMenuTypes {
    public static final MenuType<WhispererMenu> WHISPERER = registerSimple(
            Wunderreich.ID("whisperer"),
            WhispererMenu::new
    );
    
    public static final MenuType<SuctionTubeMenu> SUCTION_TUBE = registerSimple(
            Wunderreich.ID("suction_tube"),
            (containerId, inventory) -> {
                // Use player position as a fallback - in practice the block entity will provide the correct menu
                BlockPos pos = inventory.player.blockPosition();
                return new SuctionTubeMenu(containerId, inventory, pos);
            }
    );

    static <T extends AbstractContainerMenu> MenuType<T> registerSimple(
            Identifier id,
            BiFunction<Integer, Inventory, T> factory
    ) {
        final MenuType.MenuSupplier<T> supplier = (syncId, inventory) -> factory.apply(syncId, inventory);
        MenuType<T> type = new MenuType<>(supplier, FeatureFlagSet.of());
        return registerType(id, type);
    }

    @NotNull
    private static <T extends AbstractContainerMenu> MenuType<T> registerType(Identifier id, MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, id, type);
    }

    public static void ensureStaticallyLoaded() {

    }
}
