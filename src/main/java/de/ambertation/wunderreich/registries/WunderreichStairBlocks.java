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


    public static final Block WHITE_WOOL_STAIRS = registerWoolStairs(
            "white_wool_stairs",
            DyeColor.WHITE,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_GRAY_WOOL_STAIRS = registerWoolStairs(
            "light_gray_wool_stairs",
            DyeColor.LIGHT_GRAY,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GRAY_WOOL_STAIRS = registerWoolStairs(
            "gray_wool_stairs",
            DyeColor.GRAY,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLACK_WOOL_STAIRS = registerWoolStairs(
            "black_wool_stairs",
            DyeColor.BLACK,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BROWN_WOOL_STAIRS = registerWoolStairs(
            "brown_wool_stairs",
            DyeColor.BROWN,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block RED_WOOL_STAIRS = registerWoolStairs(
            "red_wool_stairs",
            DyeColor.RED,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block ORANGE_WOOL_STAIRS = registerWoolStairs(
            "orange_wool_stairs",
            DyeColor.ORANGE,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block YELLOW_WOOL_STAIRS = registerWoolStairs(
            "yellow_wool_stairs",
            DyeColor.YELLOW,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIME_WOOL_STAIRS = registerWoolStairs(
            "lime_wool_stairs",
            DyeColor.LIME,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block GREEN_WOOL_STAIRS = registerWoolStairs(
            "green_wool_stairs",
            DyeColor.GREEN,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block CYAN_WOOL_STAIRS = registerWoolStairs(
            "cyan_wool_stairs",
            DyeColor.CYAN,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block LIGHT_BLUE_WOOL_STAIRS = registerWoolStairs(
            "light_blue_wool_stairs",
            DyeColor.LIGHT_BLUE,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block BLUE_WOOL_STAIRS = registerWoolStairs(
            "blue_wool_stairs",
            DyeColor.BLUE,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PURPLE_WOOL_STAIRS = registerWoolStairs(
            "purple_wool_stairs",
            DyeColor.PURPLE,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block MAGENTA_WOOL_STAIRS = registerWoolStairs(
            "magenta_wool_stairs",
            DyeColor.MAGENTA,
            WoolStairBlock::new,
            Configs.MAIN.addStairs.get()
    );
    public static final Block PINK_WOOL_STAIRS = registerWoolStairs(
            "pink_wool_stairs",
            DyeColor.PINK,
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
        return registerStairs(name, baseBlock, creator, register, true);
    }

    // Vanilla now ships its own recipes for some sub types (e.g. wool stairs since 26.3) -
    // addRecipe lets us keep the block (existing worlds may still reference it) without
    // registering a redundant duplicate recipe.
    public static Block registerStairs(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register,
            boolean addRecipe
    ) {
        return registerStairs(name, baseBlock, creator, register, addRecipe, null);
    }

    // Blocks vanilla now provides its own version of (e.g. wool stairs since 26.3) are kept around,
    // itemless-but-not-quite: they still exist and can still be given/held so existing worlds and
    // inventories keep working, but they're hidden from the creative tab and, when broken, drop the
    // vanilla replacement instead of themselves - see WunderreichBlocks#registerVanillaReplacement.
    public static Block registerStairs(
            String name,
            Block baseBlock,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register,
            boolean addRecipe,
            Block vanillaReplacement
    ) {
        Block block = WunderreichBlocks.registerBlock(name, baseBlock, creator, register);
        if (Wunderreich.isDatagen()) {
            if (STAIR_BLOCKS == null) {
                STAIR_BLOCKS = new LinkedList<>();
            }
            STAIR_BLOCKS.add(new Block[]{block, baseBlock});
        }
        if (addRecipe) {
            WunderreichRecipes.createStairsRecipe(name, baseBlock, block);
        }
        if (vanillaReplacement != null) {
            WunderreichBlocks.registerVanillaReplacement(block, vanillaReplacement);
        }
        return block;
    }

    private static Block registerWoolStairs(
            String name,
            DyeColor color,
            BiFunction<Block, ResourceKey<Block>, Block> creator,
            boolean register
    ) {
        return registerStairs(
                name,
                Blocks.WOOL.pick(color),
                creator,
                register,
                false,
                Blocks.WOOL_STAIRS.pick(color)
        );
    }

    private static List<Block[]> STAIR_BLOCKS;

    public static List<Block[]> getStairBlocks() {
        return STAIR_BLOCKS == null ? List.of() : STAIR_BLOCKS;
    }

    static void register() {
    }
}
