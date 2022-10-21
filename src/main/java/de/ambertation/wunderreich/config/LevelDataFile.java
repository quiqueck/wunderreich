package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class LevelDataFile {
    private static final String WUNDERKISTE_TAG_NAME = "wunderkiste";
    private static final String OLD_GLOBAL_TAG_NAME = "global";

    @NotNull
    private LevelData levelData;
    private String baseName;
    private boolean didLoad;
    @NotNull
    private CompoundTag root;

    public LevelDataFile(LevelData levelData, String baseName) {
        root = new CompoundTag();
        this.didLoad = false;
        this.levelData = levelData;
        this.baseName = baseName;
    }

    void reset() {
        root = new CompoundTag();
        didLoad = false;
    }


    private File getDataFile(String comment) {
        assert baseName != null;
        return levelData.dataPath().resolve(Wunderreich.MOD_ID + baseName + comment + ".nbt").toFile();
    }

    public void load() {
        synchronized (this) {
            CompoundTag loadedRoot = null;
            File dataFile = getDataFile("");

            //reload existing file
            if (dataFile.exists()) {
                try {
                    loadedRoot = NbtIo.readCompressed(dataFile);
                } catch (IOException e) {
                    Wunderreich.LOGGER.info(
                            "Unable to access level config from '{}'. Trying previous version.",
                            dataFile.toString(),
                            e
                    );
                    dataFile = getDataFile("_old");
                    try {
                        loadedRoot = NbtIo.readCompressed(dataFile);
                    } catch (IOException ee) {
                        Wunderreich.LOGGER.error("Failed to access level config from '{}'", dataFile.toString(), ee);
                    }
                }
            }

            if (loadedRoot == null) {
                loadedRoot = new CompoundTag();
                loadedRoot.putString("create_version", Wunderreich.VERSION.toString());
            }

            this.root = loadedRoot;
            this.didLoad = true;
        }
    }

    public void save() {
        synchronized (this) {
            if (levelData == null || baseName == null) {
                Wunderreich.LOGGER.error("Unable to write config " + baseName + " - " + baseName);
                return;
            }

            final File tempFile = getDataFile("_temp");
            root.putString("modify_version", Wunderreich.VERSION.toString());
            try {
                NbtIo.writeCompressed(root, tempFile);
                final File dataFile = getDataFile("");
                final File oldFile = getDataFile("_old");

                Util.safeReplaceFile(dataFile, tempFile, oldFile);
            } catch (IOException e) {
                Wunderreich.LOGGER.error(
                        "Unable to write level config for '{}'.",
                        levelData.dataPath() + " - " + baseName,
                        e
                );
            }
        }
    }

    CompoundTag getWunderkisteInventory(String domain) {
        if (!didLoad) load();
        CompoundTag wunderkiste;
        final CompoundTag root = getRoot();
        if (!root.contains(WUNDERKISTE_TAG_NAME)) {
            wunderkiste = new CompoundTag();

            //we found the initial file format => convert it to the new one
            if (root.contains(OLD_GLOBAL_TAG_NAME)) {
                wunderkiste.put(WunderKisteBlock.DEFAULT_DOMAIN.toString(), root.getCompound(OLD_GLOBAL_TAG_NAME));
                root.remove(OLD_GLOBAL_TAG_NAME);
            }

            root.put(WUNDERKISTE_TAG_NAME, wunderkiste);
        } else {
            wunderkiste = root.getCompound(WUNDERKISTE_TAG_NAME);
        }

        if (wunderkiste.contains(domain, Tag.TAG_COMPOUND)) {
            return wunderkiste.getCompound(domain);
        } else {
            CompoundTag global = new CompoundTag();
            wunderkiste.put(domain, global);
            return global;
        }
    }

    public @NotNull CompoundTag getRoot() {
        return root;
    }
}
