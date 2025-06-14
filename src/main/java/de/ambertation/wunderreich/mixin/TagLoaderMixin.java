package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.utils.TagRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    @WrapOperation(method = "loadTagsForRegistry", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;build(Ljava/util/Map;)Ljava/util/Map;"))
    private static <T> Map<ResourceLocation, List<T>> wunderreich_wrapBuild(
            TagLoader tagLoader,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Map<ResourceLocation, List<T>>> original
    ) {
        // Get the directory from the TagLoader instance
        String directory = ((TagLoaderMixin) (Object) tagLoader).directory;

        // Find our custom TagRegistry for this directory
        TagRegistry<?> registry = TagRegistry.getRegistryForDirectory(directory);

        // Modify the tag map if we have custom tags to add
        if (registry != null) {
            tagMap = registry.addTags(tagMap);
        }

        // Call the original method (or the next mod's wrapper) with our modified tag map
        return original.call(tagLoader, tagMap);
    }
}
