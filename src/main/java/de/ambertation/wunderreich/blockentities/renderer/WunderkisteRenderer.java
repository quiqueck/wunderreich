package de.ambertation.wunderreich.blockentities.renderer;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.client.WunderreichClient;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

@Environment(value = EnvType.CLIENT)
public class WunderkisteRenderer extends ChestRenderer<WunderKisteBlockEntity> {
    private static final String BOTTOM = "bottom";
    private static final String LID = "lid";
    private static final String LOCK = "lock";
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public WunderkisteRenderer(BlockEntityRendererProvider.Context context) {
        super(context);

        ModelPart modelPart = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelPart.getChild(BOTTOM);
        this.lid = modelPart.getChild(LID);
        this.lock = modelPart.getChild(LOCK);
    }

    @Override
    public void render(WunderKisteBlockEntity blockEntity,
                       float f,
                       PoseStack poseStack,
                       MultiBufferSource multiBufferSource,
                       int i,
                       int uv2) {
        final Level level = blockEntity.getLevel();
        final boolean renderInWorld = level != null;

        BlockState blockState = blockEntity.getBlockState();
        if (blockState == null) blockState = WunderreichBlocks.WUNDER_KISTE.defaultBlockState();
        if (!renderInWorld) blockState = blockState.setValue(ChestBlock.FACING, Direction.SOUTH);

        if ((blockState.getBlock() instanceof AbstractChestBlock abstractChestBlock)) {
            final WunderKisteDomain d = WunderKisteServerExtension.getDomain(blockState);

            poseStack.pushPose();
            float g = blockState.getValue(ChestBlock.FACING).toYRot();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
            poseStack.translate(-0.5, -0.5, -0.5);

            DoubleBlockCombiner.NeighborCombineResult<ChestBlockEntity> neighborCombineResult = renderInWorld
                    ? abstractChestBlock.combine(blockState, level, blockEntity.getBlockPos(), true)
                    : DoubleBlockCombiner.Combiner::acceptNone;
            float h = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(f);
            h = 1.0f - h;
            h = 1.0f - h * h * h;

            int overlayCoords = ((Int2IntFunction) neighborCombineResult.apply(new BrightnessCombiner())).applyAsInt(i);
            Material material = getMaterial(d);
            VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutout);

            this.render(poseStack,
                    vertexConsumer,
                    this.lid,
                    this.lock,
                    this.bottom,
                    h,
                    overlayCoords,
                    uv2,
                    FastColor.ARGB32.red(d.color) / (float) 0XFF,
                    FastColor.ARGB32.green(d.color) / (float) 0XFF,
                    FastColor.ARGB32.blue(d.color) / (float) 0XFF);
            poseStack.popPose();
        }
    }

    private static Material getMaterial(WunderKisteDomain d) {
        return d == WunderKisteDomain.WHITE || d == WunderKisteDomain.GRAY || d == WunderKisteDomain.LIGHT_GRAY || d == WunderKisteDomain.BLACK || d == WunderKisteDomain.BLUE || d == WunderKisteDomain.LIGHT_BLUE
                ? WunderreichClient.WUNDER_KISTE_LOCATION
                : WunderreichClient.WUNDER_KISTE_MONOCHROME_LOCATION;
    }

    private void render(PoseStack poseStack,
                        VertexConsumer vertexConsumer,
                        ModelPart modelPart,
                        ModelPart modelPart2,
                        ModelPart modelPart3,
                        float f,
                        int overlayCoords,
                        int uv2,
                        float r,
                        float g,
                        float b) {
        modelPart2.xRot = modelPart.xRot = -(f * 1.5707964f);

        modelPart.render(poseStack, vertexConsumer, overlayCoords, uv2, r, g, b, 0.0f);
        modelPart2.render(poseStack, vertexConsumer, overlayCoords, uv2, r, g, b, 0.0f);
        modelPart3.render(poseStack, vertexConsumer, overlayCoords, uv2, r, g, b, 1.0f);
    }
}
