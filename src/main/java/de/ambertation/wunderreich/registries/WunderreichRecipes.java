package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;
import de.ambertation.wunderreich.recipes.StonecutterJsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WunderreichRecipes {
    public static final Map<ResourceLocation, JsonElement> RECIPES = new HashMap<>();

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
                .material('N', Items.NETHERITE_INGOT)
                .material('L', Blocks.LAPIS_BLOCK)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_DECORATIONS, List.of(
                        Items.QUARTZ,
                        Items.ANCIENT_DEBRIS,
                        Items.LAPIS_LAZULI
                ));

        RecipeJsonBuilder
                .create("whisperer_blank")
                .result(WunderreichItems.BLANK_WHISPERER)
                .pattern(
                        "GAG",
                        "ABA",
                        "GAG"
                )
                .material('G', Blocks.GLASS)
                .material('B', new ItemStack(Blocks.LAPIS_BLOCK, 2))
                .material('A', Items.AMETHYST_SHARD)
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                        Items.GLASS,
                        Items.LAPIS_LAZULI,
                        Items.AMETHYST_SHARD
                ));

        RecipeJsonBuilder
                .create("whisper_imprinter")
                .result(WunderreichBlocks.WHISPER_IMPRINTER)
                .pattern(
                        "ABA",
                        "DCD",
                        "DDD"
                )
                .material('C', new ItemStack(Blocks.OXIDIZED_COPPER, 2))
                .material('B', new ItemStack(Blocks.LAPIS_BLOCK, 1))
                .material('A', new ItemStack(Blocks.AMETHYST_BLOCK, 1))
                .material('D', new ItemStack(Blocks.COBBLED_DEEPSLATE, 1))
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                        Items.COPPER_INGOT,
                        Items.LAPIS_LAZULI,
                        Blocks.AMETHYST_BLOCK.asItem(),
                        Blocks.COBBLED_DEEPSLATE.asItem()
                ));

        RecipeJsonBuilder
                .create("builders_trowel")
                .result(WunderreichItems.BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', new ItemStack(Items.STICK, 1))
                .material('*', new ItemStack(Items.IRON_INGOT, 1))
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                        Items.IRON_INGOT
                ));

        RecipeJsonBuilder
                .create("diamond_builders_trowel")
                .result(WunderreichItems.DIAMOND_BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', new ItemStack(Items.STICK, 1))
                .material('*', new ItemStack(Items.DIAMOND, 1))
                .registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType.RECIPE_TOOL, List.of(
                        Items.DIAMOND
                ));
    }

    public static void createSlabRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***")
                .material('*', new ItemStack(baseBlock, 1))
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
                .material('*', new ItemStack(baseBlock, 1))
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
                .material('*', new ItemStack(baseBlock, 1))
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
                .material('*', new ItemStack(baseBlock, 1))
                .material('|', new ItemStack(fenceBlock, 1))
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
