package de.ambertation.wunderreich.testmod.mixin;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.testmod.gametest.FlammabilityGameTest;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Does what Adorn does: registers a flammable <em>tag</em> before Wunderreich registers its blocks.
 * Wunderreich used to query {@link FlammableBlockRegistry#get} for every block it registered, which
 * resolves all tag entries and crashed the server with "Tags not bound" during mod initialization.
 * A mixin (instead of a testmod entrypoint) because the entrypoint order between two mods is not
 * guaranteed, and the tag must be in the registry before {@code WunderreichBlocks} initializes.
 */
@Mixin(value = Wunderreich.class, remap = false)
public abstract class FlammableTagBeforeBlocksMixin {
    @Inject(method = "onInitialize", at = @At("HEAD"))
    private void wunderreich_testmod$registerFlammableTag(CallbackInfo ci) {
        FlammableBlockRegistry.getDefaultInstance().add(FlammabilityGameTest.FLAMMABLE_PROBE_TAG, 5, 20);
    }
}
