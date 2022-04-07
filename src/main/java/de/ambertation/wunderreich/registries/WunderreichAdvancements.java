package de.ambertation.wunderreich.registries;

import com.google.gson.JsonElement;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WunderreichAdvancements {
    public static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();
    public static PlayerTrigger USE_TROWEL;


    public static void register() {
        USE_TROWEL = CriteriaTriggers.register(new PlayerTrigger(Wunderreich.ID("use_trowel")));

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
    }
}
