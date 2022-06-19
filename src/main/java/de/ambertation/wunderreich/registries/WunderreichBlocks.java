package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.*;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.WunderKisteItem;

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
import java.util.function.Function;

public class WunderreichBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>(64);

    public static final Block WUNDER_KISTE = registerBlock("wunder_kiste",
            null,
            bb -> new WunderKisteBlock(),
            bl -> new WunderKisteItem(bl));
    public static final Block WHISPER_IMPRINTER = registerBlock("whisper_imprinter",
            null,
            bb -> new WhisperImprinter(), Configs.MAIN.enableWhispers.get());



    public static Collection<Block> getAllBlocks() {
        return Configs.BLOCK_CONFIG.getAllObjects();
    }

    static Block registerBlock(String name,
                                       Block baseBlock,
                                       Function<Block, Block> creator,
                                       boolean register) {
        if (register) {
            return registerBlock(name, baseBlock, creator);
        }
        return null;
    }

    private static Block registerBlock(String name, Block baseBlock, Function<Block, Block> creator) {
        return registerBlock(name,
                baseBlock,
                creator,
                block -> new BlockItem(block, WunderreichItems.makeItemSettings()));
    }

    private static Block registerBlock(String name,
                                       Block baseBlock,
                                       Function<Block, Block> creator,
                                       Function<Block, BlockItem> itemCreator) {
        if (Configs.BLOCK_CONFIG.booleanOrDefault(name).get()) {
            final Block block = creator.apply(baseBlock);
            Configs.BLOCK_CONFIG.newBooleanFor(name, block);
            BLOCKS.add(block);

            ResourceLocation id = Wunderreich.ID(name);

            if (block.defaultBlockState().getMaterial().isFlammable() && FlammableBlockRegistry
                    .getDefaultInstance().get(block).getBurnChance() == 0) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
            }

            Registry.register(Registry.BLOCK, id, block);


            BlockItem item = itemCreator.apply(block);
            if (item != Items.AIR) {
                Registry.register(Registry.ITEM, id, item);
                WunderreichItems.processItem(id, item);
            }

            processBlock(id, block);

            return block;
        }
        return null;
    }

    public static void processBlock(ResourceLocation id, Block bl) {
        WunderreichTags.supplyForBlock(bl);
    }

    public static FabricBlockSettings makeStoneBlockSettings() {
        return FabricBlockSettings.of(Material.STONE);
    }

    public static void register() {
        WunderreichSlabBlocks.register();
        WunderreichStairBlocks.register();
        WunderreichWallBlocks.register();
    }

}
