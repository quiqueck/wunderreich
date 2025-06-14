package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class WunderreichItems {
    private static final List<Item> ITEMS = new ArrayList<>(3);

    public static Item WHISPERER = registerItem(
            "whisperer",
            TrainedVillagerWhisperer::new,
            Configs.MAIN.addImprintedWhispers.get()
    );
    public static Item BLANK_WHISPERER = registerItem(
            "whisperer_blank",
            VillagerWhisperer::new,
            Configs.MAIN.addBlankWhispere.get()
    );

    public static Item BUILDERS_TROWEL = registerItem(
            "builders_trowel",
            (key) -> new BuildersTrowel(ToolMaterial.IRON, key),
            Configs.MAIN.allowBuilderTools.get()
    );

    public static Item DIAMOND_BUILDERS_TROWEL = registerItem(
            "diamond_builders_trowel",
            (key) -> new BuildersTrowel(ToolMaterial.DIAMOND, key),
            Configs.MAIN.allowBuilderTools.get()
    );

    @NotNull
    public static Item.Properties makeItemSettings() {
        return new Item.Properties();
    }

    public static Collection<Item> getAllItems() {
        return Configs.ITEM_CONFIG.getAllObjects();
    }

    public static Item registerItem(String name, Function<ResourceKey<Item>, Item> itemSupply) {
        return registerItem(name, itemSupply, true);
    }

    public static Item registerItem(String name, Function<ResourceKey<Item>, Item> itemSupply, boolean register) {
        boolean enabled = Configs.ITEM_CONFIG.booleanOrDefault(name).get();

        if (enabled && register) {
            final ResourceLocation id = Wunderreich.ID(name);
            final ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
            Item item = itemSupply.apply(key);

            if (item != Items.AIR) {
                //this ensures that the dynamic config contains a valid entry for this Item.
                Configs.ITEM_CONFIG.newBooleanFor(name, item);
                Registry.register(BuiltInRegistries.ITEM, id, item);
                ITEMS.add(item);
                processItem(id, item);
            }
            return item;
        }
        return null;
    }

    public static void processItem(ResourceLocation id, Item itm) {

    }

    public static void register() {

    }
}
