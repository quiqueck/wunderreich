package de.ambertation.wunderreich.utils;

import java.util.ArrayList;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class RandomList<T> implements Iterable<RandomList.Entry<T>> {
    private final ArrayList<Entry<T>> list;
    private double weightSum;

    public RandomList() {
        this(9);
    }

    public RandomList(int capacity) {
        list = new ArrayList<>(capacity);
        weightSum = 0;
    }

    @NotNull
    @Override
    public Iterator<Entry<T>> iterator() {
        return list.iterator();
    }

    public static class Entry<T> {
        public final T value;
        public final double weight;

        Entry(T value, double weight) {
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

    public void add(T value, double weight) {
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
        double sum = 0;
        double random = Math.random() * weightSum;
        for (int i = 0; i < list.size(); i++) {
            Entry<T> e = list.get(i);
            sum += e.weight;
            if (random <= sum) return i;
        }

        return list.size() - 1;
    }

    public T getRandom() {
        int idx = getRandomIndex();
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
}
