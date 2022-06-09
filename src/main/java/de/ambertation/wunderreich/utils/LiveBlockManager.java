package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.LevelData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LiveBlockManager<T extends LiveBlockManager.LiveBlock> {
    public static final Codec<List<LiveBlock>> CODEC = ExtraCodecs.nonEmptyList(LiveBlock.CODEC.listOf());
    private static final String POSITIONS_TAG = "positions";
    private final String type;
    private final Set<T> liveBlocks = ConcurrentHashMap.newKeySet(8);
    private final List<ChangeEvent> listeners = new LinkedList<>();
    private RegistryAccess registryAccess;
    private boolean isLoaded = false;
    private Timer saveTimer;

    private static final Map<ResourceKey<Level>, List<ChunkPosCounter>> FORCE_LOAD_CHUNKS = Maps.newConcurrentMap();

    public LiveBlockManager(String type) {
        this.type = type;
    }

    protected Codec<List<T>> codec() {
        return (Codec<List<T>>) ((Object) CODEC);
    }

    protected void cancleScheduledSave() {
        synchronized (this) {
            if (saveTimer != null) {
                saveTimer.cancel();
                saveTimer = null;
            }
        }
    }

    public void scheduleSave() {
        if (!isLoaded) {
            Wunderreich.LOGGER.error("Trying to schedule a save an unloaded LiveBlockManager!");
            return;
        }

        synchronized (this) {
            if (saveTimer != null) return;
            saveTimer = new java.util.Timer();
        }

        saveTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    saveRaw();
                    cancleScheduledSave();
                }
            }
        }, 10000);
    }

    private void saveRaw() {
        if (!isLoaded) {
            Wunderreich.LOGGER.error("Trying to save an unloaded LiveBlockManager!");
            return;
        }
        CompoundTag tag = LevelData.getInstance().getLiveBlocks(type);
        Tag result = codec().encodeStart(NbtOps.INSTANCE, liveBlocks.stream().toList())
                            .resultOrPartial(Wunderreich.LOGGER::error)
                            .orElse(new ListTag());
        tag.put(POSITIONS_TAG, result);
        LevelData.getInstance().saveLevelConfig();
    }

    public void save() {
        if (!isLoaded) {
            Wunderreich.LOGGER.error("Trying to save an unloaded LiveBlockManager!");
            return;
        }
        synchronized (this) {
            cancleScheduledSave();
            saveRaw();
        }
    }

    public void unLoad() {
        save();
        registryAccess = null;
        isLoaded = false;
        liveBlocks.clear();
    }

    public void load(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
        liveBlocks.clear();

        CompoundTag tag = LevelData.getInstance().getLiveBlocks(type);
        List<T> list = null;
        if (tag.contains(POSITIONS_TAG)) {
            ListTag positions = tag.getList(POSITIONS_TAG, Tag.TAG_COMPOUND);
            list = codec().parse(NbtOps.INSTANCE, positions).resultOrPartial(Wunderreich.LOGGER::error).orElse(null);
        }

        if (list != null) {
            liveBlocks.addAll(list);
            list.forEach(l -> addLoadedChunk(l));
        }
        isLoaded = true;
    }

    public void assignLevels(Map<ResourceKey<Level>, ServerLevel> levels) {
        liveBlocks.forEach(l -> l.loadLevel(levels));
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
        scheduleSave();
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
        scheduleSave();
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
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static void addLoadedChunk(LiveBlock live) {
        List<ChunkPosCounter> chunks = FORCE_LOAD_CHUNKS.computeIfAbsent(live.key, k -> new LinkedList<>());

        Optional<ChunkPosCounter> pos = chunks.stream().filter(c -> c.equals(live.chunkPos)).findAny();
        if (!pos.isPresent()) {
            chunks.add(new ChunkPosCounter(live.chunkPos));
        } else {
            pos.get().inc();
        }
    }

    @FunctionalInterface
    public interface ChangeEvent {
        void emit(LiveBlock bl);
    }

    private static class ChunkPosCounter {
        public final ChunkPos chunkPos;
        public final AtomicInteger count;

        private ChunkPosCounter(ChunkPos chunkPos) {
            this.chunkPos = chunkPos;
            count = new AtomicInteger(1);
        }

        public void inc() {
            count.incrementAndGet();
        }

        public void dec() {
            count.decrementAndGet();
        }

        public boolean isZero() {
            return count.getAcquire() == 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkPosCounter)) return false;
            ChunkPosCounter that = (ChunkPosCounter) o;
            return chunkPos.equals(that.chunkPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkPos);
        }
    }

    public static class LiveBlock {
        public static final Codec<LiveBlock> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(
                                BlockPos.CODEC.fieldOf(
                                        "pos").forGetter(o -> o.pos),
                                Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(o -> o.key)
                        )
                        .apply(instance, LiveBlock::new)
                );
        public final BlockPos pos;
        public final ChunkPos chunkPos;
        public final ResourceKey<Level> key;
        private Level level;

        public LiveBlock(BlockPos pos, Level level) {
            this(pos, level.dimension());
            this.level = level;
        }

        private LiveBlock(BlockPos pos, ResourceKey<Level> key) {
            this.pos = pos;
            this.key = key;
            this.chunkPos = new ChunkPos(pos);
        }

        void loadLevel(Map<ResourceKey<Level>, ServerLevel> levels) {
            level = levels.get(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LiveBlock liveBlock = (LiveBlock) o;
            return pos.equals(liveBlock.pos) && key.equals(liveBlock.key);
        }

        @Override
        public int hashCode() {
            return pos.hashCode() + key.hashCode();
        }

        @Override
        public String toString() {
            return key + " (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        }

        public Level getLevel() {
            return level;
        }
    }
}
