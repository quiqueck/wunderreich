package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class BlockEntityWithoutLevelRendererMixin {
    private final Map<WunderKisteDomain, WunderKisteBlockEntity> wunderKisten = Maps.newHashMap();
    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    public void wunderreich_render(ItemStack itemStack,
                                   TransformType transformType,
                                   PoseStack poseStack,
                                   MultiBufferSource multiBufferSource,
                                   int i,
                                   int j,
                                   CallbackInfo ci) {
        Item item = itemStack.getItem();
        if (item instanceof WunderKisteItem) {
            WunderKisteBlockEntity wunderKiste = wunderKisten.computeIfAbsent(
                    WunderKisteItem.getDomain(itemStack),
                    (domain) -> new WunderKisteBlockEntity(BlockPos.ZERO,
                            WunderreichBlocks.WUNDER_KISTE
                                    .defaultBlockState()
                                    .setValue(WunderKisteBlock.DOMAIN, domain))
            );

            this.blockEntityRenderDispatcher.renderItem(wunderKiste, poseStack, multiBufferSource, i, j);
            ci.cancel();
        }
    }
}
