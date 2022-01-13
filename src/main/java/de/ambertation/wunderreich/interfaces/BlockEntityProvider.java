package de.ambertation.wunderreich.interfaces;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface BlockEntityProvider<E extends BlockEntity> {
    BlockEntityType<E> getBlockEntityType();

    @Environment(EnvType.CLIENT)
    BlockEntityRendererProvider<? super E> getBlockEntityRenderProvider();
}
