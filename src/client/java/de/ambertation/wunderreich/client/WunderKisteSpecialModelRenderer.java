package de.ambertation.wunderreich.client;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.utils.WunderKisteDomain;
import de.ambertation.wunderreich.utils.WunderKisteDomainClient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.joml.Vector3fc;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WunderKisteSpecialModelRenderer implements SpecialModelRenderer<WunderKisteDomain> {
    private final SpriteGetter sprites;
    private final ChestModel model;
    private final float openness;

    public WunderKisteSpecialModelRenderer(SpriteGetter sprites, ChestModel chestModel, float openness) {
        this.sprites = sprites;
        this.model = chestModel;
        this.openness = openness;
    }

    @Override
    public void submit(
            @Nullable WunderKisteDomain domain,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        if (domain == null) domain = WunderKisteBlock.DEFAULT_DOMAIN;
        SpriteId sprite = WunderKisteDomainClient.getSpriteFor(domain);
        submitNodeCollector.submitModel(
                this.model,
                this.openness,
                poseStack,
                lightCoords,
                overlayCoords,
                domain.overlayColor,
                sprite,
                this.sprites,
                outlineColor,
                null
        );
    }

    @Override
    public @Nullable WunderKisteDomain extractArgument(ItemStack itemStack) {
        return WunderKisteItem.getDomain(itemStack);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(this.openness);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked(float openness) implements SpecialModelRenderer.Unbaked<WunderKisteDomain> {
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
        public SpecialModelRenderer<WunderKisteDomain> bake(SpecialModelRenderer.BakingContext context) {
            ChestModel chestModel = new ChestModel(context.entityModelSet().bakeLayer(ModelLayers.CHEST));
            return new WunderKisteSpecialModelRenderer(context.sprites(), chestModel, this.openness);
        }
    }
}
