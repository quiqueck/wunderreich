package de.ambertation.wunderreich.recipes.catalyst;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * The two-slot input of a {@link CatalystRecipe}.
 * <p>
 * Slot 0 is the {@link #input()} that gets consumed by the machine, slot 1 is the
 * {@link #catalyst()}. The catalyst is <b>never</b> consumed &mdash; it only has to be present
 * for the recipe to match. Machines are expected to leave the catalyst stack untouched.
 */
public record CatalystRecipeInput(ItemStack input, ItemStack catalyst) implements RecipeInput {
    public static final int INPUT_SLOT = 0;
    public static final int CATALYST_SLOT = 1;

    @Override
    public ItemStack getItem(int i) {
        if (i == INPUT_SLOT) return input;
        if (i == CATALYST_SLOT) return catalyst;
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty() && catalyst.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CatalystRecipeInput{");
        sb.append("input=").append(input);
        sb.append(", catalyst=").append(catalyst);
        sb.append('}');
        return sb.toString();
    }
}
