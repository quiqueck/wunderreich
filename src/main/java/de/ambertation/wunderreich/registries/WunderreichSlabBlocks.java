package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.*;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Function;

public class WunderreichSlabBlocks {
    //Slabs
    public static final Block GRASS_SLAB = registerSlab(
            "grass_slab",
            Blocks.GRASS_BLOCK,
            SpreadableSnowyDirtSlab.GrassSlab::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block DIRT_SLAB = registerSlab("dirt_slab", Blocks.DIRT,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block DIRT_PATH_SLAB = registerSlab(
            "dirt_path_slab",
            Blocks.DIRT_PATH,
            DirtPathSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block COARSE_DIRT_SLAB = registerSlab("coarse_dirt_slab", Blocks.COARSE_DIRT,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block SAND_SLAB = registerSlab("sand_slab", Blocks.SAND, SandSlab::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block RED_SAND_SLAB = registerSlab("red_sand_slab", Blocks.RED_SAND, SandSlab.Red::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block WHITE_CONCRETE_SLAB = registerSlab(
            "white_concrete_slab",
            Blocks.WHITE_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_GRAY_CONCRETE_SLAB = registerSlab(
            "light_gray_concrete_slab",
            Blocks.LIGHT_GRAY_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GRAY_CONCRETE_SLAB = registerSlab(
            "gray_concrete_slab",
            Blocks.GRAY_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLACK_CONCRETE_SLAB = registerSlab(
            "black_concrete_slab",
            Blocks.BLACK_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BROWN_CONCRETE_SLAB = registerSlab(
            "brown_concrete_slab",
            Blocks.BROWN_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block RED_CONCRETE_SLAB = registerSlab(
            "red_concrete_slab",
            Blocks.RED_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block ORANGE_CONCRETE_SLAB = registerSlab(
            "orange_concrete_slab",
            Blocks.ORANGE_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block YELLOW_CONCRETE_SLAB = registerSlab(
            "yellow_concrete_slab",
            Blocks.YELLOW_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIME_CONCRETE_SLAB = registerSlab(
            "lime_concrete_slab",
            Blocks.LIME_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GREEN_CONCRETE_SLAB = registerSlab(
            "green_concrete_slab",
            Blocks.GREEN_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block CYAN_CONCRETE_SLAB = registerSlab(
            "cyan_concrete_slab",
            Blocks.CYAN_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_BLUE_CONCRETE_SLAB = registerSlab(
            "light_blue_concrete_slab",
            Blocks.LIGHT_BLUE_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLUE_CONCRETE_SLAB = registerSlab(
            "blue_concrete_slab",
            Blocks.BLUE_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PURPLE_CONCRETE_SLAB = registerSlab(
            "purple_concrete_slab",
            Blocks.PURPLE_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block MAGENTA_CONCRETE_SLAB = registerSlab(
            "magenta_concrete_slab",
            Blocks.MAGENTA_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PINK_CONCRETE_SLAB = registerSlab(
            "pink_concrete_slab",
            Blocks.PINK_CONCRETE,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block WHITE_TERRACOTTA_SLAB = registerSlab(
            "white_terracotta_slab",
            Blocks.WHITE_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_GRAY_TERRACOTTA_SLAB = registerSlab(
            "light_gray_terracotta_slab",
            Blocks.LIGHT_GRAY_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GRAY_TERRACOTTA_SLAB = registerSlab(
            "gray_terracotta_slab",
            Blocks.GRAY_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLACK_TERRACOTTA_SLAB = registerSlab(
            "black_terracotta_slab",
            Blocks.BLACK_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BROWN_TERRACOTTA_SLAB = registerSlab(
            "brown_terracotta_slab",
            Blocks.BROWN_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block RED_TERRACOTTA_SLAB = registerSlab(
            "red_terracotta_slab",
            Blocks.RED_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block ORANGE_TERRACOTTA_SLAB = registerSlab(
            "orange_terracotta_slab",
            Blocks.ORANGE_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block YELLOW_TERRACOTTA_SLAB = registerSlab(
            "yellow_terracotta_slab",
            Blocks.YELLOW_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIME_TERRACOTTA_SLAB = registerSlab(
            "lime_terracotta_slab",
            Blocks.LIME_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GREEN_TERRACOTTA_SLAB = registerSlab(
            "green_terracotta_slab",
            Blocks.GREEN_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block CYAN_TERRACOTTA_SLAB = registerSlab(
            "cyan_terracotta_slab",
            Blocks.CYAN_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_BLUE_TERRACOTTA_SLAB = registerSlab(
            "light_blue_terracotta_slab",
            Blocks.LIGHT_BLUE_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLUE_TERRACOTTA_SLAB = registerSlab(
            "blue_terracotta_slab",
            Blocks.BLUE_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PURPLE_TERRACOTTA_SLAB = registerSlab(
            "purple_terracotta_slab",
            Blocks.PURPLE_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block MAGENTA_TERRACOTTA_SLAB = registerSlab(
            "magenta_terracotta_slab",
            Blocks.MAGENTA_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PINK_TERRACOTTA_SLAB = registerSlab(
            "pink_terracotta_slab",
            Blocks.PINK_TERRACOTTA,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GLASS_SLAB = registerSlab(
            "glass_slab",
            Blocks.GLASS,
            GlassSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block WHITE_STAINED_GLASS_SLAB = registerStainedGlass(
            "white_stained_glass_slab",
            Blocks.WHITE_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_GRAY_STAINED_GLASS_SLAB = registerStainedGlass(
            "light_gray_stained_glass_slab",
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GRAY_STAINED_GLASS_SLAB = registerStainedGlass(
            "gray_stained_glass_slab",
            Blocks.GRAY_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLACK_STAINED_GLASS_SLAB = registerStainedGlass(
            "black_stained_glass_slab",
            Blocks.BLACK_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BROWN_STAINED_GLASS_SLAB = registerStainedGlass(
            "brown_stained_glass_slab",
            Blocks.BROWN_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block RED_STAINED_GLASS_SLAB = registerStainedGlass(
            "red_stained_glass_slab",
            Blocks.RED_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block ORANGE_STAINED_GLASS_SLAB = registerStainedGlass(
            "orange_stained_glass_slab",
            Blocks.ORANGE_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block YELLOW_STAINED_GLASS_SLAB = registerStainedGlass(
            "yellow_stained_glass_slab",
            Blocks.YELLOW_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIME_STAINED_GLASS_SLAB = registerStainedGlass(
            "lime_stained_glass_slab",
            Blocks.LIME_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GREEN_STAINED_GLASS_SLAB = registerStainedGlass(
            "green_stained_glass_slab",
            Blocks.GREEN_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block CYAN_STAINED_GLASS_SLAB = registerStainedGlass(
            "cyan_stained_glass_slab",
            Blocks.CYAN_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_BLUE_STAINED_GLASS_SLAB = registerStainedGlass(
            "light_blue_stained_glass_slab",
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLUE_STAINED_GLASS_SLAB = registerStainedGlass(
            "blue_stained_glass_slab",
            Blocks.BLUE_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PURPLE_STAINED_GLASS_SLAB = registerStainedGlass(
            "purple_stained_glass_slab",
            Blocks.PURPLE_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block MAGENTA_STAINED_GLASS_SLAB = registerStainedGlass(
            "magenta_stained_glass_slab",
            Blocks.MAGENTA_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PINK_STAINED_GLASS_SLAB = registerStainedGlass(
            "pink_stained_glass_slab",
            Blocks.PINK_STAINED_GLASS,
            Configs.MAIN.addSlabs.get()
    );


    public static final Block WHITE_WOOL_SLAB = registerSlab(
            "white_wool_slab",
            Blocks.WHITE_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_GRAY_WOOL_SLAB = registerSlab(
            "light_gray_wool_slab",
            Blocks.LIGHT_GRAY_WOOL,
            ConcreteSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GRAY_WOOL_SLAB = registerSlab(
            "gray_wool_slab",
            Blocks.GRAY_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLACK_WOOL_SLAB = registerSlab(
            "black_wool_slab",
            Blocks.BLACK_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BROWN_WOOL_SLAB = registerSlab(
            "brown_wool_slab",
            Blocks.BROWN_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block RED_WOOL_SLAB = registerSlab(
            "red_wool_slab",
            Blocks.RED_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block ORANGE_WOOL_SLAB = registerSlab(
            "orange_wool_slab",
            Blocks.ORANGE_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block YELLOW_WOOL_SLAB = registerSlab(
            "yellow_wool_slab",
            Blocks.YELLOW_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIME_WOOL_SLAB = registerSlab(
            "lime_wool_slab",
            Blocks.LIME_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block GREEN_WOOL_SLAB = registerSlab(
            "green_wool_slab",
            Blocks.GREEN_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block CYAN_WOOL_SLAB = registerSlab(
            "cyan_wool_slab",
            Blocks.CYAN_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block LIGHT_BLUE_WOOL_SLAB = registerSlab(
            "light_blue_wool_slab",
            Blocks.LIGHT_BLUE_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block BLUE_WOOL_SLAB = registerSlab(
            "blue_wool_slab",
            Blocks.BLUE_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PURPLE_WOOL_SLAB = registerSlab(
            "purple_wool_slab",
            Blocks.PURPLE_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block MAGENTA_WOOL_SLAB = registerSlab(
            "magenta_wool_slab",
            Blocks.MAGENTA_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );
    public static final Block PINK_WOOL_SLAB = registerSlab(
            "pink_wool_slab",
            Blocks.PINK_WOOL,
            WoolSlabBlock::new,
            Configs.MAIN.addSlabs.get()
    );

    public static Block registerSlab(String name, Block baseBlock) {
        return registerSlab(name, baseBlock, true);
    }

    public static Block registerSlab(String name, Block baseBlock, Function<Block, Block> creator) {
        return registerSlab(name, baseBlock, creator, true);
    }

    public static Block registerSlab(String name, Block baseBlock, boolean register) {
        return registerSlab(name, baseBlock, DirtSlabBlock::new, register);
    }

    public static Block registerSlab(
            String name,
            Block baseBlock,
            Function<Block, Block> creator,
            boolean register
    ) {
        Block block = WunderreichBlocks.registerBlock(name, baseBlock, creator, register);
        WunderreichRecipes.createSlabRecipe(name, baseBlock, block);
        return block;
    }

    public static Block registerStainedGlass(
            String name,
            Block baseBlock,
            boolean register
    ) {
        if (baseBlock instanceof BeaconBeamBlock stained) {
            return registerSlab(name, baseBlock, (bl) -> new StainedGlassSlabBlock(stained.getColor(), bl), register);
        }
        Wunderreich.LOGGER.warn(name + " is not a valid glass block.");
        return registerSlab(name, baseBlock, (bl) -> new StainedGlassSlabBlock(DyeColor.MAGENTA, bl), register);
    }

    static void register() {
    }
}
