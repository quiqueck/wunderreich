package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Transform;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LinePrimitives {

    //-------------------------------------- VERTEX HANDLING --------------------------------------
    private static void addVertex(
            RenderContext ctx,
            Float3 p,
            float nx, float ny, float nz,
            int r, int g, int b, int a
    ) {
        ctx.vertexConsumer.vertex(ctx.pose(), (float) p.x, (float) p.y, (float) p.z)
                          .color(r, g, b, a)
                          .normal(ctx.normal(), nx, ny, nz)
                          .endVertex();
    }

    private static void addVertex(
            RenderContext ctx,
            int corner,
            float nx, float ny, float nz,
            int r, int g, int b, int a,
            Float3[] corners
    ) {
        addVertex(ctx, corners[corner], nx, ny, nz, r, g, b, a);
    }


    //-------------------------------------- LINE --------------------------------------
    public static void addLine(
            RenderContext ctx,
            Bounds.Interpolate cornerStart, Bounds.Interpolate cornerEnd,
            int r, int g, int b, int a, Float3[] corners
    ) {
        Float3 start = corners[cornerStart.idx];
        Float3 end = corners[cornerEnd.idx];
        Float3 n = end.sub(start).normalized();

        addVertex(ctx, start, (float) n.x, (float) n.y, (float) n.z, r, g, b, a);
        addVertex(ctx, end, (float) n.x, (float) n.y, (float) n.z, r, g, b, a);
    }


    //-------------------------------------- SINGLE BLOCKS --------------------------------------
    public static void renderSingleBlock(
            RenderContext ctx,
            BlockPos pos,
            float deflate,
            int color,
            float alpha
    ) {
        final float x = (float) (pos.getX() + ctx.camPos.x);
        final float y = (float) (pos.getY() + ctx.camPos.y);
        final float z = (float) (pos.getZ() + ctx.camPos.z);
        renderCubeOutline(ctx,
                x + deflate, y + deflate, z + deflate,
                1 + x - deflate, 1 + y - deflate, 1 + z - deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    public static void renderSingleBlock(RenderContext ctx, Float3 pos, float deflate, int color, float alpha) {
        final float x = (float) (pos.x + ctx.camPos.x) - 0.5f;
        final float y = (float) (pos.y + ctx.camPos.y) - 0.5f;
        final float z = (float) (pos.z + ctx.camPos.z) - 0.5f;
        renderCubeOutline(ctx,
                x + deflate, y + deflate, z + deflate,
                1 + x - deflate, 1 + y - deflate, 1 + z - deflate,
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    //-------------------------------------- BoundingBox --------------------------------------
    public static void renderBounds(RenderContext ctx, Bounds bounds, float deflate, int color, float alpha) {
        renderCubeOutline(
                ctx,
                (float) (bounds.min.x + ctx.camPos.x) + deflate - 0.5f,
                (float) (bounds.min.y + ctx.camPos.y) + deflate - 0.5f,
                (float) (bounds.min.z + ctx.camPos.z) + deflate - 0.5f,
                (float) (bounds.max.x + ctx.camPos.x) - deflate + 0.5f,
                (float) (bounds.max.y + ctx.camPos.y) - deflate + 0.5f,
                (float) (bounds.max.z + ctx.camPos.z) - deflate + 0.5f,
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private static void renderCubeOutline(
            RenderContext ctx,
            float lx, float ly, float lz,
            float hx, float hy, float hz,
            int r, int g, int b, int a
    ) {
        final Matrix4f pose = ctx.pose();
        final Matrix3f normal = ctx.normal();

        ctx.vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, ly, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, lx, hy, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, ly, hz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, lz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        ctx.vertexConsumer.vertex(pose, hx, hy, hz).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
    }


    //-------------------------------------- TRANSFORMS --------------------------------------
    public static void renderTransform(RenderContext ctx, Transform t, int color, float alpha) {
        renderTransform(ctx, t, FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                (int) (alpha * 0xFF)
        );
    }

    private static void renderTransform(RenderContext ctx, Transform t, int r, int g, int b, int a) {
        Float3[] corners = t.translate(ctx.camPos).getCornersInWorldSpace(false);

        DebugRenderer.renderFloatingText(
                t.toString(),
                corners[0].x - ctx.camPos.x,
                corners[0].y - 0.1 - ctx.camPos.y,
                corners[0].z - ctx.camPos.z,
                OverlayRenderer.COLOR_BOUNDING_BOX
        );

        addLine(ctx,
                Bounds.Interpolate.MIN_MIN_MIN,
                Bounds.Interpolate.MAX_MIN_MIN,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MIN_MIN,
                Bounds.Interpolate.MAX_MIN_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MIN_MAX,
                Bounds.Interpolate.MIN_MIN_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MIN_MIN_MAX,
                Bounds.Interpolate.MIN_MIN_MIN,
                r, g, b, a, corners
        );


        addLine(ctx,
                Bounds.Interpolate.MIN_MAX_MIN,
                Bounds.Interpolate.MAX_MAX_MIN,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MAX_MIN,
                Bounds.Interpolate.MAX_MAX_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MAX_MAX,
                Bounds.Interpolate.MIN_MAX_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MIN_MAX_MAX,
                Bounds.Interpolate.MIN_MAX_MIN,
                r, g, b, a, corners
        );


        addLine(ctx,
                Bounds.Interpolate.MIN_MIN_MIN,
                Bounds.Interpolate.MIN_MAX_MIN,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MIN_MIN_MAX,
                Bounds.Interpolate.MIN_MAX_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MIN_MAX,
                Bounds.Interpolate.MAX_MAX_MAX,
                r, g, b, a, corners
        );
        addLine(ctx,
                Bounds.Interpolate.MAX_MIN_MIN,
                Bounds.Interpolate.MAX_MAX_MIN,
                r, g, b, a, corners
        );
    }
}
