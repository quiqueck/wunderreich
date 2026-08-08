package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.recipes.ImprinterOverrides;

import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 26.3 moved recipe loading into the datapack registry pass, which happens before any
 * {@code PreparableReloadListener} runs. The imprinter override layer has to be readable while the
 * recipes are being built ({@code RecipeManagerMixin}), so load it here, from the {@link ResourceManager}
 * of the reload that is about to start. {@link ImprinterOverrides#ensureLoaded} de-duplicates against
 * {@code ImprinterOverrideReloadListener}, so the files are still read only once per reload.
 */
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void wunder_loadOverrides(
            ResourceManager resourceManager,
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
            List<Registry.PendingTags<?>> pendingTags,
            FeatureFlagSet featureFlagSet,
            Commands.CommandSelection commandSelection,
            PermissionSet permissionSet,
            Executor executor,
            Executor executor2,
            CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir
    ) {
        ImprinterOverrides.ensureLoaded(resourceManager);
    }
}
