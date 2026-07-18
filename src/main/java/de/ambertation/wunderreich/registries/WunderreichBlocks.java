package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.SuctionTube;
import de.ambertation.wunderreich.blocks.WhisperImprinter;
import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.items.WunderKisteItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class WunderreichBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>(64);
    public static final Block WUNDER_KISTE = registerBlock(
            "wunder_kiste",
            null,
            (baseBlock, key) -> new WunderKisteBlock(key),
            (baseBlock, key) -> new WunderKisteItem(baseBlock, key)
    );
    public static final Block WHISPER_IMPRINTER = registerBlock(
            "whisper_imprinter",
            null,
            (baseBlock, key) -> new WhisperImprinter(key), Configs.MAIN.enableWhispers.get()
    );
    public static final Block SUCTION_TUBE = registerBlock(
            "suction_tube",
            null,
            (baseBlock, key) -> new SuctionTube(key)
    );


    public static Collection<Block> getAllBlocks() {
        return Configs.BLOCK_CONFIG.getAllObjects();
    }

    static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register
    ) {
        if (register) {
            return registerBlock(name, baseBlock, creator);
        }
        return null;
    }

    private static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator
    ) {
        return registerBlock(
                name,
                baseBlock,
                creator,
                (block, key) -> new BlockItem(block, WunderreichItems.makeItemSettings().setId(key))
        );
    }

    private static Block registerBlock(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            BiFunction<Block, ResourceKey<Item>, BlockItem> itemCreator
    ) {
        if (Configs.BLOCK_CONFIG.booleanOrDefault(name).get()) {
            final Identifier id = Wunderreich.ID(name);
            final ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);


            final Block block = creator.apply(baseBlock, key);
            Configs.BLOCK_CONFIG.newBooleanFor(name, block);
            BLOCKS.add(block);


            if (block.defaultBlockState().ignitedByLava() && FlammableBlockRegistry
                    .getDefaultInstance().get(block).getBurnOdds() == 0) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
            }

            Registry.register(BuiltInRegistries.BLOCK, id, block);

            final ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
            BlockItem item = itemCreator.apply(block, itemKey);
            if (item != Items.AIR) {
                Registry.register(BuiltInRegistries.ITEM, id, item);
                WunderreichItems.processItem(id, item);
            }

            processBlock(id, block);

            return block;
        }
        return null;
    }

    public static void processBlock(Identifier id, Block bl) {

    }

    public static BlockBehaviour.Properties makeStoneBlockSettings() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
    }

    public static void register() {
        WunderreichSlabBlocks.register();
        WunderreichStairBlocks.register();
        WunderreichWallBlocks.register();
    }

}
