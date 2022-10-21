package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelData {
    public final static String DATA_FOLDER = "data";
    private static final String LIVEBLOCKS_TAG_NAME = "live_blocks";
    private static LevelData INSTANCE;
    @Nullable
    Path levelPath;
    @Nullable
    LevelDataFile levelFile;

    private LevelData() {
    }

    public static LevelData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LevelData();
        }
        return INSTANCE;
    }

    private void reset() {
        levelFile = null;
        levelPath = null;

        for (LevelDataFile f : FILES.values()) {
            f.reset();
        }
        FILES.clear();
    }

    public void loadNewLevel(LevelStorageSource.LevelStorageAccess accessor) {
        reset();
        if (accessor == null) return;

        levelPath = accessor.getLevelPath(LevelResource.ROOT);
        levelFile = new LevelDataFile(this, "");
        levelFile.load();
    }


    public void saveLevelConfig() {
        if (levelFile != null)
            levelFile.save();
    }

    public CompoundTag getRoot() {
        if (levelFile != null) return levelFile.getRoot();
        return new CompoundTag();
    }

    public CompoundTag getLiveBlocks(String type) {
        CompoundTag liveBlocks;
        final CompoundTag root = getRoot();
        if (!root.contains(LIVEBLOCKS_TAG_NAME)) {
            liveBlocks = new CompoundTag();
            root.put(LIVEBLOCKS_TAG_NAME, liveBlocks);
        } else {
            liveBlocks = root.getCompound(LIVEBLOCKS_TAG_NAME);
        }

        if (!liveBlocks.contains(type)) {
            CompoundTag item = new CompoundTag();
            liveBlocks.put(type, item);
            return item;
        } else {
            return liveBlocks.getCompound(type);
        }
    }

    public CompoundTag getWunderkisteInventory(WunderKisteDomain.ID domain) {
        if (domain.extraFile == null)
            return levelFile.getWunderkisteInventory(domain.toString());

        return domain.extraFile.getWunderkisteInventory(domain.toString());
    }

    public void saveWunderkisteInventory(WunderKisteDomain.ID domain) {
        if (domain.extraFile == null) {
            this.saveLevelConfig();
        } else {
            domain.extraFile.save();
        }
    }

    private static final Map<String, LevelDataFile> FILES = new HashMap<>();

    public LevelDataFile fileForName(String name) {
        return FILES.computeIfAbsent(name, n -> new LevelDataFile(this, n));
    }

    Path dataPath() {
        return levelPath.resolve(DATA_FOLDER);
    }
}
