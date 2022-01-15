package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.blocks.WhisperImprinter;
import de.ambertation.wunderreich.config.WunderreichConfigs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WunderreichBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>(2);

    public static final Block WUNDER_KISTE = registerBlock("wunder_kiste", new WunderKisteBlock());
    public static final Block WHISPER_IMPRINTER = registerBlock("whisper_imprinter", new WhisperImprinter());

    public static Collection<Block> getAllBlocks() {
        return WunderreichConfigs.BLOCK_CONFIG.getAllObjects();
    }

    public static Block registerBlock(String name, Block block, boolean register) {
        if (register) {
            return registerBlock(name, block);
        }
        return block;
    }

    public static Block registerBlock(String name, Block block) {
        if (WunderreichConfigs.BLOCK_CONFIG.newBooleanFor(name, block).get()) {
            BLOCKS.add(block);

            ResourceLocation id = Wunderreich.ID(name);

            if (block.defaultBlockState().getMaterial().isFlammable() && FlammableBlockRegistry
                    .getDefaultInstance().get(block).getBurnChance() == 0) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
            }

            Registry.register(Registry.BLOCK, id, block);


            BlockItem item = new BlockItem(block, WunderreichItems.makeItemSettings());
            if (item != Items.AIR) {
                Registry.register(Registry.ITEM, id, item);
                WunderreichItems.processItem(id, item);
            }

            processBlock(id, block);
        }
        return block;
    }

    public static void processBlock(ResourceLocation id, Block bl) {
        WunderreichTags.supplyForBlock(bl);
    }

    public static FabricBlockSettings makeStoneBlockSettings() {
        return FabricBlockSettings.of(Material.STONE);
    }

    public static void register() {

    }

}
