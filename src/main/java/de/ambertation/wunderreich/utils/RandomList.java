package de.ambertation.wunderreich.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class RandomList<T> implements Iterable<RandomList.Entry<T>> {
    private final ArrayList<Entry<T>> list;
    private float weightSum;

    public RandomList() {
        this(9);
    }

    public RandomList(int capacity) {
        list = new ArrayList<>(capacity);
        weightSum = 0;
    }

    public static float random() {
        return (float) Math.random();
    }

    @NotNull
    @Override
    public Iterator<Entry<T>> iterator() {
        return list.iterator();
    }

    public void add(T value, float weight) {
        Entry<T> e = new Entry(value, weight);
        list.add(e);
        weightSum += e.weight;
    }

    public T get(int idx) {
        Entry<T> e = list.get(idx);
        if (e == null) return null;
        return e.value;
    }

    public int getRandomIndex() {
        return getRandomIndex(RandomList::random);
    }

    public int getRandomIndex(Supplier<Float> rnd) {
        final int CYCLES = 3;
        final int count = list.size();
        float sum = 0;
        final float r = rnd.get();
        float random = r * weightSum * CYCLES;
        for (int i = 0; i < count * CYCLES; i++) {
            Entry<T> e = list.get(i % count);
            sum += e.weight;
            if (random <= sum) return i % count;
        }

        return list.size() - 1;
    }

    public T getRandom() {
        return getRandom(RandomList::random);
    }

    public T getRandom(Supplier<Float> rnd) {
        int idx = getRandomIndex(rnd);
        if (idx < 0 || idx >= list.size()) return null;
        return get(idx);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public static class Entry<T> {
        public final T value;
        public final float weight;

        Entry(T value, float weight) {
            this.value = value;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "value=" + value +
                    ", weight=" + weight +
                    '}';
        }
    }
}
