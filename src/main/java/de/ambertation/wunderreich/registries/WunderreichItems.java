package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.MainConfig;
import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import ru.bclib.registry.ItemRegistry;

import java.util.List;

public class WunderreichItems {
    private static final ItemRegistry REGISTRY = new ItemRegistry(CreativeTabs.TAB_ITEMS, Configs.ITEM_CONFIG);

    public static Item WHISPERER = registerItem("whisperer", new TrainedVillagerWhisperer(), Configs.MAIN.get(MainConfig.ALLOW_LIBRARIAN_SELECTION));
    public static Item BLANK_WHISPERER = registerItem("whisperer_blank", new VillagerWhisperer(), Configs.MAIN.get(MainConfig.ALLOW_LIBRARIAN_SELECTION));
    public static Item BUILDERS_TROWEL = registerItem("builders_trowel", new BuildersTrowel(), Configs.MAIN.get(MainConfig.ALLOW_BUILDER_TOOLS));

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
