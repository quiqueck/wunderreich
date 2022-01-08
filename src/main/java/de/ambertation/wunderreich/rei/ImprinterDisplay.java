package de.ambertation.wunderreich.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Collections;
import java.util.List;

public class ImprinterDisplay extends BasicDisplay {
    public ImprinterDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
    }

    public static ImprinterDisplay of(Recipe<?> recipe) {
        return new ImprinterDisplay(
                EntryIngredients.ofIngredients(recipe.getIngredients()),
                Collections.singletonList(EntryIngredients.of(recipe.getResultItem()))
        );
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ServerPlugin.IMPRINTER;
    }
}
