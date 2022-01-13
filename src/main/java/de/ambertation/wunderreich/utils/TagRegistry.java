package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.registries.WunderreichTags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class TagRegistry<T> {


    public TagRegistry() {
        this(new HashMap<>());
    }

    protected TagRegistry(Map<ResourceLocation, Set<T>> t) {
        this.tags = t;
    }

    private final Map<ResourceLocation, Set<T>> tags;
    private final Tag<T> empty = SetTag.empty();

    public void add(Tag<T> tag, T... objects) {
        if (tag instanceof Tag.Named named) {
            addNamed(named, objects);
        } else {
            Wunderreich.LOGGER.warn("Unable to add " + tag);
        }
    }

    public void addNamed(Tag.Named<T> tag, T... objects) {
        ResourceLocation tagID = tag.getName();
        Set<T> set = tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
        for (T obj : objects) {
            if (obj != null) {
                set.add(obj);
            }
        }
    }

    private void add(ResourceLocation tagID, SetTag<T> objects) {
        Set<T> set = tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
        set.addAll(objects.getValues());
    }

    private TagRegistry<T> deepCopy() {
        Map<ResourceLocation, Set<T>> t = new HashMap<>();
        tags.entrySet().stream().forEach(entry -> {
            t.put(entry.getKey(), Sets.newHashSet(entry.getValue()));
        });

        return new TagRegistry<>(t);
    }

    private Map<ResourceLocation, Tag<T>> buildMap() {
        return tags.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> SetTag.create(e.getValue())
        ));
    }

    private TagCollection<T> buildTagCollection() {
        Map<ResourceLocation, Tag<T>> map = buildMap();

        return new TagCollection<T>() {
            @Override
            public Map<ResourceLocation, Tag<T>> getAllTags() {
                return map;
            }

            @Override
            public Tag<T> getTagOrEmpty(ResourceLocation resourceLocation) {
                return map.getOrDefault(resourceLocation, empty);
            }

            @Nullable
            @Override
            public ResourceLocation getId(Tag<T> tag) {
                if (tag instanceof Tag.Named) {
                    return ((Tag.Named) tag).getName();
                }
                return (ResourceLocation) map.get(tag);
            }
        };
    }

    public static <T> TagCollection<T> addWunderreichTags(ResourceKey<? extends Registry<? extends T>> resourceKey,
                                                          TagCollection<T> tagCollection) {
        Map<ResourceLocation, Tag<T>> reg = null;
        if (resourceKey.location().equals(new ResourceLocation("block"))) {
            TagRegistry<Block> blockReg = WunderreichTags.BLOCK.deepCopy();

            tagCollection
                    .getAllTags()
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        if (entry.getValue() instanceof SetTag<T> set) {
                            blockReg.add(entry.getKey(), (SetTag<Block>) set);
                        }
                    });

            return (TagCollection<T>) blockReg.buildTagCollection();
        }
        return tagCollection;
    }
}
