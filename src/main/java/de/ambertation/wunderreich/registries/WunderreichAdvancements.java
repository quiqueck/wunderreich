package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class WunderreichAdvancements {
    public static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();

    public static void register() {
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
                .parent(root)
                .inventoryChangedCriteria("has_whisper", WunderreichItems.WHISPERER);
        whisperer.register();
    }
}
