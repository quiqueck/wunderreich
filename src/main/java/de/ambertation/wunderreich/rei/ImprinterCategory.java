package de.ambertation.wunderreich.rei;

import com.google.common.collect.Lists;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ImprinterCategory implements DisplayCategory<ImprinterDisplay> {
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("block.wunderreich.whisper_imprinter");
    }

    @Override
    public CategoryIdentifier<? extends ImprinterDisplay> getCategoryIdentifier() {
        return ServerPlugin.IMPRINTER;
    }


    @Override
    public List<Widget> setupDisplay(ImprinterDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - (107 / 2), bounds.getCenterY() - 13);
        List<Widget> widgets = Lists.newArrayList();

        //Background
        widgets.add(Widgets.createRecipeBase(bounds));

        //Inputs
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5))
                .entries(display.getInputEntries().get(ImprinterReceip.COST_A_SLOT))
                .markInput());

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 27, startPoint.y + 5))
                .entries(display.getInputEntries().get(ImprinterReceip.COST_B_SLOT))
                .markInput());

        //Arrow
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 46, startPoint.y + 4)));

        // Result
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 78, startPoint.y + 5)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 78, startPoint.y + 5))
                .entries(display.getOutputEntries().get(0))
                .disableBackground()
                .markOutput());

        return widgets;
    }
}
