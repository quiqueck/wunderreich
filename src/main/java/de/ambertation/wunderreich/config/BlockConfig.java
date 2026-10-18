package de.ambertation.wunderreich.config;

import de.ambertation.wunderlib.configs.DynamicConfig;
import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockConfig extends DynamicConfig<Block> {
    private final List<Block> requiredBlocks = new ArrayList<>(1);

    public BlockConfig() {
        super(Wunderreich.VERSION_PROVIDER, "blocks");
    }

    public boolean isEnabled(Block block) {
        if (block == null) return false;
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null;
    }

    /**
     * Blocks that other parts of the mod depend on (like the Wunderkiste) can not be disabled. They have no
     * switch in the config file: an old switch is ignored (with a warning when it disabled the block) and
     * removed from the file.
     *
     * @param name the config name (path of the block id)
     */
    public static boolean isRequired(String name) {
        return "wunder_kiste".equals(name);
    }

    /**
     * Removes the switch for a block that can not be disabled (see {@link #isRequired(String)}) from the
     * config file and logs a warning if it was set to false.
     *
     * @param name the config name (path of the block id)
     */
    public void dropRequiredSwitch(String name) {
        final BooleanValue oldSwitch = new BooleanValue("", name, true, true).hideInUI();
        if (!oldSwitch.get()) {
            Wunderreich.LOGGER.warn(
                    "'{}' can not be disabled: its setting in config/wunderreich/blocks.json is ignored and was removed from the file.",
                    name
            );
        }
        oldSwitch.remove();
    }

    /**
     * Registers a block that can not be disabled (see {@link #isRequired(String)}), so it is listed with
     * all other blocks of the mod.
     */
    public void addRequired(Block block) {
        requiredBlocks.add(block);
    }

    @Override
    public boolean valueOf(Block block) {
        if (block != null && requiredBlocks.contains(block)) return true;
        return super.valueOf(block);
    }

    @Override
    public Collection<Block> getAllObjects() {
        if (requiredBlocks.isEmpty()) return super.getAllObjects();
        final List<Block> all = new ArrayList<>(requiredBlocks);
        all.addAll(super.getAllObjects());
        return all;
    }
}
