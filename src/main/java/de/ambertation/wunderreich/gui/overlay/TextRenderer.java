package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float2;
import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.math.Transform;
import de.ambertation.wunderlib.ui.layout.LineWithWidth;
import de.ambertation.wunderlib.ui.layout.components.MultiLineText;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class TextRenderer {
    public record TextItem(String content, Float3 pos, int color) {
        public void render(@NotNull RenderContext ctx) {
            DebugRenderer.renderFloatingText(
                    ctx.poseStack(),
                    ctx.bufferSource(),
                    content,
                    pos.x,
                    pos.y,
                    pos.z,
                    color
            );
        }
    }

    public static void render(@NotNull RenderContext ctx, Float3 pos, String content, int color) {
        ctx.pushText(content, pos, color);
    }

    public static void render(@NotNull RenderContext ctx, Float3 pos, int color) {
        ctx.pushText(
                pos.toString(),
                pos,
                color
        );
    }

    public static void render(@NotNull RenderContext ctx, Float3 pos, Float3 content, int color) {
        ctx.pushText(
                content.toString(),
                pos,
                color
        );
    }

    public static void render(@NotNull RenderContext ctx, Float3 pos, Bounds content, int color) {
        ctx.pushText(
                content.toString(),
                pos,
                color
        );
    }

    public static void render(@NotNull RenderContext ctx, Float3 pos, Transform content, int color) {
        ctx.pushText(
                content.toString(),
                pos,
                color
        );
    }


    public static float draw(GuiGraphics guiGraphics, Font font, Float2 pos, int maxWidth, Component c, int color) {
        c = MultiLineText.parse(c);
        ImmutableList<LineWithWidth> lines = font
                .split(c, maxWidth)
                .stream()
                .map((component) -> new LineWithWidth(component, font.width(component)))
                .collect(ImmutableList.toImmutableList());
        int x = (int) pos.x;
        int y = (int) pos.y;

        for (LineWithWidth line : lines) {
            guiGraphics.drawString(font, line.text(), x, y, color);
            y += font.lineHeight;
        }

        return y;
    }
}
