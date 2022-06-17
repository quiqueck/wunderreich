package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.blocks.StairBlock;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Function;

public class WunderreichStairBlocks {
    public static final Block PINK_CONCRETE_STAIRS = registerStairs("pink_concrete_stairs",
                                                                    Blocks.PINK_CONCRETE,
                                                                    StairBlock::new,
                                                                    Configs.MAIN.addStairs.get());
    // Stairs
    public static final Block WHITE_CONCRETE_STAIRS = registerStairs("white_concrete_stairs",
                                                                     Blocks.WHITE_CONCRETE,
                                                                     StairBlock::new,
                                                                     Configs.MAIN.addStairs.get());
    public static final Block LIGHT_GRAY_CONCRETE_STAIRS = registerStairs("light_gray_concrete_stairs",
                                                                          Blocks.LIGHT_GRAY_CONCRETE,
                                                                          StairBlock::new,
                                                                          Configs.MAIN.addStairs.get());
    public static final Block GRAY_CONCRETE_STAIRS = registerStairs("gray_concrete_stairs",
                                                                    Blocks.GRAY_CONCRETE,
                                                                    StairBlock::new,
                                                                    Configs.MAIN.addStairs.get());
    public static final Block BLACK_CONCRETE_STAIRS = registerStairs("black_concrete_stairs",
                                                                     Blocks.BLACK_CONCRETE,
                                                                     StairBlock::new,
                                                                     Configs.MAIN.addStairs.get());
    public static final Block BROWN_CONCRETE_STAIRS = registerStairs("brown_concrete_stairs",
                                                                     Blocks.BROWN_CONCRETE,
                                                                     StairBlock::new,
                                                                     Configs.MAIN.addStairs.get());
    public static final Block RED_CONCRETE_STAIRS = registerStairs("red_concrete_stairs",
                                                                   Blocks.RED_CONCRETE,
                                                                   StairBlock::new,
                                                                   Configs.MAIN.addStairs.get());
    public static final Block ORANGE_CONCRETE_STAIRS = registerStairs("orange_concrete_stairs",
                                                                      Blocks.ORANGE_CONCRETE,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block YELLOW_CONCRETE_STAIRS = registerStairs("yellow_concrete_stairs",
                                                                      Blocks.YELLOW_CONCRETE,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block LIME_CONCRETE_STAIRS = registerStairs("lime_concrete_stairs",
                                                                    Blocks.LIME_CONCRETE,
                                                                    StairBlock::new,
                                                                    Configs.MAIN.addStairs.get());
    public static final Block GREEN_CONCRETE_STAIRS = registerStairs("green_concrete_stairs",
                                                                     Blocks.GREEN_CONCRETE,
                                                                     StairBlock::new,
                                                                     Configs.MAIN.addStairs.get());
    public static final Block CYAN_CONCRETE_STAIRS = registerStairs("cyan_concrete_stairs",
                                                                    Blocks.CYAN_CONCRETE,
                                                                    StairBlock::new,
                                                                    Configs.MAIN.addStairs.get());
    public static final Block LIGHT_BLUE_CONCRETE_STAIRS = registerStairs("light_blue_concrete_stairs",
                                                                          Blocks.LIGHT_BLUE_CONCRETE,
                                                                          StairBlock::new,
                                                                          Configs.MAIN.addStairs.get());
    public static final Block BLUE_CONCRETE_STAIRS = registerStairs("blue_concrete_stairs",
                                                                    Blocks.BLUE_CONCRETE,
                                                                    StairBlock::new,
                                                                    Configs.MAIN.addStairs.get());
    public static final Block PURPLE_CONCRETE_STAIRS = registerStairs("purple_concrete_stairs",
                                                                      Blocks.PURPLE_CONCRETE,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block MAGENTA_CONCRETE_STAIRS = registerStairs("magenta_concrete_stairs",
                                                                       Blocks.MAGENTA_CONCRETE,
                                                                       StairBlock::new,
                                                                       Configs.MAIN.addStairs.get());






    public static final Block WHITE_TERRACOTTA_STAIRS = registerStairs("white_terracotta_stairs",
                                                                       Blocks.WHITE_TERRACOTTA,
                                                                       StairBlock::new,
                                                                       Configs.MAIN.addStairs.get());
    public static final Block LIGHT_GRAY_TERRACOTTA_STAIRS = registerStairs("light_gray_terracotta_stairs",
                                                                            Blocks.LIGHT_GRAY_TERRACOTTA,
                                                                            StairBlock::new,
                                                                            Configs.MAIN.addStairs.get());
    public static final Block GRAY_TERRACOTTA_STAIRS = registerStairs("gray_terracotta_stairs",
                                                                      Blocks.GRAY_TERRACOTTA,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block BLACK_TERRACOTTA_STAIRS = registerStairs("black_terracotta_stairs",
                                                                       Blocks.BLACK_TERRACOTTA,
                                                                       StairBlock::new,
                                                                       Configs.MAIN.addStairs.get());
    public static final Block BROWN_TERRACOTTA_STAIRS = registerStairs("brown_terracotta_stairs",
                                                                       Blocks.BROWN_TERRACOTTA,
                                                                       StairBlock::new,
                                                                       Configs.MAIN.addStairs.get());
    public static final Block RED_TERRACOTTA_STAIRS = registerStairs("red_terracotta_stairs",
                                                                     Blocks.RED_TERRACOTTA,
                                                                     StairBlock::new,
                                                                     Configs.MAIN.addStairs.get());
    public static final Block ORANGE_TERRACOTTA_STAIRS = registerStairs("orange_terracotta_stairs",
                                                                        Blocks.ORANGE_TERRACOTTA,
                                                                        StairBlock::new,
                                                                        Configs.MAIN.addStairs.get());
    public static final Block YELLOW_TERRACOTTA_STAIRS = registerStairs("yellow_terracotta_stairs",
                                                                        Blocks.YELLOW_TERRACOTTA,
                                                                        StairBlock::new,
                                                                        Configs.MAIN.addStairs.get());
    public static final Block LIME_TERRACOTTA_STAIRS = registerStairs("lime_terracotta_stairs",
                                                                      Blocks.LIME_TERRACOTTA,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block GREEN_TERRACOTTA_STAIRS = registerStairs("green_terracotta_stairs",
                                                                       Blocks.GREEN_TERRACOTTA,
                                                                       StairBlock::new,
                                                                       Configs.MAIN.addStairs.get());
    public static final Block CYAN_TERRACOTTA_STAIRS = registerStairs("cyan_terracotta_stairs",
                                                                      Blocks.CYAN_TERRACOTTA,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block LIGHT_BLUE_TERRACOTTA_STAIRS = registerStairs("light_blue_terracotta_stairs",
                                                                            Blocks.LIGHT_BLUE_TERRACOTTA,
                                                                            StairBlock::new,
                                                                            Configs.MAIN.addStairs.get());
    public static final Block BLUE_TERRACOTTA_STAIRS = registerStairs("blue_terracotta_stairs",
                                                                      Blocks.BLUE_TERRACOTTA,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block PURPLE_TERRACOTTA_STAIRS = registerStairs("purple_terracotta_stairs",
                                                                        Blocks.PURPLE_TERRACOTTA,
                                                                        StairBlock::new,
                                                                        Configs.MAIN.addStairs.get());
    public static final Block MAGENTA_TERRACOTTA_STAIRS = registerStairs("magenta_terracotta_stairs",
                                                                         Blocks.MAGENTA_TERRACOTTA,
                                                                         StairBlock::new,
                                                                         Configs.MAIN.addStairs.get());
    public static final Block PINK_TERRACOTTA_STAIRS = registerStairs("pink_terracotta_stairs",
                                                                      Blocks.PINK_TERRACOTTA,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());





    public static final Block WHITE_WOOL_STAIRS = registerStairs("white_wool_stairs",
                                                                 Blocks.WHITE_WOOL,
                                                                 StairBlock::new,
                                                                 Configs.MAIN.addStairs.get());
    public static final Block LIGHT_GRAY_WOOL_STAIRS = registerStairs("light_gray_wool_stairs",
                                                                      Blocks.LIGHT_GRAY_WOOL,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block GRAY_WOOL_STAIRS = registerStairs("gray_wool_stairs",
                                                                Blocks.GRAY_WOOL,
                                                                StairBlock::new,
                                                                Configs.MAIN.addStairs.get());
    public static final Block BLACK_WOOL_STAIRS = registerStairs("black_wool_stairs",
                                                                 Blocks.BLACK_WOOL,
                                                                 StairBlock::new,
                                                                 Configs.MAIN.addStairs.get());
    public static final Block BROWN_WOOL_STAIRS = registerStairs("brown_wool_stairs",
                                                                 Blocks.BROWN_WOOL,
                                                                 StairBlock::new,
                                                                 Configs.MAIN.addStairs.get());
    public static final Block RED_WOOL_STAIRS = registerStairs("red_wool_stairs",
                                                               Blocks.RED_WOOL,
                                                               StairBlock::new,
                                                               Configs.MAIN.addStairs.get());
    public static final Block ORANGE_WOOL_STAIRS = registerStairs("orange_wool_stairs",
                                                                  Blocks.ORANGE_WOOL,
                                                                  StairBlock::new,
                                                                  Configs.MAIN.addStairs.get());
    public static final Block YELLOW_WOOL_STAIRS = registerStairs("yellow_wool_stairs",
                                                                  Blocks.YELLOW_WOOL,
                                                                  StairBlock::new,
                                                                  Configs.MAIN.addStairs.get());
    public static final Block LIME_WOOL_STAIRS = registerStairs("lime_wool_stairs",
                                                                Blocks.LIME_WOOL,
                                                                StairBlock::new,
                                                                Configs.MAIN.addStairs.get());
    public static final Block GREEN_WOOL_STAIRS = registerStairs("green_wool_stairs",
                                                                 Blocks.GREEN_WOOL,
                                                                 StairBlock::new,
                                                                 Configs.MAIN.addStairs.get());
    public static final Block CYAN_WOOL_STAIRS = registerStairs("cyan_wool_stairs",
                                                                Blocks.CYAN_WOOL,
                                                                StairBlock::new,
                                                                Configs.MAIN.addStairs.get());
    public static final Block LIGHT_BLUE_WOOL_STAIRS = registerStairs("light_blue_wool_stairs",
                                                                      Blocks.LIGHT_BLUE_WOOL,
                                                                      StairBlock::new,
                                                                      Configs.MAIN.addStairs.get());
    public static final Block BLUE_WOOL_STAIRS = registerStairs("blue_wool_stairs",
                                                                Blocks.BLUE_WOOL,
                                                                StairBlock::new,
                                                                Configs.MAIN.addStairs.get());
    public static final Block PURPLE_WOOL_STAIRS = registerStairs("purple_wool_stairs",
                                                                  Blocks.PURPLE_WOOL,
                                                                  StairBlock::new,
                                                                  Configs.MAIN.addStairs.get());
    public static final Block MAGENTA_WOOL_STAIRS = registerStairs("magenta_wool_stairs",
                                                                   Blocks.MAGENTA_WOOL,
                                                                   StairBlock::new,
                                                                   Configs.MAIN.addStairs.get());
    public static final Block PINK_WOOL_STAIRS = registerStairs("pink_wool_stairs",
                                                                Blocks.PINK_WOOL,
                                                                StairBlock::new,
                                                                Configs.MAIN.addStairs.get());

    public static Block registerStairs(String name,
                                       Block baseBlock,
                                       Function<Block, Block> creator,
                                       boolean register) {
        Block block = WunderreichBlocks.registerBlock(name, baseBlock, creator, register);
        WunderreichRecipes.createStairsRecipe(name, baseBlock, block);
        return block;
    }

    static void register(){}
}
