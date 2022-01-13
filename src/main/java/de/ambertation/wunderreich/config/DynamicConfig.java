package de.ambertation.wunderreich.config;

import java.util.Collection;
import java.util.HashMap;

public class DynamicConfig<T> extends ConfigFile {
    private final HashMap<String, BooleanValue> dynamicConfig = new HashMap<>();
    private final HashMap<T, BooleanValue> dynamicLookup = new HashMap<>();

    public DynamicConfig(String category) {
        super(category);
    }

    public BooleanValue newBooleanFor(String name, T object) {
        if (dynamicConfig.containsKey(name)) {
            return dynamicConfig.get(name);
        }

        BooleanValue bv = new BooleanValue("", name, true);
        dynamicConfig.put(name, bv);
        dynamicLookup.put(object, bv);
        return bv;
    }

    public BooleanValue valueOf(String object) {
        return dynamicConfig.get(object);
    }

    public BooleanValue valueOf(T object) {
        return dynamicLookup.get(object);
    }

    public Collection<T> getAllObjects() {
        return dynamicLookup.keySet();
    }
}