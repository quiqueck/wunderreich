package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.wunderlib.math.Float2;
import de.ambertation.wunderlib.math.Float3;
import de.ambertation.wunderlib.utils.ColorUtilARGB32;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;

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
                ColorUtilARGB32.color(nfo.color, (int) (nfo.alpha * 0xFF))
        );
    }

    public static void renderSingleBlock(
            RenderContext ctx, BufferBuilder builder,
            Float3 pos, float deflate, int color, float alpha
    ) {
        renderSingleBlock(
                ctx, builder, pos, deflate,
                ColorUtilARGB32.color(color, (int) (alpha * 0xFF))
        );
    }

    public static void renderSingleBlock(
            RenderContext ctx, BufferBuilder builder,
            Float3 pos, float deflate, int color
    ) {
        Matrix4f m = ctx.pose();
        PoseStack.Pose rotation = ctx.normal();
        float lx = (float) (pos.x + ctx.worldToCamSpace.x) - 0.5f;
        float ly = (float) (pos.y + ctx.worldToCamSpace.y) - 0.5f;
        float lz = (float) (pos.z + ctx.worldToCamSpace.z) - 0.5f;
        float hx = lx + 1 - deflate;
        float hy = ly + 1 - deflate;
        float hz = lz + 1 - deflate;
        lx += deflate;
        ly += deflate;
        lz += deflate;
        builder.addVertex(m, lx, ly, lz).setColor(color).setNormal(rotation, 0, 0, -1);
        builder.addVertex(m, lx, hy, lz).setColor(color).setNormal(rotation, 0, 0, -1);
        builder.addVertex(m, hx, hy, lz).setColor(color).setNormal(rotation, 0, 0, -1);
        builder.addVertex(m, hx, ly, lz).setColor(color).setNormal(rotation, 0, 0, -1);

        builder.addVertex(m, lx, ly, hz).setColor(color).setNormal(rotation, 0, 0, 1);
        builder.addVertex(m, hx, ly, hz).setColor(color).setNormal(rotation, 0, 0, 1);
        builder.addVertex(m, hx, hy, hz).setColor(color).setNormal(rotation, 0, 0, 1);
        builder.addVertex(m, lx, hy, hz).setColor(color).setNormal(rotation, 0, 0, 1);

        builder.addVertex(m, lx, ly, hz).setColor(color).setNormal(rotation, 0, -1, 0);
        builder.addVertex(m, lx, ly, lz).setColor(color).setNormal(rotation, 0, -1, 0);
        builder.addVertex(m, hx, ly, lz).setColor(color).setNormal(rotation, 0, -1, 0);
        builder.addVertex(m, hx, ly, hz).setColor(color).setNormal(rotation, 0, -1, 0);

        builder.addVertex(m, lx, hy, hz).setColor(color).setNormal(rotation, 0, 1, 0);
        builder.addVertex(m, hx, hy, hz).setColor(color).setNormal(rotation, 0, 1, 0);
        builder.addVertex(m, hx, hy, lz).setColor(color).setNormal(rotation, 0, 1, 0);
        builder.addVertex(m, lx, hy, lz).setColor(color).setNormal(rotation, 0, 1, 0);

        builder.addVertex(m, lx, ly, hz).setColor(color).setNormal(rotation, -1, 0, 0);
        builder.addVertex(m, lx, hy, hz).setColor(color).setNormal(rotation, -1, 0, 0);
        builder.addVertex(m, lx, hy, lz).setColor(color).setNormal(rotation, -1, 0, 0);
        builder.addVertex(m, lx, ly, lz).setColor(color).setNormal(rotation, -1, 0, 0);

        builder.addVertex(m, hx, ly, hz).setColor(color).setNormal(rotation, 1, 0, 0);
        builder.addVertex(m, hx, ly, lz).setColor(color).setNormal(rotation, 1, 0, 0);
        builder.addVertex(m, hx, hy, lz).setColor(color).setNormal(rotation, 1, 0, 0);
        builder.addVertex(m, hx, hy, hz).setColor(color).setNormal(rotation, 1, 0, 0);
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
                ColorUtilARGB32.color(color, (int) (alpha * 0xFF))
        );
    }

    public static void renderQuad(
            RenderContext ctx, BufferBuilder builder,
            Float3 p1, Float3 p2, Float3 p3, Float3 p4,
            Float3 normal,
            int color
    ) {
        renderQuadCameraSpace(
                ctx, builder,
                p1.add(ctx.worldToCamSpace),
                p2.add(ctx.worldToCamSpace),
                p3.add(ctx.worldToCamSpace),
                p4.add(ctx.worldToCamSpace),
                normal,
                color
        );
    }

    private static void renderQuadCameraSpace(
            RenderContext ctx, BufferBuilder builder,
            Float3 p1, Float3 p2, Float3 p3, Float3 p4,
            Float3 normal,
            int color
    ) {
        Matrix4f m = ctx.pose();
        PoseStack.Pose rotation = ctx.normal();

        builder.addVertex(m, (float) p1.x, (float) p1.y, (float) p1.z)
               .setColor(color)
               .setNormal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
        ;
        builder.addVertex(m, (float) p2.x, (float) p2.y, (float) p2.z)
               .setColor(color)
               .setNormal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
        ;
        builder.addVertex(m, (float) p3.x, (float) p3.y, (float) p3.z)
               .setColor(color)
               .setNormal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
        ;
        builder.addVertex(m, (float) p4.x, (float) p4.y, (float) p4.z)
               .setColor(color)
               .setNormal(rotation, (float) normal.x, (float) normal.y, (float) normal.z)
        ;
    }
}
