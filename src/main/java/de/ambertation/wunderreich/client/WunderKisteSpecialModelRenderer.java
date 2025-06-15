package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.joml.Vector3f;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WunderKisteSpecialModelRenderer implements SpecialModelRenderer<WunderKisteDomain> {
    private final ChestModel model;
    private final float openness;

    public WunderKisteSpecialModelRenderer(ChestModel chestModel, float openness) {
        this.model = chestModel;
        this.openness = openness;
    }

    @Override
    public void render(
            @Nullable WunderKisteDomain domain,
            ItemDisplayContext itemDisplayContext,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            int j,
            boolean bl
    ) {
        if (domain == null) domain = WunderKisteBlock.DEFAULT_DOMAIN;
        VertexConsumer vertexConsumer = domain.getMaterial().buffer(multiBufferSource, RenderType::entitySolid);
        this.model.setupAnim(this.openness);
        this.model.renderToBuffer(poseStack, vertexConsumer, i, j, domain.overlayColor);
    }

    @Override
    public @Nullable WunderKisteDomain extractArgument(ItemStack itemStack) {
        return WunderKisteItem.getDomain(itemStack);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(this.openness);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked(float openness) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<WunderKisteSpecialModelRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                            Codec.FLOAT.optionalFieldOf("openness", 0.0F)
                                                       .forGetter(WunderKisteSpecialModelRenderer.Unbaked::openness)
                                    )
                                    .apply(instance, WunderKisteSpecialModelRenderer.Unbaked::new)
        );

        public Unbaked() {
            this(0.0F);
        }

        @Override
        public MapCodec<WunderKisteSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            ChestModel chestModel = new ChestModel(entityModelSet.bakeLayer(ModelLayers.CHEST));
            return new WunderKisteSpecialModelRenderer(chestModel, this.openness);
        }
    }
}
