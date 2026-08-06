package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blocks.AmethystStairBlock;
import de.ambertation.wunderreich.blocks.StairBlock;
import de.ambertation.wunderreich.blocks.WoolStairBlock;
import de.ambertation.wunderreich.config.Configs;

import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class WunderreichStairBlocks {
    public static final Block PINK_CONCRETE_STAIRS = registerStairs(
            "pink_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.PINK),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    // Stairs
    public static final Block WHITE_CONCRETE_STAIRS = registerStairs(
            "white_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.WHITE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_GRAY_CONCRETE_STAIRS = registerStairs(
            "light_gray_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GRAY_CONCRETE_STAIRS = registerStairs(
            "gray_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.GRAY),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLACK_CONCRETE_STAIRS = registerStairs(
            "black_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.BLACK),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BROWN_CONCRETE_STAIRS = registerStairs(
            "brown_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.BROWN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block RED_CONCRETE_STAIRS = registerStairs(
            "red_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.RED),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block ORANGE_CONCRETE_STAIRS = registerStairs(
            "orange_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.ORANGE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block YELLOW_CONCRETE_STAIRS = registerStairs(
            "yellow_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.YELLOW),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIME_CONCRETE_STAIRS = registerStairs(
            "lime_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.LIME),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GREEN_CONCRETE_STAIRS = registerStairs(
            "green_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.GREEN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block CYAN_CONCRETE_STAIRS = registerStairs(
            "cyan_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.CYAN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_BLUE_CONCRETE_STAIRS = registerStairs(
            "light_blue_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLUE_CONCRETE_STAIRS = registerStairs(
            "blue_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.BLUE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PURPLE_CONCRETE_STAIRS = registerStairs(
            "purple_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.PURPLE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block MAGENTA_CONCRETE_STAIRS = registerStairs(
            "magenta_concrete_stairs",
            Blocks.CONCRETE.pick(DyeColor.MAGENTA),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );


    public static final Block WHITE_TERRACOTTA_STAIRS = registerStairs(
            "white_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_GRAY_TERRACOTTA_STAIRS = registerStairs(
            "light_gray_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GRAY_TERRACOTTA_STAIRS = registerStairs(
            "gray_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLACK_TERRACOTTA_STAIRS = registerStairs(
            "black_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BROWN_TERRACOTTA_STAIRS = registerStairs(
            "brown_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block RED_TERRACOTTA_STAIRS = registerStairs(
            "red_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.RED),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block ORANGE_TERRACOTTA_STAIRS = registerStairs(
            "orange_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block YELLOW_TERRACOTTA_STAIRS = registerStairs(
            "yellow_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIME_TERRACOTTA_STAIRS = registerStairs(
            "lime_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GREEN_TERRACOTTA_STAIRS = registerStairs(
            "green_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block CYAN_TERRACOTTA_STAIRS = registerStairs(
            "cyan_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_BLUE_TERRACOTTA_STAIRS = registerStairs(
            "light_blue_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLUE_TERRACOTTA_STAIRS = registerStairs(
            "blue_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PURPLE_TERRACOTTA_STAIRS = registerStairs(
            "purple_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block MAGENTA_TERRACOTTA_STAIRS = registerStairs(
            "magenta_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PINK_TERRACOTTA_STAIRS = registerStairs(
            "pink_terracotta_stairs",
            Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK),
            StairBlock::new,
            Configs.MAIN.addStairs.get()
    );


    public static final Block WHITE_WOOL_STAIRS = registerStairs(
            "white_wool_stairs",
            Blocks.WOOL.pick(DyeColor.WHITE),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_GRAY_WOOL_STAIRS = registerStairs(
            "light_gray_wool_stairs",
            Blocks.WOOL.pick(DyeColor.LIGHT_GRAY),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GRAY_WOOL_STAIRS = registerStairs(
            "gray_wool_stairs",
            Blocks.WOOL.pick(DyeColor.GRAY),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLACK_WOOL_STAIRS = registerStairs(
            "black_wool_stairs",
            Blocks.WOOL.pick(DyeColor.BLACK),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BROWN_WOOL_STAIRS = registerStairs(
            "brown_wool_stairs",
            Blocks.WOOL.pick(DyeColor.BROWN),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block RED_WOOL_STAIRS = registerStairs(
            "red_wool_stairs",
            Blocks.WOOL.pick(DyeColor.RED),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block ORANGE_WOOL_STAIRS = registerStairs(
            "orange_wool_stairs",
            Blocks.WOOL.pick(DyeColor.ORANGE),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block YELLOW_WOOL_STAIRS = registerStairs(
            "yellow_wool_stairs",
            Blocks.WOOL.pick(DyeColor.YELLOW),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIME_WOOL_STAIRS = registerStairs(
            "lime_wool_stairs",
            Blocks.WOOL.pick(DyeColor.LIME),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GREEN_WOOL_STAIRS = registerStairs(
            "green_wool_stairs",
            Blocks.WOOL.pick(DyeColor.GREEN),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block CYAN_WOOL_STAIRS = registerStairs(
            "cyan_wool_stairs",
            Blocks.WOOL.pick(DyeColor.CYAN),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_BLUE_WOOL_STAIRS = registerStairs(
            "light_blue_wool_stairs",
            Blocks.WOOL.pick(DyeColor.LIGHT_BLUE),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLUE_WOOL_STAIRS = registerStairs(
            "blue_wool_stairs",
            Blocks.WOOL.pick(DyeColor.BLUE),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PURPLE_WOOL_STAIRS = registerStairs(
            "purple_wool_stairs",
            Blocks.WOOL.pick(DyeColor.PURPLE),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block MAGENTA_WOOL_STAIRS = registerStairs(
            "magenta_wool_stairs",
            Blocks.WOOL.pick(DyeColor.MAGENTA),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PINK_WOOL_STAIRS = registerStairs(
            "pink_wool_stairs",
            Blocks.WOOL.pick(DyeColor.PINK),
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );


    public static final Block AMETHYST_STAIRS = registerStairs(
            "amethyst_stairs",
            Blocks.AMETHYST_BLOCK,
            AmethystStairBlock::new,
            Configs.MAIN.addStairs.get()
    );

    public static Block registerStairs(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register
    ) {
        Block block = WunderreichBlocks.registerBlock(name, baseBlock, creator, register);
        if (Wunderreich.isDatagen()) {
            if (STAIR_BLOCKS == null) {
                STAIR_BLOCKS = new LinkedList<>();
            }
            STAIR_BLOCKS.add(new Block[]{block, baseBlock});
        }
        WunderreichRecipes.createStairsRecipe(name, baseBlock, block);
        return block;
    }

    private static List<Block[]> STAIR_BLOCKS;

    public static List<Block[]> getStairBlocks() {
        return STAIR_BLOCKS == null ? List.of() : STAIR_BLOCKS;
    }

    static void register() {
    }
}
