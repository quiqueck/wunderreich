package de.ambertation.wunderreich.config;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelData {
    public final static String DATA_FOLDER = "data";
    private static LevelData INSTANCE;

    public static LevelData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LevelData();
        }
        return INSTANCE;
    }

    private LevelData() {
        CompoundTag root = new CompoundTag();
    }

    @Nullable
    private Path levelPath;

    @NotNull
    private CompoundTag root;

    private void reset() {
        levelPath = null;
        CompoundTag root = new CompoundTag();
    }

    public void loadNewLevel(LevelStorageSource levelSource, String levelID) {
        LevelStorageSource.LevelStorageAccess accessor;
        accessor = getLevelStorageAccessAndLock(levelSource, levelID);

        loadNewLevel(accessor);
        unlock(levelID, accessor);
    }

    public void loadNewLevel(LevelStorageSource.LevelStorageAccess accessor) {
        reset();
        if (accessor == null) return;

        levelPath = accessor.getLevelPath(LevelResource.ROOT);
        loadLevelConfig();
    }

    private File getDataFile(String comment) {
        return levelPath.resolve(DATA_FOLDER + "/" + Wunderreich.MOD_ID + comment + ".nbt").toFile();
    }

    private void loadLevelConfig() {
        CompoundTag loadedRoot = null;
        File dataFile = getDataFile("");

        //reload existing file
        if (dataFile.exists()) {
            try {
                loadedRoot = NbtIo.readCompressed(dataFile);
            } catch (IOException e) {
                Wunderreich.LOGGER.info("Unable to access level config from '{}'. Trying previous version.",
                        dataFile.toString(),
                        e);
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
            loadedRoot.putString("create_version", Wunderreich.VERSION);
        }

        this.root = loadedRoot;
    }

    public void saveLevelConfig() {
        if (levelPath == null) {
            Wunderreich.LOGGER.error("Unable to write level config.");
            return;
        }

        final File tempFile = getDataFile("_temp");
        root.putString("modify_version", Wunderreich.VERSION);
        try {
            NbtIo.writeCompressed(root, tempFile);
            final File dataFile = getDataFile("");
            final File oldFile = getDataFile("_old");

            Util.safeReplaceFile(dataFile, tempFile, oldFile);
        } catch (IOException e) {
            Wunderreich.LOGGER.error("Unable to write level config for '{}'.", levelPath.toString(), e);
        }
    }

    public CompoundTag getRoot() {
        return root;
    }

    public CompoundTag getGlobalInventory() {
        //TODO: remove!!!
        if (root == null) {
            Wunderreich.LOGGER.error("Accessed global Inventory before level load.");
            return new CompoundTag();
        }

        if (root.contains("global", Tag.TAG_COMPOUND)) {
            return root.getCompound("global");
        } else {
            CompoundTag global = new CompoundTag();
            root.put("global", global);
            return global;
        }
    }

    private void unlock(String levelID, LevelStorageAccess levelStorageAccess) {
        try {
            levelStorageAccess.close();
        } catch (IOException e) {
            Wunderreich.LOGGER.error("Closing level access did fail for '{}'.", levelID, e);
        }
    }

    @Nullable
    private LevelStorageAccess getLevelStorageAccessAndLock(LevelStorageSource levelSource, String levelID) {
        LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = levelSource.createAccess(levelID);
        } catch (IOException e) {
            Wunderreich.LOGGER.error("Error while reading level folder for '{}'.", levelID, e);

            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), levelID);
            Minecraft.getInstance()
                     .setScreen(null);
            return null;
        }
        return levelStorageAccess;
    }
}
