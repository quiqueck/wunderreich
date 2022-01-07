package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.registries.WunderreichBlocks;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ImprinterCategory  implements DisplayCategory<ImprinterDisplay> {
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("wunderreich.rei.category.imprinter");
    }

    @Override
    public CategoryIdentifier<? extends ImprinterDisplay> getCategoryIdentifier() {
        return ServerPlugin.IMPRINTER;
    }
}
