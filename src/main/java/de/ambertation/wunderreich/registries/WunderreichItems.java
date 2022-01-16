package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WunderreichItems {
    private static final List<Item> ITEMS = new ArrayList<>(3);

    public static Item WHISPERER = registerItem("whisperer",
                                                new TrainedVillagerWhisperer(),
                                                Configs.MAIN.allowLibrarianSelection.get());
    public static Item BLANK_WHISPERER = registerItem("whisperer_blank",
                                                      new VillagerWhisperer(),
                                                      Configs.MAIN.allowLibrarianSelection.get() || Configs.MAIN.cyclingNeedsWhisperer.get());

    public static Item BUILDERS_TROWEL = registerItem("builders_trowel",
                                                      new BuildersTrowel(),
                                                      Configs.MAIN.allowBuilderTools.get());

    @NotNull
    public static FabricItemSettings makeItemSettings() {
        return (FabricItemSettings) new FabricItemSettings().tab(CreativeTabs.TAB_ITEMS);
    }

    public static Collection<Item> getAllItems() {
        return Configs.ITEM_CONFIG.getAllObjects();
    }

    public static Item registerItem(String name, Item item) {
        return registerItem(name, item, true);
    }

    public static Item registerItem(String name, Item item, boolean register) {
        //this ensures that the dynamic config contains a valid entry for this Item.
        boolean enabled = Configs.ITEM_CONFIG.newBooleanFor(name, item).get();

        if (enabled && register) {
            final ResourceLocation id = Wunderreich.ID(name);
            if (item != Items.AIR) {
                Registry.register(Registry.ITEM, id, item);
                ITEMS.add(item);
                processItem(id, item);
            }
        }

        return item;
    }

    public static void processItem(ResourceLocation id, Item itm) {
        WunderreichTags.supplyForItem(itm);
    }

    public static void register() {

    }
}
