package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.utils.TagRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TagManager.LoaderInfo.class)
public class LoaderInfoMixin {
    @ModifyArg(method = "addToBuilder", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagContainer$Builder;add(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagCollection;)Lnet/minecraft/tags/TagContainer$Builder;"))
    public <T> TagCollection<T> wunderreich_addToBuilder(ResourceKey<? extends Registry<? extends T>> resourceKey,
                                                         TagCollection<T> tagCollection) {
        return TagRegistry.addWunderreichTags(resourceKey, tagCollection);
    }
}
