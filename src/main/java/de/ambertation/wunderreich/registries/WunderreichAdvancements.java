package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;

import net.minecraft.advancements.critereon.LocationTrigger;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class WunderreichAdvancements {
    public static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();
    public static LocationTrigger USE_TROWEL;


    public static void register() {
        USE_TROWEL = CriterionRegistry.register(new LocationTrigger(Wunderreich.ID("use_trowel")));

        AdvancementsJsonBuilder root = AdvancementsJsonBuilder
                .create("root")
                .startDisplay(
                        WunderreichBlocks.WHISPER_IMPRINTER.asItem(),
                        b -> b
                                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                                .showToast()
                                .visible()
                                .announceToChat()
                )
                .inventoryChangedCriteria("has_imprinter", WunderreichBlocks.WHISPER_IMPRINTER.asItem());
        root.register();

        AdvancementsJsonBuilder whisper_blank = AdvancementsJsonBuilder
                .create(WunderreichItems.BLANK_WHISPERER, b -> b.showToast().visible().announceToChat())
                .parent(root)
                .inventoryChangedCriteria("has_blank", WunderreichItems.BLANK_WHISPERER);
        whisper_blank.register();

        AdvancementsJsonBuilder whisperer = AdvancementsJsonBuilder
                .create(WunderreichItems.WHISPERER, b -> b.showToast().visible().announceToChat().goal())
                .parent(whisper_blank)
                .inventoryChangedCriteria("has_whisper", WunderreichItems.WHISPERER);
        whisperer.register();

//        AdvancementsJsonBuilder builders_trowel = AdvancementsJsonBuilder
//                .create(WunderreichItems.BUILDERS_TROWEL, b -> b.showToast().visible().announceToChat())
//                .parent(root)
//                .startCriteria("use_trowel", USE_TROWEL.getId().toString(), b -> {});
//        .inventoryChangedCriteria("has_trowel", WunderreichItems.BUILDERS_TROWEL);
//

        AdvancementsJsonBuilder builders_trowel = AdvancementsJsonBuilder
                .create("used_trowel")
                .startDisplay(WunderreichItems.BUILDERS_TROWEL, b -> b.showToast().visible().announceToChat())
                .parent(root)
                .startCriteria("use_trowel", USE_TROWEL.getId().toString(), b -> {});
        builders_trowel.register();
    }
}
