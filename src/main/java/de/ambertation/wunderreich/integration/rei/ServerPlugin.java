package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.core.component.DataComponentMap;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;

import static de.ambertation.wunderreich.registries.WunderreichDataComponents.WHISPERER;

public class ServerPlugin implements REICommonPlugin {
    public static final CategoryIdentifier<ImprinterDisplay> IMPRINTER = CategoryIdentifier.of(
            Wunderreich.MOD_ID,
            ImprinterRecipe.Type.ID.getPath()
    );

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Wunderreich.ID("imprinter"), ImprinterDisplay.SERIALIZER);
    }

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        final EntryComparator<DataComponentMap> nbtHasher = EntryComparator.component(WHISPERER);

        registry.register(
                (context, stack) -> nbtHasher.hash(context, stack.getComponents()),
                WunderreichItems.WHISPERER
        );
    }
}
