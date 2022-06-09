package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LiveBlockManager<T extends LiveBlockManager.LiveBlock> {

    private boolean isLoaded = false;
    private final Set<T> liveBlocks = ConcurrentHashMap.newKeySet(8);
    private final List<ChangeEvent> listeners = new LinkedList<>();

    public void unLoad() {
        isLoaded = false;
        liveBlocks.clear();
    }

    public void load() {
        liveBlocks.clear();
        isLoaded = true;
    }

    public int size() {
        return liveBlocks.size();
    }

    public boolean contains(T live) {
        return liveBlocks.contains(live);
    }

    public boolean add(T live) {
        if (!isLoaded) {
            Wunderreich.LOGGER.error("Trying to add " + live + " to an unloaded LiveBlockManager!");
            return false;
        }

        if (contains(live)) return false;

        liveBlocks.add(live);
        emitChangeAt(live);
        return true;
    }

    public boolean remove(T live) {
        if (!isLoaded) {
            Wunderreich.LOGGER.error("Trying to remove " + live + " from an unloaded LiveBlockManager!");
            return false;
        }

        if (!contains(live)) return false;

        liveBlocks.remove(live);
        emitChangeAt(live);
        return true;
    }

    public void forEach(Consumer<T> consumer) {
        liveBlocks.forEach(consumer);
    }

    public void emitChangeAt(LiveBlock bl) {
        listeners.forEach(l -> l.emit(bl));
    }

    public void emitChange() {
        liveBlocks.forEach(bl -> listeners.forEach(l -> l.emit(bl)));
    }

    public void onChangeAt(ChangeEvent listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    @FunctionalInterface
    public interface ChangeEvent {
        void emit(LiveBlock bl);
    }

    public static class LiveBlock {
        public final BlockPos pos;
        public final Level level;

        public LiveBlock(BlockPos pos, Level level) {
            this.pos = pos;
            this.level = level;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LiveBlock liveBlock = (LiveBlock) o;
            return pos.equals(liveBlock.pos) && level.dimension().equals(liveBlock.level.dimension());
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        @Override
        public String toString() {
            return level.dimensionType() + " (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        }
    }
}
