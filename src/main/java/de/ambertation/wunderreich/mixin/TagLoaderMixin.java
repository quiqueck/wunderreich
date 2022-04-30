package de.ambertation.wunderreich.mixin;

import de.ambertation.wunderreich.utils.TagRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    @ModifyArg(method = "loadAndBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;build(Ljava/util/Map;)Lnet/minecraft/tags/TagCollection;"))
    public Map<ResourceLocation, Tag.Builder> wunderreich_loadAndBuild(Map<ResourceLocation, Tag.Builder> tagMap) {
        final TagRegistry<?> registry = TagRegistry.getRegistryForDirectory(directory);
        if (registry != null) {
            return registry.addTags(tagMap);
        }

        return tagMap;
    }
}
