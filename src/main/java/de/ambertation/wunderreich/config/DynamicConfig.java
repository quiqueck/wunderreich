package de.ambertation.wunderreich.config;

import java.util.Collection;
import java.util.HashMap;

public class DynamicConfig<T> extends ConfigFile {
    private static final boolean DEFAULT_VALUE = true;
    private final HashMap<String, BooleanValue> dynamicConfig = new HashMap<>();
    private final HashMap<T, BooleanValue> dynamicLookup = new HashMap<>();

    public DynamicConfig(String category) {
        super(category);
    }

    public BooleanValue booleanOrDefault(String name) {
        if (dynamicConfig.containsKey(name)) {
            return dynamicConfig.get(name);
        }
        return new BooleanValue("", name, DEFAULT_VALUE);
    }

    public BooleanValue newBooleanFor(String name, T object) {
        if (dynamicConfig.containsKey(name)) {
            return dynamicConfig.get(name);
        }

        BooleanValue bv = new BooleanValue("", name, DEFAULT_VALUE);
        dynamicConfig.put(name, bv);
        dynamicLookup.put(object, bv);
        return bv;
    }

    public BooleanValue valueOf(String object) {
        return dynamicConfig.get(object);
    }

    public boolean valueOf(T object) {
        if (object == null) return false;
        BooleanValue bv = dynamicLookup.get(object);
        if (bv == null) return false;
        return bv.get();
    }

    public Collection<T> getAllObjects() {
        return dynamicLookup.keySet();
    }
}