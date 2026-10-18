package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Texture materials of the Wunderkiste. Kept out of {@link WunderreichClient} (whose static fields are initialised
 * as soon as the client entrypoint runs) and out of {@link WunderKisteDomain}'s constructor (which runs during block
 * registration): building a {@link Material} for the chest sheet references {@link Sheets}, and loading that class
 * before all mods registered their banner patterns / pottery sherds breaks those mods' textures (and makes
 * Supplementaries refuse to start, see quiqueck/wunderreich#18). This class is only loaded at render / atlas time.
 */
@Environment(EnvType.CLIENT)
public final class WunderKisteMaterials {
    private static final Map<String, Material> WUNDERKISTE_MATERIALS = Maps.newHashMap();
    public static final Material WUNDER_KISTE_LOCATION = getWunderkisteColor("wunder_kiste");
    public static final Material WUNDER_KISTE_TOP_LOCATION = chestMaterial("wunder_kiste_top");
    public static final Material WUNDER_KISTE_MONOCHROME_TOP_LOCATION = chestMaterial("wunder_kiste_bw_top");

    private WunderKisteMaterials() {
    }

    private static Material chestMaterial(String string) {
        return new Material(Sheets.CHEST_SHEET, Wunderreich.ID("entity/chest/" + string));
    }

    public static Material getWunderkisteColor(String name) {
        return WUNDERKISTE_MATERIALS.computeIfAbsent(name, WunderKisteMaterials::chestMaterial);
    }

    public static void getAllWunderkisteMaterials(Consumer<Material> consumer) {
        //make sure every domain resolved its (lazy) material before we report them
        for (WunderKisteDomain domain : WunderKisteDomain.values()) {
            domain.getMaterial();
        }
        for (Map.Entry<String, Material> entry : WUNDERKISTE_MATERIALS.entrySet()) {
            consumer.accept(entry.getValue());
        }
    }
}
