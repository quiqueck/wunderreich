package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.*;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class WunderreichBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>(2);

    public static final Block WUNDER_KISTE = registerBlock("wunder_kiste", new WunderKisteBlock());
    public static final Block WHISPER_IMPRINTER = registerBlock("whisper_imprinter", new WhisperImprinter());

    //Slabs
    public static final Block GRASS_SLAB = registerSlab("grass_slab",
                                                        Blocks.GRASS_BLOCK,
                                                        SpreadableSnowyDirtSlab.GrassSlab::new,
                                                        Configs.MAIN.addDirtSlabs.get());
    public static final Block DIRT_SLAB = registerSlab("dirt_slab", Blocks.DIRT,
                                                       Configs.MAIN.addDirtSlabs.get());
    public static final Block COARSE_DIRT_SLAB = registerSlab("coarse_dirt_slab", Blocks.COARSE_DIRT,
                                                              Configs.MAIN.addDirtSlabs.get());
    public static final Block SAND_SLAB = registerSlab("sand_slab", Blocks.SAND, SandSlab::new,
                                                       Configs.MAIN.addDirtSlabs.get());
    public static final Block RED_SAND_SLAB = registerSlab("red_sand_slab", Blocks.RED_SAND, SandSlab.Red::new,
                                                           Configs.MAIN.addDirtSlabs.get());

    public static final Block WHITE_CONCRETE_SLAB = registerSlab("white_concrete_slab",
                                                                 Blocks.WHITE_CONCRETE,
                                                                 ConcreteSlabBlock::new,
                                                                 Configs.MAIN.addDirtSlabs.get());
    public static final Block LIGHT_GRAY_CONCRETE_SLAB = registerSlab("light_gray_concrete_slab",
                                                                      Blocks.LIGHT_GRAY_CONCRETE,
                                                                      ConcreteSlabBlock::new,
                                                                      Configs.MAIN.addDirtSlabs.get());
    public static final Block GRAY_CONCRETE_SLAB = registerSlab("gray_concrete_slab",
                                                                Blocks.GRAY_CONCRETE,
                                                                ConcreteSlabBlock::new,
                                                                Configs.MAIN.addDirtSlabs.get());
    public static final Block BLACK_CONCRETE_SLAB = registerSlab("black_concrete_slab",
                                                                 Blocks.BLACK_CONCRETE,
                                                                 ConcreteSlabBlock::new,
                                                                 Configs.MAIN.addDirtSlabs.get());
    public static final Block BROWN_CONCRETE_SLAB = registerSlab("brown_concrete_slab",
                                                                 Blocks.BROWN_CONCRETE,
                                                                 ConcreteSlabBlock::new,
                                                                 Configs.MAIN.addDirtSlabs.get());
    public static final Block RED_CONCRETE_SLAB = registerSlab("red_concrete_slab",
                                                               Blocks.RED_CONCRETE,
                                                               ConcreteSlabBlock::new,
                                                               Configs.MAIN.addDirtSlabs.get());
    public static final Block ORANGE_CONCRETE_SLAB = registerSlab("orange_concrete_slab",
                                                                  Blocks.ORANGE_CONCRETE,
                                                                  ConcreteSlabBlock::new,
                                                                  Configs.MAIN.addDirtSlabs.get());
    public static final Block YELLOW_CONCRETE_SLAB = registerSlab("yellow_concrete_slab",
                                                                  Blocks.YELLOW_CONCRETE,
                                                                  ConcreteSlabBlock::new,
                                                                  Configs.MAIN.addDirtSlabs.get());
    public static final Block LIME_CONCRETE_SLAB = registerSlab("lime_concrete_slab",
                                                                Blocks.LIME_CONCRETE,
                                                                ConcreteSlabBlock::new,
                                                                Configs.MAIN.addDirtSlabs.get());
    public static final Block GREEN_CONCRETE_SLAB = registerSlab("green_concrete_slab",
                                                                 Blocks.GREEN_CONCRETE,
                                                                 ConcreteSlabBlock::new,
                                                                 Configs.MAIN.addDirtSlabs.get());
    public static final Block CYAN_CONCRETE_SLAB = registerSlab("cyan_concrete_slab",
                                                                Blocks.CYAN_CONCRETE,
                                                                ConcreteSlabBlock::new,
                                                                Configs.MAIN.addDirtSlabs.get());
    public static final Block LIGHT_BLUE_CONCRETE_SLAB = registerSlab("light_blue_concrete_slab",
                                                                      Blocks.LIGHT_BLUE_CONCRETE,
                                                                      ConcreteSlabBlock::new,
                                                                      Configs.MAIN.addDirtSlabs.get());
    public static final Block BLUE_CONCRETE_SLAB = registerSlab("blue_concrete_slab",
                                                                Blocks.BLUE_CONCRETE,
                                                                ConcreteSlabBlock::new,
                                                                Configs.MAIN.addDirtSlabs.get());
    public static final Block PURPLE_CONCRETE_SLAB = registerSlab("purple_concrete_slab",
                                                                  Blocks.PURPLE_CONCRETE,
                                                                  ConcreteSlabBlock::new,
                                                                  Configs.MAIN.addDirtSlabs.get());
    public static final Block MAGENTA_CONCRETE_SLAB = registerSlab("magenta_concrete_slab",
                                                                   Blocks.MAGENTA_CONCRETE,
                                                                   ConcreteSlabBlock::new,
                                                                   Configs.MAIN.addDirtSlabs.get());
    public static final Block PINK_CONCRETE_SLAB = registerSlab("pink_concrete_slab",
                                                                Blocks.PINK_CONCRETE,
                                                                ConcreteSlabBlock::new,
                                                                Configs.MAIN.addDirtSlabs.get());

    public static Collection<Block> getAllBlocks() {
        return Configs.BLOCK_CONFIG.getAllObjects();
    }

    public static Block registerSlab(String name, Block baseBlock) {
        return registerSlab(name, baseBlock, true);
    }

    public static Block registerSlab(String name, Block baseBlock, Function<Block, DirtSlabBlock> creator) {
        return registerSlab(name, baseBlock, creator, true);
    }

    public static Block registerSlab(String name, Block baseBlock, boolean register) {
        return registerSlab(name, baseBlock, DirtSlabBlock::new, register);
    }

    public static Block registerSlab(String name,
                                     Block baseBlock,
                                     Function<Block, DirtSlabBlock> creator,
                                     boolean register) {
        Block block = registerBlock(name, creator.apply(baseBlock), register);
        WunderreichRecipes.createSlabRecipe(name, baseBlock, block);
        return block;
    }

    public static Block registerBlock(String name, Block block, boolean register) {
        if (register) {
            return registerBlock(name, block);
        }
        return block;
    }

    public static Block registerBlock(String name, Block block) {
        if (Configs.BLOCK_CONFIG.newBooleanFor(name, block).get()) {
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
