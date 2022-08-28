package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.util.FastColor;

public class SolidPrimitives {
    public static void renderSingleBlock(
            RenderContext ctx, BufferBuilder builder,
            BlockInfo nfo
    ) {
        renderSingleBlock(
                ctx,
                builder,
                nfo.pos,
                nfo.deflate,
                FastColor.ARGB32.red(nfo.color),
                FastColor.ARGB32.green(nfo.color),
                FastColor.ARGB32.blue(nfo.color),
                (int) (nfo.alpha * 0xFF)
        );
    }

    public static void renderSingleBlock(
            RenderContext ctx, BufferBuilder builder,
            Float3 pos, float deflate, int color, float alpha
    ) {
        renderSingleBlock(ctx, builder, pos, deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    public static void renderSingleBlock(
            RenderContext ctx, BufferBuilder builder,
            Float3 pos, float deflate, int r, int g, int b, int a
    ) {
        Matrix4f m = ctx.pose();
        Matrix3f rotation = ctx.normal();
        float lx = (float) (pos.x + ctx.worldToCamSpace.x) - 0.5f;
        float ly = (float) (pos.y + ctx.worldToCamSpace.y) - 0.5f;
        float lz = (float) (pos.z + ctx.worldToCamSpace.z) - 0.5f;
        float hx = lx + 1 - deflate;
        float hy = ly + 1 - deflate;
        float hz = lz + 1 - deflate;
        lx += deflate;
        ly += deflate;
        lz += deflate;
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, 0, -1).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();
        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 0, 1).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();
        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 0, -1, 0).endVertex();

        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, 0, 1, 0).endVertex();

        builder.vertex(m, lx, ly, hz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, hy, hz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, hy, lz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();
        builder.vertex(m, lx, ly, lz).color(r, g, b, a).normal(rotation, -1, 0, 0).endVertex();

        builder.vertex(m, hx, ly, hz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, ly, lz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, hy, lz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
        builder.vertex(m, hx, hy, hz).color(r, g, b, a).normal(rotation, 1, 0, 0).endVertex();
    }

    //-------------------------------------- 2D Shapes --------------------------------------
    public static void renderQuadXZ(
            RenderContext ctx, BufferBuilder builder,
            Float3 center, Float2 size,
            int color, float alpha
    ) {
        Float3 sz = size.div(2).xxy();
        renderQuad(
                ctx, builder,
                center.add(sz.mul(Float3.XZ_PLANE)),
                center.add(sz.mul(Float3.XmZ_PLANE)),
                center.add(sz.mul(Float3.mXmZ_PLANE)),
                center.add(sz.mul(Float3.mXZ_PLANE)),
                Float3.Y_AXIS,
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    public static void renderQuad(
            RenderContext ctx, BufferBuilder builder,
            Float3 p1, Float3 p2, Float3 p3, Float3 p4,
            Float3 normal,
            int r, int g, int b, int a
    ) {
        renderQuadCameraSpace(
                ctx, builder,
                p1.add(ctx.worldToCamSpace),
                p2.add(ctx.worldToCamSpace),
                p3.add(ctx.worldToCamSpace),
                p4.add(ctx.worldToCamSpace),
                normal,
                r, g, b, a
        );
    }

    private static void renderQuadCameraSpace(
            RenderContext ctx, BufferBuilder builder,
            Float3 p1, Float3 p2, Float3 p3, Float3 p4,
            Float3 normal,
            int r, int g, int b, int a
    ) {
        Matrix4f m = ctx.pose();
        Matrix3f rotation = ctx.normal();

        builder.vertex(m, (float) p1.x, (float) p1.y, (float) p1.z)
               .color(r, g, b, a)
               .normal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
               .endVertex();
        builder.vertex(m, (float) p2.x, (float) p2.y, (float) p2.z)
               .color(r, g, b, a)
               .normal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
               .endVertex();
        builder.vertex(m, (float) p3.x, (float) p3.y, (float) p3.z)
               .color(r, g, b, a)
               .normal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
               .endVertex();
        builder.vertex(m, (float) p4.x, (float) p4.y, (float) p4.z)
               .color(r, g, b, a)
               .normal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
               .endVertex();
    }
}
