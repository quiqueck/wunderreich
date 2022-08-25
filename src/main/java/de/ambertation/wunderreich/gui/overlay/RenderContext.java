package de.ambertation.wunderreich.gui.overlay;

import de.ambertation.lib.math.Float3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RenderContext {
    private PoseStack poseStack;
    public VertexConsumer vertexConsumer;
    public Vec3 worldToCamSpace;
    public Float3 camToWorldSpace;
    private Matrix4f pose;
    private Matrix3f normal;


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

    public void invalidate() {
        vertexConsumer = null;
        setPoseStack(null);
    }
}
