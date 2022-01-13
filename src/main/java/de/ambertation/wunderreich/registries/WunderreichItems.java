package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;

import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import ru.bclib.registry.ItemRegistry;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WunderreichItems {
    private static final ItemRegistry REGISTRY = new ItemRegistry(CreativeTabs.TAB_ITEMS,
            WunderreichConfigs.ITEM_CONFIG);

    public static Item WHISPERER = registerItem("whisperer",
            new TrainedVillagerWhisperer(),
            WunderreichConfigs.MAIN.allowLibrarianSelection.get());
    public static Item BLANK_WHISPERER = registerItem("whisperer_blank",
            new VillagerWhisperer(),
            WunderreichConfigs.MAIN.allowLibrarianSelection.get() || WunderreichConfigs.MAIN.cyclingNeedsWhisperer.get());

    public static Item BUILDERS_TROWEL = registerItem("builders_trowel",
            new BuildersTrowel(),
            WunderreichConfigs.MAIN.allowBuilderTools.get());

    @NotNull
    public static ItemRegistry getItemRegistry() {
        return REGISTRY;
    }

    public static FabricItemSettings makeItemSettings() {
        return getItemRegistry().makeItemSettings();
    }

    public static List<Item> getModItems() {
        return REGISTRY.getModItems(Wunderreich.MOD_ID);
    }

    public static Item registerItem(String name, Item item) {
        return getItemRegistry().register(Wunderreich.makeID(name), item);
    }

    public static Item registerItem(String name, Item item, boolean register) {
        if (register) {
            registerItem(name, item);
        }

        return item;
    }

    public static void register() {

    }
}
