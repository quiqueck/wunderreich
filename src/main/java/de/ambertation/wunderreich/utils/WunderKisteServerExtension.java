package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.inventory.WunderKisteContainer;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Maps;

import java.util.Map;

public class WunderKisteServerExtension {
    private final Map<WunderKisteDomain, WunderKisteContainer> containers = Maps.newHashMap();
    public static final LiveBlockManager<LiveBlockManager.LiveBlock> WUNDERKISTEN = new LiveBlockManager<>("wunderkiste");

    public static WunderKisteDomain getDomain(BlockState state) {
        if (WunderreichRules.Wunderkiste.colorsOrDomains() && state.hasProperty(WunderKisteBlock.DOMAIN))
            return state.getValue(WunderKisteBlock.DOMAIN);
        return WunderKisteBlock.DEFAULT_DOMAIN;
    }

    public WunderKisteContainer getContainer(BlockState state) {
        return getContainer(getDomain(state));
    }

    public WunderKisteContainer getContainer(WunderKisteDomain domain) {
        return containers.computeIfAbsent(
                WunderreichRules.Wunderkiste.haveMultiple()
                        ? domain
                        : WunderKisteBlock.DEFAULT_DOMAIN,
                this::loadOrCreate
        );
    }

    private WunderKisteContainer loadOrCreate(WunderKisteDomain wunderKisteDomain) {
        WunderKisteContainer wunderKisteContainer = new WunderKisteContainer(wunderKisteDomain);
        wunderKisteContainer.load();
        wunderKisteContainer.addListener((container) -> {
            WunderKisteBlock.updateAllBoxes(container, false, true);
        });
        return wunderKisteContainer;
    }

    public void saveAll() {
        containers.entrySet().forEach(e -> e.getValue().save());
    }

    public void onCloseServer() {
        Wunderreich.LOGGER.info("Unloading Cache for Wunderkiste");

        //Make sure the levels can unload when the server closes
        WUNDERKISTEN.unLoad();
    }

    public void onStartServer(LayeredRegistryAccess<RegistryLayer> registryAccess) {
        //we start a new world, so clear any old block
        Wunderreich.LOGGER.info("Initializing Cache for Wunderkiste");
        containers.clear();

        //this needs access to the LevelData
        WUNDERKISTEN.load(registryAccess);
    }

    public void onLevelsCreated(Map<ResourceKey<Level>, ServerLevel> levels) {
        Wunderreich.LOGGER.info("Assigning Levels to Wunderkiste in " + levels.size() + " levels.");

        //this needs access to the LevelData
        WUNDERKISTEN.assignLevels(levels);
    }
}
