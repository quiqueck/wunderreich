package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.tag.TagFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class TagRegistry<T> {
    private static final List<TagRegistry<?>> REGISTRIES = Lists.newArrayList();
    public static final TagRegistry<Block> BLOCK = new TagRegistry<>("tags/blocks",
            Registry.BLOCK,
            TagFactory.BLOCK::create);
    public static final TagRegistry<Item> ITEM = new TagRegistry<>("tags/items",
            Registry.ITEM,
            TagFactory.ITEM::create);
    private final Map<ResourceLocation, Set<T>> tags;
    private final DefaultedRegistry<T> registry;
    private final String directory;
    private final Function<ResourceLocation, Tag.Named<T>> tagCreator;

    public TagRegistry(String directory,
                       DefaultedRegistry<T> registry,
                       Function<ResourceLocation, Tag.Named<T>> tagCreator) {
        this.tags = Maps.newHashMap();
        this.registry = registry;
        this.directory = directory;
        this.tagCreator = tagCreator;
        REGISTRIES.add(this);
    }

    public static TagRegistry<?> getRegistryForDirectory(String directory) {
        for (TagRegistry<?> reg : REGISTRIES) {
            if (reg.isForDirectory(directory))
                return reg;
        }
        return null;
    }

    @SafeVarargs
    public final void add(Tag.Named<T> tag, T... objects) {
        ResourceLocation tagID = tag.getName();
        Set<T> set = tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
        for (T obj : objects) {
            if (obj != null) {
                set.add(obj);
            }
        }
    }

    public boolean isForDirectory(String directory) {
        return this.directory.equals(directory);
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

    public Map<ResourceLocation, Tag.Builder> addTags(Map<ResourceLocation, Tag.Builder> tagMap) {
        for (Map.Entry<ResourceLocation, Set<T>> entry : tags.entrySet()) {
            final ResourceLocation location = entry.getKey();
            final Set<T> elements = entry.getValue();
            final Tag.Builder builder = tagMap.computeIfAbsent(location, key -> Tag.Builder.tag());

            for (T element : elements) {
                ResourceLocation elementLocation = getLocation(element);
                if (elementLocation != null) {
                    builder.addElement(elementLocation, Wunderreich.MOD_ID);
                }
            }
        }

        return tagMap;
    }

    public Tag.Named<T> createCommon(String name) {
        return tagCreator.apply(new ResourceLocation("c", name));
    }
}
