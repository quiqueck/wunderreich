package de.ambertation.wunderreich.registries;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import ru.bclib.recipes.GridRecipe;

public class WunderreichReceipes {
    public static void register(){
        GridRecipe.make(Wunderreich.makeID("whisperer_blank"), WunderreichItems.BLANK_WHISPERER)
                .checkConfig(Configs.RECIPE_CONFIG)
                .setShape("GAG", "ABA", "GAG")
                .addMaterial('G', Blocks.GLASS)
                .addMaterial('B', new ItemStack(Blocks.LAPIS_BLOCK, 2))
                .addMaterial('A', Items.AMETHYST_SHARD)
                .build();

        GridRecipe.make(Wunderreich.makeID("whsiper_imprinter"), WunderreichBlocks.WHISPER_IMPRINTER)
                .checkConfig(Configs.RECIPE_CONFIG)
                .setShape("ABA", "DCD", "DDD")
                .addMaterial('C', new ItemStack(Blocks.OXIDIZED_COPPER, 2))
                .addMaterial('B', new ItemStack(Blocks.LAPIS_BLOCK, 1))
                .addMaterial('A', new ItemStack(Blocks.AMETHYST_BLOCK, 1))
                .addMaterial('D', new ItemStack(Blocks.COBBLED_DEEPSLATE, 1))
                .build();
    }
}
