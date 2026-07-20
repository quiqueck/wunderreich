package de.ambertation.wunderreich.interfaces;

/**
 * Lets a common {@code Block} declare its render layer for {@code BlockModelProvider} (datagen) to
 * write into the generated model's {@code render_type}, without the Block class needing to
 * reference the client-only {@code ChunkSectionLayer} type.
 */
public interface ChangeRenderLayer {
    enum RenderLayer {
        CUTOUT,
        TRANSLUCENT
    }

    RenderLayer getRenderType();
}
