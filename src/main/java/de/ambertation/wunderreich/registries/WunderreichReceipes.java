package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.utils.RecipeJsonBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class WunderreichReceipes {
    public static void register() {
        RecipeJsonBuilder
                .create("box_of_eir")
                .result(WunderreichItems.BLANK_WHISPERER)
                .pattern(
                        "LLL",
                        "#N#",
                        "###"
                )
                .material('#', Blocks.QUARTZ_BRICKS)
                .material('N', Items.NETHERITE_INGOT)
                .material('L', Blocks.LAPIS_BLOCK)
                .build();

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
                .build();

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
                .material('D', new ItemStack(Blocks.COBBLED_DEEPSLATE, 1));

        RecipeJsonBuilder
                .create("builders_trowel")
                .result(WunderreichItems.BUILDERS_TROWEL)
                .pattern(
                        "***",
                        " * ",
                        " # "
                )
                .material('#', new ItemStack(Items.STICK, 1))
                .material('*', new ItemStack(Items.IRON_INGOT, 1));
    }
}
