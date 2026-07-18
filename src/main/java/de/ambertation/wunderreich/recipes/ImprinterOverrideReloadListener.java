package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

/**
 * Server-data reload listener that (re)loads the imprinter override files on world load and
 * {@code /reload}. The actual read is delegated to {@link ImprinterOverrides#ensureLoaded} which
 * de-duplicates against the recipe reload (the {@code RecipeManagerMixin} loads the overrides for
 * the same {@link ResourceManager} instance before it builds the recipes), so the files are read
 * only once per reload regardless of listener ordering.
 */
public class ImprinterOverrideReloadListener implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = Wunderreich.ID("imprinter_overrides");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ImprinterOverrides.ensureLoaded(resourceManager);
    }
}
