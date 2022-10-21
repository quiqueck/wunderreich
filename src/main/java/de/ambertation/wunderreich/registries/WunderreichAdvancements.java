package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class WunderreichAdvancements {
    public static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();
    public static PlayerTrigger USE_TROWEL;
    public static PlayerTrigger OPEN_WUNDERKISTE;
    public static PlayerTrigger COLOR_WUNDERKISTE;


    public static void register() {
        USE_TROWEL = CriteriaTriggers.register(new PlayerTrigger(Wunderreich.ID("use_trowel")));
        OPEN_WUNDERKISTE = CriteriaTriggers.register(new PlayerTrigger(Wunderreich.ID("open_wunderkiste")));
        COLOR_WUNDERKISTE = CriteriaTriggers.register(new PlayerTrigger(Wunderreich.ID("color_wunderkiste")));

        Item rootItem = CreativeTabs.getBlockIcon().asItem();
        if (rootItem == Blocks.LAPIS_BLOCK.asItem()) rootItem = CreativeTabs.getItemIcon();

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WHISPER_IMPRINTER))
            rootItem = WunderreichBlocks.WHISPER_IMPRINTER.asItem();
        else if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL))
            rootItem = WunderreichItems.BUILDERS_TROWEL;
        else if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.DIAMOND_BUILDERS_TROWEL))
            rootItem = WunderreichItems.DIAMOND_BUILDERS_TROWEL;


        ResourceLocation root = AdvancementsJsonBuilder
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

        ResourceLocation whisper_blank = root;
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BLANK_WHISPERER)) {
            whisper_blank = AdvancementsJsonBuilder
                    .create(WunderreichItems.BLANK_WHISPERER, b -> b.showToast().visible().announceToChat())
                    .parent(root)
                    .inventoryChangedCriteria("has_blank", WunderreichItems.BLANK_WHISPERER)
                    .register();
        }
        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.WHISPERER)) {
            ResourceLocation whisperer = AdvancementsJsonBuilder
                    .create(WunderreichItems.WHISPERER, b -> b.showToast().visible().announceToChat().goal())
                    .parent(whisper_blank)
                    .inventoryChangedCriteria("has_whisper", WunderreichItems.WHISPERER)
                    .register();
        }


        if (Configs.ITEM_CONFIG.isEnabled(WunderreichItems.BUILDERS_TROWEL)) {
            ResourceLocation builders_trowel = AdvancementsJsonBuilder
                    .create("used_trowel")
                    .startDisplay(WunderreichItems.BUILDERS_TROWEL, b -> b.showToast().visible().announceToChat())
                    .parent(root)
                    .startCriteria("use_trowel", USE_TROWEL.getId().toString(), b -> {
                    }).register();
        }

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WUNDER_KISTE)) {
            assert WunderreichBlocks.WUNDER_KISTE != null;
            ResourceLocation opened_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_open")
                    .startDisplay(
                            WunderreichBlocks.WUNDER_KISTE.asItem(),
                            b -> b.showToast().visible().announceToChat()
                    )
                    .parent(root)
                    .startCriteria("open_wunderkiste", OPEN_WUNDERKISTE.getId().toString(), b -> {
                    }).register();

            ResourceLocation colored_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_color")
                    .startDisplay(
                            Items.RED_DYE,
                            b -> b.showToast().visible().announceToChat().goal()
                    )
                    .parent(opened_wunderkiste)
                    .startCriteria("color_wunderkiste", COLOR_WUNDERKISTE.getId().toString(), b -> {
                    }).register();
        }
    }
}
