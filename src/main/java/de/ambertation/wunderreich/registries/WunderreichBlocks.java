package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.BoxOfEirBlock;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.config.MainConfig;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import ru.bclib.registry.BlockRegistry;

import java.util.List;

public class WunderreichBlocks {
    private static final BlockRegistry REGISTRY = new BlockRegistry(CreativeTabs.TAB_BLOCKS, Configs.BLOCK_CONFIG);

    public static final Block BOX_OF_EIR = registerBlock("box_of_eir", new BoxOfEirBlock());

    @NotNull
    public static BlockRegistry getBlockRegistry() {
        return REGISTRY;
    }

    public static List<Block> getModBlocks() {
        return BlockRegistry.getModBlocks(Wunderreich.MOD_ID);
    }

    public static List<Item> getModBlockItems() {
        return BlockRegistry.getModBlockItems(Wunderreich.MOD_ID);
    }

    public static Block registerBlock(String name, Block block, boolean register) {
        if (register) {
            return registerBlock(name, block);
        }
        return block;
    }

    public static Block registerBlock(String name, Block block) {
        if (!Configs.BLOCK_CONFIG.getBooleanRoot(name, true)) {
            return block;
        }
        getBlockRegistry().register(Wunderreich.makeID(name), block);
        return block;
    }

    public static Block registerBlockOnly(String name, Block block, boolean register) {
        if (register) {
            return registerBlockOnly(name, block);
        }
        return block;
    }

    public static Block registerBlockOnly(String name, Block block) {
        return getBlockRegistry().registerBlockOnly(Wunderreich.makeID(name), block);
    }

    public static FabricItemSettings makeBlockItemSettings() {
        return getBlockRegistry().makeItemSettings();
    }

    public static void register(){

    }

}
