package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.advancements.critereon.LocationTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class WunderreichAdvancements {
    public static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();
    public static LocationTrigger USE_TROWEL;
    public static LocationTrigger OPEN_WUNDERKISTE;
    public static LocationTrigger COLOR_WUNDERKISTE;


    public static void register() {
        USE_TROWEL = CriterionRegistry.register(new LocationTrigger(Wunderreich.ID("use_trowel")));
        OPEN_WUNDERKISTE = CriterionRegistry.register(new LocationTrigger(Wunderreich.ID("open_wunderkiste")));
        COLOR_WUNDERKISTE = CriterionRegistry.register(new LocationTrigger(Wunderreich.ID("color_wunderkiste")));

        ResourceLocation root = AdvancementsJsonBuilder
                .create("root")
                .startDisplay(
                        WunderreichBlocks.WHISPER_IMPRINTER.asItem(),
                        b -> b
                                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                                .showToast()
                                .visible()
                                .announceToChat()
                             )
                .inventoryChangedCriteria("has_imprinter", WunderreichBlocks.WHISPER_IMPRINTER.asItem())
                .register();

        ResourceLocation whisper_blank = AdvancementsJsonBuilder
                .create(WunderreichItems.BLANK_WHISPERER, b -> b.showToast().visible().announceToChat())
                .parent(root)
                .inventoryChangedCriteria("has_blank", WunderreichItems.BLANK_WHISPERER)
                .register();

        ResourceLocation whisperer = AdvancementsJsonBuilder
                .create(WunderreichItems.WHISPERER, b -> b.showToast().visible().announceToChat().goal())
                .parent(whisper_blank)
                .inventoryChangedCriteria("has_whisper", WunderreichItems.WHISPERER)
                .register();

        ResourceLocation builders_trowel = AdvancementsJsonBuilder
                .create("used_trowel")
                .startDisplay(WunderreichItems.BUILDERS_TROWEL, b -> b.showToast().visible().announceToChat())
                .parent(root)
                .startCriteria("use_trowel", USE_TROWEL.getId().toString(), b -> {
                }).register();

        if (Configs.BLOCK_CONFIG.isEnabled(WunderreichBlocks.WUNDER_KISTE)) {
            ResourceLocation opened_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_open")
                    .startDisplay(WunderreichBlocks.WUNDER_KISTE.asItem(),
                            b -> b.showToast().visible().announceToChat())
                    .parent(root)
                    .startCriteria("open_wunderkiste", OPEN_WUNDERKISTE.getId().toString(), b -> { }).register();

            ResourceLocation colored_wunderkiste = AdvancementsJsonBuilder
                    .create("wunderkiste_color")
                    .startDisplay(Items.RED_DYE,
                            b -> b.showToast().visible().announceToChat().goal())
                    .parent(opened_wunderkiste)
                    .startCriteria("color_wunderkiste", COLOR_WUNDERKISTE.getId().toString(), b -> { }).register();
        }
    }
}
