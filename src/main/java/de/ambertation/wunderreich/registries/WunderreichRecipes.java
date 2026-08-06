package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.StonecutterJsonBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WunderreichRecipes {
    public static final Map<Identifier, JsonElement> RECIPES = new HashMap<>();

    public static void register() {
        RecipeJsonBuilder
                .create("wunder_kiste")
                .result(WunderreichBlocks.WUNDER_KISTE)
                .pattern(
                        "LLL",
                        "#N#",
                        "###"
                )
                .material('#', Blocks.QUARTZ_BRICKS)
                .material('N', Blocks.DIAMOND_BLOCK)
                .material('L', Blocks.LAPIS_BLOCK)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.QUARTZ,
                                Items.DIAMOND,
                                Items.DIAMOND_BLOCK,
                                Items.LAPIS_LAZULI
                        )
                );

        RecipeJsonBuilder
                .create("whisperer_blank")
                .result(WunderreichItems.BLANK_WHISPERER)
                .pattern(
                        "GAG",
                        "ABA",
                        "GAG"
                )
                .material('G', Blocks.GLASS)
                .material('B', Blocks.LAPIS_BLOCK)
                .material('A', Items.AMETHYST_SHARD)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.GLASS,
                                Items.LAPIS_LAZULI,
                                Items.AMETHYST_SHARD
                        )
                );

        RecipeJsonBuilder
                .create("whisper_imprinter")
                .result(WunderreichBlocks.WHISPER_IMPRINTER)
                .pattern(
                        "ABA",
                        "DCD",
                        "DDD"
                )
                .material('C', Blocks.COPPER_BLOCK.weathering().oxidized())
                .material('B', Blocks.LAPIS_BLOCK)
                .material('A', Blocks.AMETHYST_BLOCK)
                .material('D', Blocks.COBBLED_DEEPSLATE)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.COPPER_INGOT,
                                Items.LAPIS_LAZULI,
                                Blocks.AMETHYST_BLOCK.asItem(),
                                Blocks.COBBLED_DEEPSLATE.asItem()
                        )
                );

        RecipeJsonBuilder
                .create("builders_trowel")
                .result(WunderreichItems.BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', Items.STICK)
                .material('*', Items.IRON_INGOT)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.IRON_INGOT
                        )
                );

        RecipeJsonBuilder
                .create("diamond_builders_trowel")
                .result(WunderreichItems.DIAMOND_BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', Items.STICK)
                .material('*', Items.DIAMOND)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.DIAMOND
                        )
                );

        RecipeJsonBuilder
                .create("suction_tube")
                .result(WunderreichBlocks.SUCTION_TUBE)
                .pattern(
                        "LPL",
                        "LHL",
                        "IRI"
                )
                .material('L', Items.LAPIS_LAZULI)
                .material('I', Items.IRON_INGOT)
                .material('P', Items.PISTON)
                .material('H', Blocks.HOPPER)
                .material('R', Items.REDSTONE)
                .registerAndCreateAdvancement(
                        AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                                Items.LAPIS_LAZULI,
                                Items.PISTON,
                                Blocks.HOPPER.asItem(),
                                Items.REDSTONE
                        )
                );
    }

    public static void createSlabRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(2)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }

    public static void createStairsRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("*  ", "** ", "***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }

    public static void createWallRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***", "***")
                .material('*', baseBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }


    public static void createWoodWallRecipe(String name, Block baseBlock, Block fenceBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("* *", "|||")
                .material('*', baseBlock)
                .material('|', fenceBlock)
                .count(6)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);

        StonecutterJsonBuilder
                .create(name)
                .result(block)
                .ingredient(baseBlock)
                .count(1)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS);
    }
}
