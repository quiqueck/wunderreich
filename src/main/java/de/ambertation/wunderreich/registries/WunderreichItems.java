package de.ambertation.wunderreich.registries;

import de.ambertation.lib.math.sdf.SDFUnion;
import de.ambertation.lib.math.sdf.shapes.Box;
import de.ambertation.lib.math.sdf.shapes.Cylinder;
import de.ambertation.lib.math.sdf.shapes.Sphere;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.BuildersTrowel;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.items.VillagerWhisperer;
import de.ambertation.wunderreich.items.construction.BluePrint;
import de.ambertation.wunderreich.items.construction.Ruler;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
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
            () -> new BuildersTrowel(Tiers.IRON),
            Configs.MAIN.allowBuilderTools.get()
    );

    public static Item DIAMOND_BUILDERS_TROWEL = registerItem(
            "diamond_builders_trowel",
            () -> new BuildersTrowel(Tiers.DIAMOND),
            Configs.MAIN.allowBuilderTools.get()
    );

    public static Item RULER = registerItem(
            "ruler",
            Ruler::new,
            Configs.MAIN.allowConstructionTools.get()
    );

    public static Item BLUE_PRINT = registerItem(
            "blueprint_empty",
            () -> new BluePrint(null),
            Configs.MAIN.allowConstructionTools.get()
    );

    public static Item BLUE_PRINT_SPHERE = registerItem(
            "blueprint_sphere",
            () -> new BluePrint(() -> new Sphere(Sphere.DEFAULT_TRANSFORM, 0)),
            Configs.MAIN.allowConstructionTools.get()
    );

    public static Item BLUE_PRINT_BOX = registerItem(
            "blueprint_box",
            () -> new BluePrint(() -> new Box(Box.DEFAULT_TRANSFORM, 0)),
            Configs.MAIN.allowConstructionTools.get()
    );

    public static Item BLUE_PRINT_CYLINDER = registerItem(
            "blueprint_cylinder",
            () -> new BluePrint(() -> new Cylinder(Cylinder.DEFAULT_TRANSFORM, 0)),
            Configs.MAIN.allowConstructionTools.get()
    );
    public static Item BLUE_PRINT_UNION = registerItem(
            "blueprint_union",
            () -> new BluePrint(() -> new SDFUnion(null, null)),
            Configs.MAIN.allowConstructionTools.get()
    );


    @NotNull
    public static FabricItemSettings makeItemSettings() {
        return (FabricItemSettings) new FabricItemSettings().tab(CreativeTabs.TAB_ITEMS);
    }

    public static Collection<Item> getAllItems() {
        return Configs.ITEM_CONFIG.getAllObjects();
    }

    public static Item registerItem(String name, Supplier<Item> itemSupply) {
        return registerItem(name, itemSupply, true);
    }

    public static Item registerItem(String name, Supplier<Item> itemSupply, boolean register) {
        boolean enabled = Configs.ITEM_CONFIG.booleanOrDefault(name).get();

        if (enabled && register) {
            Item item = itemSupply.get();
            final ResourceLocation id = Wunderreich.ID(name);
            if (item != Items.AIR) {
                //this ensures that the dynamic config contains a valid entry for this Item.
                Configs.ITEM_CONFIG.newBooleanFor(name, item);
                Registry.register(Registry.ITEM, id, item);
                ITEMS.add(item);
                processItem(id, item);
            }
            return item;
        }
        return null;
    }

    public static void processItem(ResourceLocation id, Item itm) {
        WunderreichTags.supplyForItem(itm);
    }

    public static void register() {

    }
}
