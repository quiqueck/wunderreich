package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagRegistry<T> {
    private static final List<TagRegistry<?>> REGISTRIES = Lists.newArrayList();
    public static final TagRegistry<Block> BLOCK = new TagRegistry<>(BuiltInRegistries.BLOCK);
    public static final TagRegistry<Item> ITEM = new TagRegistry<>(BuiltInRegistries.ITEM);
    private final Map<ResourceLocation, Set<T>> tags;
    private final DefaultedRegistry<T> registry;

    public TagRegistry(DefaultedRegistry<T> registry) {
        this.tags = Maps.newHashMap();
        this.registry = registry;
        REGISTRIES.add(this);
    }

    public static TagRegistry<?> getRegistryForDirectory(String directory) {
        for (TagRegistry<?> reg : REGISTRIES) {
            if (reg.isForDirectory(directory)) return reg;
        }
        return null;
    }

    public void add(TagKey<T> tag, T... objects) {
        ResourceLocation tagID = tag.location();
        Set<T> set = tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
        for (T obj : objects) {
            if (obj != null) {
                set.add(obj);
            }
        }
    }

    public boolean isForDirectory(String directory) {
        return TagManager.getTagDir(this.getRegistryKey()).equals(directory);
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registry.key();
    }

    private ResourceLocation getLocation(T element) {
        ResourceLocation id = registry.getKey(element);
        if (id != registry.getDefaultKey()) {
            return id;
        }
        return null;
    }

    public Map<ResourceLocation, List<TagLoader.EntryWithSource>> addTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap) {
        for (Map.Entry<ResourceLocation, Set<T>> entry : tags.entrySet()) {
            final ResourceLocation location = entry.getKey();
            final Set<T> elements = entry.getValue();
            final List<TagLoader.EntryWithSource> builder = tagMap.computeIfAbsent(location, (loc) -> new ArrayList());

            for (T element : elements) {
                ResourceLocation elementLocation = getLocation(element);
                if (elementLocation != null) {
                    builder.add(new TagLoader.EntryWithSource(TagEntry.element(elementLocation), Wunderreich.MOD_ID));
                }
            }
        }

        return tagMap;
    }

    public TagKey<T> createCommon(String name) {
        return TagKey.create(getRegistryKey(), new ResourceLocation("c", name));
    }
}
