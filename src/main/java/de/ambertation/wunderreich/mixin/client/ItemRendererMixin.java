package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.items.WunderKisteItem;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.Maps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    private final Map<WunderKisteDomain, WunderKisteBlockEntity> wunderKisten = Maps.newHashMap();

    @Inject(method = "renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V", at = @At("HEAD"), cancellable = true)
    public void wunderreich_renderStatic(
            ItemStack itemStack,
            ItemDisplayContext itemDisplayContext,
            int i,
            int j,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            Level level,
            int k,
            CallbackInfo ci
    ) {
        Item item = itemStack.getItem();
        if (item instanceof WunderKisteItem) {
            WunderKisteBlockEntity wunderKiste = wunderKisten.computeIfAbsent(
                    WunderKisteItem.getDomain(itemStack),
                    (domain) -> new WunderKisteBlockEntity(
                            BlockPos.ZERO,
                            WunderreichBlocks.WUNDER_KISTE
                                    .defaultBlockState()
                                    .setValue(WunderKisteBlock.DOMAIN, domain)
                    )
            );

            BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            // Get the renderer for our WunderKiste block entity
            BlockEntityRenderer<WunderKisteBlockEntity> renderer = dispatcher.getRenderer(wunderKiste);
            if (renderer != null) {
                renderer.render(wunderKiste, 0.0f, poseStack, multiBufferSource, i, j, Vec3.ZERO);
            }
            ci.cancel();
        }
    }
}
