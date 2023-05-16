package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.wunderlib.math.Float3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.LinkedList;
import java.util.List;


@Environment(EnvType.CLIENT)
public class RenderContext {
    private PoseStack poseStack;
    private MultiBufferSource bufferSource;
    public VertexConsumer vertexConsumer;
    public Vec3 worldToCamSpace;
    public Float3 camToWorldSpace;
    private Matrix4f pose;
    private Matrix3f normal;
    private List<TextRenderer.TextItem> textItems = new LinkedList<>();


    public PoseStack poseStack() {
        return poseStack;
    }

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
        if (poseStack != null) {
            this.pose = poseStack.last().pose();
            this.normal = poseStack.last().normal();
        } else {
            this.pose = null;
            this.normal = null;
        }
    }

    public MultiBufferSource bufferSource() {
        return bufferSource;
    }

    public void setBufferSource(MultiBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public void setCamera(Camera camera) {
        worldToCamSpace = camera.getPosition().reverse();
        camToWorldSpace = Float3.of(camera.getPosition());
    }

    public Matrix4f pose() {
        return pose;
    }

    public Matrix3f normal() {
        return normal;
    }

    public void pushText(String content, Float3 pos, int color) {
        textItems.add(new TextRenderer.TextItem(content, pos, color));
    }

    public void pushText(String content, double x, double y, double z, int color) {
        textItems.add(new TextRenderer.TextItem(content, Float3.of(x, y, z), color));
    }

    public void renderAllText() {
        textItems.forEach(itm -> itm.render(this));
        textItems.clear();
    }

    public void invalidate() {
        vertexConsumer = null;
        setPoseStack(null);
        setBufferSource(null);
        textItems.clear();
    }
}
