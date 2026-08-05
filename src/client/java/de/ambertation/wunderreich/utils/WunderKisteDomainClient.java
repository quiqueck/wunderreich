package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Client-only sprite resolution for {@link WunderKisteDomain}, kept out of {@code WunderreichClient}
 * so the entrypoint stays just wiring. {@code WunderKisteDomain} itself only stores a plain
 * {@code textureKey}; this class is the only place that turns it into an actual {@link SpriteId}.
 */
public class WunderKisteDomainClient {
    private static final Map<String, SpriteId> WUNDERKISTE_MATERIALS = Maps.newHashMap();
    public static SpriteId WUNDER_KISTE_LOCATION = getWunderkisteColor("wunder_kiste");
    public static SpriteId WUNDER_KISTE_TOP_LOCATION = chestMaterial("wunder_kiste_top");
    public static SpriteId WUNDER_KISTE_MONOCHROME_TOP_LOCATION = chestMaterial("wunder_kiste_bw_top");

    private static SpriteId chestMaterial(String string) {
        return new SpriteId(Sheets.CHEST_SHEET, Wunderreich.ID("entity/chest/" + string));
    }

    public static SpriteId getWunderkisteColor(String name) {
        return WUNDERKISTE_MATERIALS.computeIfAbsent(name, WunderKisteDomainClient::chestMaterial);
    }

    public static SpriteId getSpriteFor(WunderKisteDomain domain) {
        if (Configs.MAIN.multiTexturedWunderkiste.get()) {
            return getWunderkisteColor(domain.textureKey);
        } else if (domain.useMonochromeFallback) {
            return getWunderkisteColor("wunder_kiste_bw");
        } else {
            return getWunderkisteColor("wunder_kiste");
        }
    }
}
