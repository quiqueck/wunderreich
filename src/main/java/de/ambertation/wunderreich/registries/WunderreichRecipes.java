package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.recipes.RecipeJsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonElement;

import java.util.HashMap;
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
                .register();

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
                .register();

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
                .register();

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
                .register();
    }

    public static void createSlabRecipe(String name, Block baseBlock, Block block) {
        RecipeJsonBuilder
                .create(name)
                .result(block)
                .pattern("***")
                .material('*', new ItemStack(baseBlock, 1))
                .count(6)
                .register();
    }
}
