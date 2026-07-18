package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public class ImprinterDisplay extends BasicDisplay {
    public static final DisplaySerializer<ImprinterDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(ImprinterDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(ImprinterDisplay::getOutputEntries)
            ).apply(instance, ImprinterDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), ImprinterDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), ImprinterDisplay::getOutputEntries,
                    ImprinterDisplay::new
            )
    );

    public ImprinterDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
    }

    public static ImprinterDisplay of(ImprinterRecipe recipe) {
        return new ImprinterDisplay(
                List.of(
                        // COST_A_SLOT: the imprint input (book / overridden cost)
                        EntryIngredients.of(recipe.getInput()),
                        // COST_B_SLOT: the blank whisperer that gets trained
                        EntryIngredients.of(new ItemStack(WunderreichItems.BLANK_WHISPERER))
                ),
                // result: the trained whisperer
                List.of(EntryIngredients.of(recipe.output))
        );
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ServerPlugin.IMPRINTER;
    }

    @Override
    public DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return SERIALIZER;
    }
}
