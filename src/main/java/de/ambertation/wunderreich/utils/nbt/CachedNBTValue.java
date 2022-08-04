package de.ambertation.wunderreich.utils.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CachedNBTValue<D, T extends Tag> {
    private final CompoundTag baseTag;
    private D cachedValue;
    public final String tagName;
    private final Function<T, D> getter;
    private final Function<D, Tag> setter;
    private final BiConsumer<D, D> changed;

    public CachedNBTValue(
            CompoundTag baseTag,
            String tagName,
            Function<T, D> getter,
            Function<D, Tag> setter
    ) {
        this(baseTag, tagName, getter, setter, (old, fresh) -> {
        });
    }

    public CachedNBTValue(
            CompoundTag baseTag,
            String tagName,
            Function<T, D> getter,
            Function<D, Tag> setter,
            BiConsumer<D, D> changed
    ) {
        cachedValue = null;

        this.baseTag = baseTag;
        this.tagName = tagName;
        this.getter = getter;
        this.setter = setter;
        this.changed = changed;
    }

    public D get() {
        if (cachedValue != null) return cachedValue;

        if (!baseTag.contains(tagName)) {
            return null;
        } else {
            cachedValue = getter.apply((T) baseTag.get(tagName));
            changed.accept(null, cachedValue);
            return cachedValue;
        }
    }

    public void set(D newValue) {
        D oldValue = cachedValue;
        cachedValue = newValue;
        if (newValue == null) {
            if (baseTag.contains(tagName)) {
                baseTag.remove(tagName);
            }
        } else {
            baseTag.put(tagName, setter.apply(newValue));
        }
        changed.accept(oldValue, cachedValue);
    }
}
