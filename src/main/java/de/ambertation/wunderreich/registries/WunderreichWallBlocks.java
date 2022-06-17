package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.config.Configs;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import de.ambertation.wunderreich.blocks.WallBlock;

import java.util.function.Function;

public class WunderreichWallBlocks {
    public static final Block WHITE_WOOL_WALL = registerWall("white_wool_wall",
                                                             Blocks.WHITE_WOOL,
                                                             WallBlock::new,
                                                             Configs.MAIN.addWalls.get());
    public static final Block LIGHT_GRAY_WOOL_WALL = registerWall("light_gray_wool_wall",
                                                                  Blocks.LIGHT_GRAY_WOOL,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block GRAY_WOOL_WALL = registerWall("gray_wool_wall",
                                                            Blocks.GRAY_WOOL,
                                                            WallBlock::new,
                                                            Configs.MAIN.addWalls.get());
    public static final Block BLACK_WOOL_WALL = registerWall("black_wool_wall",
                                                             Blocks.BLACK_WOOL,
                                                             WallBlock::new,
                                                             Configs.MAIN.addWalls.get());
    public static final Block BROWN_WOOL_WALL = registerWall("brown_wool_wall",
                                                             Blocks.BROWN_WOOL,
                                                             WallBlock::new,
                                                             Configs.MAIN.addWalls.get());
    public static final Block RED_WOOL_WALL = registerWall("red_wool_wall",
                                                           Blocks.RED_WOOL,
                                                           WallBlock::new,
                                                           Configs.MAIN.addWalls.get());
    public static final Block ORANGE_WOOL_WALL = registerWall("orange_wool_wall",
                                                              Blocks.ORANGE_WOOL,
                                                              WallBlock::new,
                                                              Configs.MAIN.addWalls.get());
    public static final Block YELLOW_WOOL_WALL = registerWall("yellow_wool_wall",
                                                              Blocks.YELLOW_WOOL,
                                                              WallBlock::new,
                                                              Configs.MAIN.addWalls.get());
    public static final Block LIME_WOOL_WALL = registerWall("lime_wool_wall",
                                                            Blocks.LIME_WOOL,
                                                            WallBlock::new,
                                                            Configs.MAIN.addWalls.get());
    public static final Block GREEN_WOOL_WALL = registerWall("green_wool_wall",
                                                             Blocks.GREEN_WOOL,
                                                             WallBlock::new,
                                                             Configs.MAIN.addWalls.get());
    public static final Block CYAN_WOOL_WALL = registerWall("cyan_wool_wall",
                                                            Blocks.CYAN_WOOL,
                                                            WallBlock::new,
                                                            Configs.MAIN.addWalls.get());
    public static final Block LIGHT_BLUE_WOOL_WALL = registerWall("light_blue_wool_wall",
                                                                  Blocks.LIGHT_BLUE_WOOL,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block BLUE_WOOL_WALL = registerWall("blue_wool_wall",
                                                            Blocks.BLUE_WOOL,
                                                            WallBlock::new,
                                                            Configs.MAIN.addWalls.get());
    public static final Block PURPLE_WOOL_WALL = registerWall("purple_wool_wall",
                                                              Blocks.PURPLE_WOOL,
                                                              WallBlock::new,
                                                              Configs.MAIN.addWalls.get());
    public static final Block MAGENTA_WOOL_WALL = registerWall("magenta_wool_wall",
                                                               Blocks.MAGENTA_WOOL,
                                                               WallBlock::new,
                                                               Configs.MAIN.addWalls.get());
    public static final Block PINK_WOOL_WALL = registerWall("pink_wool_wall",
                                                            Blocks.PINK_WOOL,
                                                            WallBlock::new,
                                                            Configs.MAIN.addWalls.get());




    public static final Block WHITE_CONCRETE_WALL = registerWall("white_concrete_wall",
                                                                 Blocks.WHITE_CONCRETE,
                                                                 WallBlock::new,
                                                                 Configs.MAIN.addWalls.get());
    public static final Block LIGHT_GRAY_CONCRETE_WALL = registerWall("light_gray_concrete_wall",
                                                                      Blocks.LIGHT_GRAY_CONCRETE,
                                                                      WallBlock::new,
                                                                      Configs.MAIN.addWalls.get());
    public static final Block GRAY_CONCRETE_WALL = registerWall("gray_concrete_wall",
                                                                Blocks.GRAY_CONCRETE,
                                                                WallBlock::new,
                                                                Configs.MAIN.addWalls.get());
    public static final Block BLACK_CONCRETE_WALL = registerWall("black_concrete_wall",
                                                                 Blocks.BLACK_CONCRETE,
                                                                 WallBlock::new,
                                                                 Configs.MAIN.addWalls.get());
    public static final Block BROWN_CONCRETE_WALL = registerWall("brown_concrete_wall",
                                                                 Blocks.BROWN_CONCRETE,
                                                                 WallBlock::new,
                                                                 Configs.MAIN.addWalls.get());
    public static final Block RED_CONCRETE_WALL = registerWall("red_concrete_wall",
                                                               Blocks.RED_CONCRETE,
                                                               WallBlock::new,
                                                               Configs.MAIN.addWalls.get());
    public static final Block ORANGE_CONCRETE_WALL = registerWall("orange_concrete_wall",
                                                                  Blocks.ORANGE_CONCRETE,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block YELLOW_CONCRETE_WALL = registerWall("yellow_concrete_wall",
                                                                  Blocks.YELLOW_CONCRETE,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block LIME_CONCRETE_WALL = registerWall("lime_concrete_wall",
                                                                Blocks.LIME_CONCRETE,
                                                                WallBlock::new,
                                                                Configs.MAIN.addWalls.get());
    public static final Block GREEN_CONCRETE_WALL = registerWall("green_concrete_wall",
                                                                 Blocks.GREEN_CONCRETE,
                                                                 WallBlock::new,
                                                                 Configs.MAIN.addWalls.get());
    public static final Block CYAN_CONCRETE_WALL = registerWall("cyan_concrete_wall",
                                                                Blocks.CYAN_CONCRETE,
                                                                WallBlock::new,
                                                                Configs.MAIN.addWalls.get());
    public static final Block LIGHT_BLUE_CONCRETE_WALL = registerWall("light_blue_concrete_wall",
                                                                      Blocks.LIGHT_BLUE_CONCRETE,
                                                                      WallBlock::new,
                                                                      Configs.MAIN.addWalls.get());
    public static final Block BLUE_CONCRETE_WALL = registerWall("blue_concrete_wall",
                                                                Blocks.BLUE_CONCRETE,
                                                                WallBlock::new,
                                                                Configs.MAIN.addWalls.get());
    public static final Block PURPLE_CONCRETE_WALL = registerWall("purple_concrete_wall",
                                                                  Blocks.PURPLE_CONCRETE,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block MAGENTA_CONCRETE_WALL = registerWall("magenta_concrete_wall",
                                                                   Blocks.MAGENTA_CONCRETE,
                                                                   WallBlock::new,
                                                                   Configs.MAIN.addWalls.get());
    public static final Block PINK_CONCRETE_WALL = registerWall("pink_concrete_wall",
                                                                Blocks.PINK_CONCRETE,
                                                                WallBlock::new,
                                                                Configs.MAIN.addWalls.get());




    public static final Block WHITE_TERRACOTTA_WALL = registerWall("white_terracotta_wall",
                                                                   Blocks.WHITE_TERRACOTTA,
                                                                   WallBlock::new,
                                                                   Configs.MAIN.addWalls.get());
    public static final Block LIGHT_GRAY_TERRACOTTA_WALL = registerWall("light_gray_terracotta_wall",
                                                                        Blocks.LIGHT_GRAY_TERRACOTTA,
                                                                        WallBlock::new,
                                                                        Configs.MAIN.addWalls.get());
    public static final Block GRAY_TERRACOTTA_WALL = registerWall("gray_terracotta_wall",
                                                                  Blocks.GRAY_TERRACOTTA,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block BLACK_TERRACOTTA_WALL = registerWall("black_terracotta_wall",
                                                                   Blocks.BLACK_TERRACOTTA,
                                                                   WallBlock::new,
                                                                   Configs.MAIN.addWalls.get());
    public static final Block BROWN_TERRACOTTA_WALL = registerWall("brown_terracotta_wall",
                                                                   Blocks.BROWN_TERRACOTTA,
                                                                   WallBlock::new,
                                                                   Configs.MAIN.addWalls.get());
    public static final Block RED_TERRACOTTA_WALL = registerWall("red_terracotta_wall",
                                                                 Blocks.RED_TERRACOTTA,
                                                                 WallBlock::new,
                                                                 Configs.MAIN.addWalls.get());
    public static final Block ORANGE_TERRACOTTA_WALL = registerWall("orange_terracotta_wall",
                                                                    Blocks.ORANGE_TERRACOTTA,
                                                                    WallBlock::new,
                                                                    Configs.MAIN.addWalls.get());
    public static final Block YELLOW_TERRACOTTA_WALL = registerWall("yellow_terracotta_wall",
                                                                    Blocks.YELLOW_TERRACOTTA,
                                                                    WallBlock::new,
                                                                    Configs.MAIN.addWalls.get());
    public static final Block LIME_TERRACOTTA_WALL = registerWall("lime_terracotta_wall",
                                                                  Blocks.LIME_TERRACOTTA,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block GREEN_TERRACOTTA_WALL = registerWall("green_terracotta_wall",
                                                                   Blocks.GREEN_TERRACOTTA,
                                                                   WallBlock::new,
                                                                   Configs.MAIN.addWalls.get());
    public static final Block CYAN_TERRACOTTA_WALL = registerWall("cyan_terracotta_wall",
                                                                  Blocks.CYAN_TERRACOTTA,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block LIGHT_BLUE_TERRACOTTA_WALL = registerWall("light_blue_terracotta_wall",
                                                                        Blocks.LIGHT_BLUE_TERRACOTTA,
                                                                        WallBlock::new,
                                                                        Configs.MAIN.addWalls.get());
    public static final Block BLUE_TERRACOTTA_WALL = registerWall("blue_terracotta_wall",
                                                                  Blocks.BLUE_TERRACOTTA,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());
    public static final Block PURPLE_TERRACOTTA_WALL = registerWall("purple_terracotta_wall",
                                                                    Blocks.PURPLE_TERRACOTTA,
                                                                    WallBlock::new,
                                                                    Configs.MAIN.addWalls.get());
    public static final Block MAGENTA_TERRACOTTA_WALL = registerWall("magenta_terracotta_wall",
                                                                     Blocks.MAGENTA_TERRACOTTA,
                                                                     WallBlock::new,
                                                                     Configs.MAIN.addWalls.get());
    public static final Block PINK_TERRACOTTA_WALL = registerWall("pink_terracotta_wall",
                                                                  Blocks.PINK_TERRACOTTA,
                                                                  WallBlock::new,
                                                                  Configs.MAIN.addWalls.get());




    public static final Block ACACIA_WALL = registerWall("acacia_wall",
                                                         Blocks.ACACIA_PLANKS,
                                                         WallBlock::new,
                                                         Configs.MAIN.addWalls.get());
    public static final Block BIRCH_WALL = registerWall("birch_wall",
                                                        Blocks.BIRCH_PLANKS,
                                                        WallBlock::new,
                                                        Configs.MAIN.addWalls.get());
    public static final Block OAK_WALL = registerWall("oak_wall",
                                                      Blocks.OAK_PLANKS,
                                                      WallBlock::new,
                                                      Configs.MAIN.addWalls.get());
    public static final Block DARK_OAK_WALL = registerWall("dark_oak_wall",
                                                           Blocks.DARK_OAK_PLANKS,
                                                           WallBlock::new,
                                                           Configs.MAIN.addWalls.get());
    public static final Block JUNGLE_WALL = registerWall("jungle_wall",
                                                         Blocks.JUNGLE_PLANKS,
                                                         WallBlock::new,
                                                         Configs.MAIN.addWalls.get());
    public static final Block CRIMSON_WALL = registerWall("crimson_wall",
                                                          Blocks.CRIMSON_PLANKS,
                                                          WallBlock::new,
                                                          Configs.MAIN.addWalls.get());
    public static final Block WARPED_WALL = registerWall("warped_wall",
                                                         Blocks.WARPED_PLANKS,
                                                         WallBlock::new,
                                                         Configs.MAIN.addWalls.get());
    public static final Block SPRUCE_WALL = registerWall("spruce_wall",
                                                         Blocks.SPRUCE_PLANKS,
                                                         WallBlock::new,
                                                         Configs.MAIN.addWalls.get());
    public static final Block MANGROVE_WALL = registerWall("mangrove_wall",
                                                           Blocks.MANGROVE_PLANKS,
                                                           WallBlock::new,
                                                           Configs.MAIN.addWalls.get());


    public static Block registerWall(String name,
                                       Block baseBlock,
                                       Function<Block, Block> creator,
                                       boolean register) {
        Block block = WunderreichBlocks.registerBlock(name, baseBlock, creator, register);
        WunderreichRecipes.createWallRecipe(name, baseBlock, block);
        return block;
    }
    static void register(){

    }
}
