package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.LevelData;
import de.ambertation.wunderreich.registries.WunderreichRules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.google.common.collect.Maps;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LiveBlockManager<T extends LiveBlockManager.LiveBlock> {
    public static final TicketType<ChunkPos> TICKET = TicketType.create(
            "wunderkiste",
            Comparator.comparingLong(ChunkPos::toLong)
    );
    public static final Codec<List<LiveBlock>> CODEC = ExtraCodecs.nonEmptyList(LiveBlock.CODEC.listOf());
    private static final String POSITIONS_TAG = "positions";
    private final String type;
    private final Set<T> liveBlocks = ConcurrentHashMap.newKeySet(8);
    private final List<ChangeEvent> listeners = new LinkedList<>();
    private LayeredRegistryAccess<RegistryLayer> registryAccess;
    private boolean isLoaded = false;
    private Timer saveTimer;

    private static final Map<Level, List<ChunkPosCounter>> FORCE_LOAD_CHUNKS = Maps.newConcurrentMap();

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

    public void load(LayeredRegistryAccess<RegistryLayer> registryAccess) {
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
        addLoadedChunk(live, WunderreichRules.Wunderkiste.chunkLoaderDist());
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
        removeLoadedChunk(live, WunderreichRules.Wunderkiste.chunkLoaderDist());
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

    private static Stream<ChunkPos> chunksWithRadius(ChunkPos start, int radius) {
        Stream.Builder<ChunkPos> p = Stream.builder();
        for (int x = 1 - radius; x < radius; x++) {
            for (int z = 1 - radius; z < radius; z++) {
                p.add(new ChunkPos(start.x + x, start.z + z));
            }
        }
        return p.build();
    }

    public static void addLoadedChunk(LiveBlock live, int radius) {
        List<ChunkPosCounter> chunks = FORCE_LOAD_CHUNKS.computeIfAbsent(live.level, k -> new LinkedList<>());

        chunksWithRadius(live.chunkPos, radius).forEach(cPos -> {
                    Optional<ChunkPosCounter> pos = chunks.stream().filter(c -> c.equals(cPos)).findAny();
                    if (pos.isEmpty()) {
                        chunks.add(new ChunkPosCounter(cPos));
                        addTicket(live.level, cPos);
                    } else {
                        pos.get().inc();
                    }
                }
        );
    }


    private static void addTicket(Level level, ChunkPos cPos) {

        if (level instanceof ServerLevel server) {
            Wunderreich.LOGGER.info("Keep Chunk " + cPos + " in " + level
                    .dimension()
                    .location() + " permanently loaded");
            server.getChunkSource().chunkMap
                    .getDistanceManager()
                    .addRegionTicket(TICKET, cPos, 2, cPos);
        }
    }

    public static void removeLoadedChunk(LiveBlock live, int radius) {
        List<ChunkPosCounter> chunks = FORCE_LOAD_CHUNKS.computeIfAbsent(live.level, k -> new LinkedList<>());

        chunksWithRadius(live.chunkPos, radius).forEach(cPos -> {
                    Optional<ChunkPosCounter> pos = chunks.stream().filter(c -> c.equals(cPos)).findAny();
                    if (pos.isPresent()) {
                        if (pos.get().dec() == 0) {
                            chunks.remove(cPos);
                            removeTicket(live.level, cPos);
                        }
                    }
                }
        );
    }

    private static void removeTicket(Level level, ChunkPos cPos) {
        if (level instanceof ServerLevel server) {
            Wunderreich.LOGGER.info("Remove Chunk " + cPos + " in " + level
                    .dimension()
                    .location() + " from force loaded list");

            server.getChunkSource().chunkMap
                    .getDistanceManager()
                    .removeRegionTicket(TICKET, cPos, 2, cPos);
        }
    }

    public void rebuildLoadedChunks() {
        for (var e : FORCE_LOAD_CHUNKS.entrySet()) {
            for (var cPos : e.getValue()) {
                removeTicket(e.getKey(), cPos);
            }
        }

        FORCE_LOAD_CHUNKS.clear();

        for (LiveBlock live : liveBlocks) {
            addLoadedChunk(live, WunderreichRules.Wunderkiste.chunkLoaderDist());
        }
    }

    public boolean shouldTick(ServerLevel level) {
        return FORCE_LOAD_CHUNKS.containsKey(level);
    }

    public boolean shouldTick(ServerLevel level, BlockPos pos) {
        ChunkPos cPos = new ChunkPos(pos);
        return shouldTick(level, cPos);
    }

    public boolean shouldTick(ServerLevel level, ChunkPos cPos) {
        List<ChunkPosCounter> chunks = FORCE_LOAD_CHUNKS.computeIfAbsent(level, k -> new LinkedList<>());
        return chunks.contains(cPos);
    }


    @FunctionalInterface
    public interface ChangeEvent {
        void emit(LiveBlock bl);
    }

    private static class ChunkPosCounter extends ChunkPos {
        public final AtomicInteger count;

        private ChunkPosCounter(ChunkPos chunkPos) {
            super(chunkPos.x, chunkPos.z);
            count = new AtomicInteger(1);
        }

        public void inc() {
            count.incrementAndGet();
        }

        public int dec() {
            return count.decrementAndGet();
        }

        @Override
        public boolean equals(Object object) {
            return super.equals(object);
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
            if (level == null) {
                level = levels.get(key);
                addLoadedChunk(this, WunderreichRules.Wunderkiste.chunkLoaderDist());
            }
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
