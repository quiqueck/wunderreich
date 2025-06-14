package de.ambertation.wunderreich.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface ChangeRenderLayer {
    @Environment(EnvType.CLIENT)
    ChunkSectionLayer getRenderType();
}
