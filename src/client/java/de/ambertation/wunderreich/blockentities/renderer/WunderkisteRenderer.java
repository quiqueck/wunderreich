package de.ambertation.wunderreich.blockentities.renderer;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichRules;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteDomainClient;
import de.ambertation.wunderreich.utils.WunderKisteServerExtension;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class WunderkisteRenderer
        implements BlockEntityRenderer<WunderKisteBlockEntity, WunderkisteRenderer.WunderKisteRenderState> {

    private static final Vertex[] TOP_PLANE = {
            new Vertex(2.0f / 16.0f, 10.001f / 16.0f, 2.0f / 16.0f, 13.0f / 16.0f, 13.0f / 16.0f),
            new Vertex(2.0f / 16.0f, 10.001f / 16.0f, 14.0f / 16.0f, 1.0f / 16.0f, 13.0f / 16.0f),
            new Vertex(14.0f / 16.0f, 10.001f / 16.0f, 14.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f),
            new Vertex(14.0f / 16.0f, 10.001f / 16.0f, 2.0f / 16.0f, 13.0f / 16.0f, 1.0f / 16.0f)
    };

    private final SpriteGetter sprites;
    private final MultiblockChestResources<ChestModel> models;

    public WunderkisteRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.models = ChestRenderer.LAYERS.map(layer -> new ChestModel(context.bakeLayer(layer)));
    }

    private static SpriteId getTopSprite(WunderKisteDomain d) {
        return d.useMonochromeFallback
                ? WunderKisteDomainClient.WUNDER_KISTE_MONOCHROME_TOP_LOCATION
                : WunderKisteDomainClient.WUNDER_KISTE_TOP_LOCATION;
    }

    @Override
    public WunderKisteRenderState createRenderState() {
        return new WunderKisteRenderState();
    }

    @Override
    public void extractRenderState(
            WunderKisteBlockEntity blockEntity,
            WunderKisteRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);

        final Level level = blockEntity.getLevel();
        final boolean renderInWorld = level != null;

        BlockState blockState = blockEntity.getBlockState();
        if (blockState == null) blockState = WunderreichBlocks.WUNDER_KISTE.defaultBlockState();
        if (!renderInWorld) blockState = blockState.setValue(ChestBlock.FACING, Direction.SOUTH);

        state.facing = blockState.getValue(ChestBlock.FACING);
        state.type = blockState.hasProperty(ChestBlock.TYPE)
                ? blockState.getValue(ChestBlock.TYPE)
                : ChestType.SINGLE;
        state.domain = WunderreichRules.Wunderkiste.showColors()
                ? WunderKisteServerExtension.getDomain(blockState)
                : WunderKisteBlock.DEFAULT_DOMAIN;

        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combineResult;
        if (renderInWorld && blockState.getBlock() instanceof AbstractChestBlock<?> abstractChestBlock) {
            combineResult = abstractChestBlock.combine(blockState, level, blockEntity.getBlockPos(), true);
        } else {
            combineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }

        state.open = ((Float2FloatFunction) combineResult.apply(ChestBlock.opennessCombiner(blockEntity))).get(partialTick);
        if (state.type != ChestType.SINGLE) {
            state.lightCoords = ((Int2IntFunction) combineResult.apply(new BrightnessCombiner())).applyAsInt(state.lightCoords);
        }
    }

    @Override
    public void submit(
            WunderKisteRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        final WunderKisteDomain domain = state.domain == null ? WunderKisteBlock.DEFAULT_DOMAIN : state.domain;

        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));

        float open = state.open;
        open = 1.0f - open;
        open = 1.0f - open * open * open;

        // Colored chest body: per-domain sprite, tinted with the domain overlay color.
        final ChestModel model = this.models.select(state.type);
        submitNodeCollector.submitModel(
                model,
                open,
                poseStack,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                domain.overlayColor,
                WunderKisteDomainClient.getSpriteFor(domain),
                this.sprites,
                0,
                state.breakProgress
        );

        // Colored top plane overlay (only visible while the lid is opening).
        if (open > 0) {
            submitTopPlane(state, poseStack, submitNodeCollector, domain);
        }

        poseStack.popPose();
    }

    private void submitTopPlane(
            WunderKisteRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            WunderKisteDomain domain
    ) {
        final SpriteId topId = getTopSprite(domain);
        final TextureAtlasSprite topSprite = this.sprites.get(topId);
        final RenderType renderType = RenderTypes.entitySolid(topId.atlasLocation());
        final int color = domain.color;
        final int light = state.lightCoords;

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            final Matrix4f matrix = pose.pose();
            final Vector3f normal = pose.transformNormal(0.0f, 1.0f, 0.0f, new Vector3f());
            for (Vertex v : TOP_PLANE) {
                Vector4f p = new Vector4f(v.pos.x(), v.pos.y(), v.pos.z(), 1.0f).mul(matrix);
                vertexConsumer.addVertex(
                        p.x(),
                        p.y(),
                        p.z(),
                        color,
                        topSprite.getU(v.u),
                        topSprite.getV(v.v),
                        OverlayTexture.NO_OVERLAY,
                        light,
                        normal.x(),
                        normal.y(),
                        normal.z()
                );
            }
        });
    }

    @Environment(value = EnvType.CLIENT)
    public static class WunderKisteRenderState extends BlockEntityRenderState {
        public ChestType type = ChestType.SINGLE;
        public Direction facing = Direction.SOUTH;
        public float open;
        @Nullable
        public WunderKisteDomain domain;
    }

    @Environment(value = EnvType.CLIENT)
    static class Vertex {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float f, float g, float h, float i, float j) {
            this.pos = new Vector3f(f, g, h);
            this.u = i;
            this.v = j;
        }
    }
}
