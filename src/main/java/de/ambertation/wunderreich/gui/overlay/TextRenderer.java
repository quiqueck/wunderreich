package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.ui.layout.LineWithWidth;
import de.ambertation.lib.ui.layout.components.MultiLineText;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.ImmutableList;

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


    public static void draw(PoseStack stack, Font font, Float2 pos, int maxWidth, Component c, int color) {
        c = MultiLineText.parse(c);
        ImmutableList<LineWithWidth> lines = font
                .split(c, maxWidth)
                .stream()
                .map((component) -> new LineWithWidth(component, font.width(component)))
                .collect(ImmutableList.toImmutableList());
        float x = (float) pos.x;
        float y = (float) pos.y;

        for (LineWithWidth line : lines) {
            font.drawShadow(stack, line.text(), x, y, color);
            y += font.lineHeight;
        }

    }
}
