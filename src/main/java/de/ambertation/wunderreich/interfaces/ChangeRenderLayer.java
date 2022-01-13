package de.ambertation.wunderreich.interfaces;

import net.minecraft.client.renderer.RenderType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ChangeRenderLayer {
    @Environment(EnvType.CLIENT)
    RenderType getRenderType();
}
