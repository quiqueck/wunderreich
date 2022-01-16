package de.ambertation.wunderreich.mixin.client;

import de.ambertation.wunderreich.blockentities.WunderKisteBlockEntity;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class BlockEntityWithoutLevelRendererMixin {
    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Shadow
    @Final
    private EnderChestBlockEntity enderChest;

    private WunderKisteBlockEntity wunderKiste;

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    public void wunderreich_render(ItemStack itemStack,
                                   TransformType transformType,
                                   PoseStack poseStack,
                                   MultiBufferSource multiBufferSource,
                                   int i,
                                   int j,
                                   CallbackInfo ci) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof WunderKisteBlock) {
                if (wunderKiste == null) {
                    wunderKiste = new WunderKisteBlockEntity(BlockPos.ZERO,
                                                             WunderreichBlocks.WUNDER_KISTE.defaultBlockState());
                }
                this.blockEntityRenderDispatcher.renderItem(wunderKiste, poseStack, multiBufferSource, i, j);
                ci.cancel();
            }
        }
    }
}
