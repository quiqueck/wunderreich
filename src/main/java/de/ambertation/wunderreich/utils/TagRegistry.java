package de.ambertation.wunderreich.utils;

import com.google.common.collect.Maps;
import de.ambertation.wunderreich.Wunderreich;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Sets;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagRegistry<T> {
    private static final List<TagRegistry<?>> REGISTRIES = Lists.newArrayList();

    public static final TagRegistry<Block> BLOCK = new TagRegistry<>(Registry.BLOCK);
    public static final TagRegistry<Item> ITEM = new TagRegistry<>(Registry.ITEM);

    public TagRegistry(DefaultedRegistry<T> registry) {
        this.tags = Maps.newHashMap();
        this.registry = registry;
        REGISTRIES.add(this);
    }

    private final Map<ResourceLocation, Set<T>> tags;
    private final DefaultedRegistry<T> registry;


    public void add(TagKey<T> tag, T... objects) {
        ResourceLocation tagID = tag.location();
        Set<T> set = tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
        for (T obj : objects) {
            if (obj != null) {
                set.add(obj);
            }
        }
    }

    public static TagRegistry<?> getRegistryForDirectory(String directory){
        for (TagRegistry<?> reg : REGISTRIES){
            if (reg.isForDirectory(directory))
                return reg;
        }
        return null;
    }

    public boolean isForDirectory(String directory){
        return TagManager.getTagDir(this.getRegistryKey()).equals(directory);
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey(){
        return registry.key();
    }

    private ResourceLocation getLocation(T element){
        ResourceLocation id = registry.getKey(element);
        if (id != registry.getDefaultKey()) {
            return id;
        }
        return null;
    }

    public Map<ResourceLocation, Tag.Builder> addTags(Map<ResourceLocation, Tag.Builder> tagMap){
         for (Map.Entry<ResourceLocation, Set<T>> entry : tags.entrySet()){
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

    public TagKey<T> createCommon(String name){
        return TagKey.create(getRegistryKey(), new ResourceLocation("c", name));
    }
}
