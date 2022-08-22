package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Transform;

import net.minecraft.client.renderer.debug.DebugRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TextRenderer {

    public static void render(Float3 pos, String content, int color) {
        DebugRenderer.renderFloatingText(content, pos.x, pos.y, pos.z, color);
    }

    public static void render(Float3 pos, int color) {
        DebugRenderer.renderFloatingText(pos.toString(), pos.x, pos.y, pos.z, color);
    }

    public static void render(Float3 pos, Float3 content, int color) {
        DebugRenderer.renderFloatingText(content.toString(), pos.x, pos.y, pos.z, color);
    }

    public static void render(Float3 pos, Bounds content, int color) {
        DebugRenderer.renderFloatingText(content.toString(), pos.x, pos.y, pos.z, color);
    }

    public static void render(Float3 pos, Transform content, int color) {
        DebugRenderer.renderFloatingText(content.toString(), pos.x, pos.y, pos.z, color);
    }
}
