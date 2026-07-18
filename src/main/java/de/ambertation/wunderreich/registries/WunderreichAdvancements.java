package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WunderreichAdvancements {
    public static final Map<Identifier, JsonElement> ADVANCEMENTS = new HashMap<>();

    public static final Identifier USE_TROWEL_ID = Wunderreich.ID("use_trowel");
    public static final Identifier OPEN_WUNDERKISTE_ID = Wunderreich.ID("open_wunderkiste");
    public static final Identifier COLOR_WUNDERKISTE_ID = Wunderreich.ID("color_wunderkiste");
    public static final Identifier TRANSPORTED_ITEM_ID = Wunderreich.ID("transport_item");

    public static PlayerTrigger USE_TROWEL;
    public static PlayerTrigger OPEN_WUNDERKISTE;
    public static PlayerTrigger COLOR_WUNDERKISTE;
    public static PlayerTrigger TRANSPORTED_ITEM;

    public static Criterion<PlayerTrigger.TriggerInstance> USE_TROWEL_CRITERION;
    public static Criterion<PlayerTrigger.TriggerInstance> OPEN_WUNDERKISTE_CRITERION;
    public static Criterion<PlayerTrigger.TriggerInstance> COLOR_WUNDERKISTE_CRITERION;
    public static Criterion<PlayerTrigger.TriggerInstance> TRANSPORTED_ITEM_CRITERION;

    public static void register() {
        USE_TROWEL = register(USE_TROWEL_ID, new PlayerTrigger());
        USE_TROWEL_CRITERION = USE_TROWEL.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));

        OPEN_WUNDERKISTE = register(OPEN_WUNDERKISTE_ID, new PlayerTrigger());
        OPEN_WUNDERKISTE_CRITERION = OPEN_WUNDERKISTE.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));

        COLOR_WUNDERKISTE = register(COLOR_WUNDERKISTE_ID, new PlayerTrigger());
        COLOR_WUNDERKISTE_CRITERION = COLOR_WUNDERKISTE.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));

        TRANSPORTED_ITEM = register(TRANSPORTED_ITEM_ID, new PlayerTrigger());
        TRANSPORTED_ITEM_CRITERION = TRANSPORTED_ITEM.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));

        Item rootItem = CreativeTabs.getBlockIcon().asItem();
        if (rootItem == Blocks.LAPIS_BLOCK.asItem()) rootItem = CreativeTabs.getItemIcon();

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WHISPER_IMPRINTER))
            rootItem = WunderreichBlocks.WHISPER_IMPRINTER.asItem();
        else if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL))
            rootItem = WunderreichItems.BUILDERS_TROWEL;
        else if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.DIAMOND_BUILDERS_TROWEL))
            rootItem = WunderreichItems.DIAMOND_BUILDERS_TROWEL;


        Identifier root = AdvancementsJsonBuilder
                .create("root")
                .startDisplay(
                        rootItem,
                        b -> b
                                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                                .showToast()
                                .visible()
                                .announceToChat()
                )
                .inventoryChangedCriteria("has_imprinter", rootItem)
                .register();

        Identifier whisper_blank = root;
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BLANK_WHISPERER)) {
            whisper_blank = AdvancementsJsonBuilder
                    .create(WunderreichItems.BLANK_WHISPERER, b -> b.showToast().visible().announceToChat())
                    .parent(root)
                    .inventoryChangedCriteria("has_blank", WunderreichItems.BLANK_WHISPERER)
                    .register();
        }
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.WHISPERER)) {
            Identifier whisperer = AdvancementsJsonBuilder
                    .create(WunderreichItems.WHISPERER, b -> b.showToast().visible().announceToChat().goal())
                    .parent(whisper_blank)
                    .inventoryChangedCriteria("has_whisper", WunderreichItems.WHISPERER)
                    .register();
        }


        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL)) {
            Identifier builders_trowel = AdvancementsJsonBuilder
                    .create("used_trowel")
                    .startDisplay(WunderreichItems.BUILDERS_TROWEL, b -> b.showToast().visible().announceToChat())
                    .parent(root)
                    .startCriteria(
                            "use_trowel", USE_TROWEL_ID.toString(), b -> {
                            }
                    )
                    .register();
        }

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WUNDER_KISTE)) {
            assert WunderreichBlocks.WUNDER_KISTE != null;
            Identifier opened_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_open")
                    .startDisplay(
                            WunderreichBlocks.WUNDER_KISTE.asItem(),
                            b -> b.showToast().visible().announceToChat()
                    )
                    .parent(root)
                    .startCriteria(
                            "open_wunderkiste", OPEN_WUNDERKISTE_ID.toString(), b -> {
                            }
                    ).register();

            Identifier colored_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_color")
                    .startDisplay(
                            Items.RED_DYE,
                            b -> b.showToast().visible().announceToChat().goal()
                    )
                    .parent(opened_wunderkiste)
                    .startCriteria(
                            "color_wunderkiste", COLOR_WUNDERKISTE_ID.toString(), b -> {
                            }
                    ).register();
        }

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.SUCTION_TUBE)) {
            assert WunderreichBlocks.SUCTION_TUBE != null;

            Identifier held_suction_tube = AdvancementsJsonBuilder
                    .create("suction_tube")
                    .startDisplay(
                            WunderreichBlocks.SUCTION_TUBE.asItem(),
                            b -> b.showToast().visible().announceToChat()
                    )
                    .parent(root)
                    .inventoryChangedCriteria("has_suction_tube", WunderreichBlocks.SUCTION_TUBE.asItem())
                    .register();

            AdvancementsJsonBuilder
                    .create("item_transported")
                    .startDisplay(
                            Blocks.HOPPER.asItem(),
                            b -> b.showToast().visible().announceToChat().challenge()
                    )
                    .parent(held_suction_tube)
                    .startCriteria(
                            "transported_item", TRANSPORTED_ITEM_ID.toString(), b -> {
                            }
                    ).register();
        }
    }

    public static <T extends CriterionTrigger<?>> T register(Identifier id, T trigger) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
    }
}
