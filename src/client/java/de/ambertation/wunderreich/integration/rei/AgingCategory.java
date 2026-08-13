package de.ambertation.wunderreich.integration.rei;

import de.ambertation.wunderreich.integration.AgingRecipeUI;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.List;

/**
 * REI category for {@code wunderreich:aging} (the Chronarium).
 * <p>
 * Mirrors {@link ImprinterCategory}, but adds the two things this recipe type needs to explain:
 * a hover tooltip on the catalyst slot saying it is never consumed, plus a permanently visible
 * line of text below the recipe repeating that and naming the processing time. The arrow is
 * animated over the real recipe duration.
 */
public class AgingCategory implements DisplayCategory<AgingDisplay> {
    private static final int INNER_WIDTH = 124;

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(WunderreichBlocks.CHRONARIUM);
    }

    @Override
    public Component getTitle() {
        return AgingRecipeUI.title();
    }

    @Override
    public CategoryIdentifier<? extends AgingDisplay> getCategoryIdentifier() {
        return ServerPlugin.AGING;
    }

    /**
     * Slots plus three lines of text (time, experience, catalyst note).
     */
    @Override
    public int getDisplayHeight() {
        return 76;
    }

    @Override
    public List<Widget> setupDisplay(AgingDisplay display, Rectangle bounds) {
        final Point startPoint = new Point(bounds.getCenterX() - (INNER_WIDTH / 2), bounds.getY() + 5);
        final List<Widget> widgets = Lists.newArrayList();

        widgets.add(Widgets.createRecipeBase(bounds));

        // Input - consumed.
        final Rectangle inputBounds = new Rectangle(startPoint.x + 4, startPoint.y, 18, 18);
        widgets.add(Widgets.createSlot(new Point(inputBounds.x, inputBounds.y))
                           .entries(display.getInputEntries().get(AgingDisplay.INPUT_SLOT))
                           .markInput());

        // Catalyst - required, but handed back. The tooltip is attached to the slot area so it
        // shows up even when the slot is empty/cycling.
        final Rectangle catalystBounds = new Rectangle(startPoint.x + 26, startPoint.y, 18, 18);
        widgets.add(Widgets.createSlot(new Point(catalystBounds.x, catalystBounds.y))
                           .entries(display.getInputEntries().get(AgingDisplay.CATALYST_SLOT))
                           .markInput());
        widgets.add(Widgets.createTooltip(catalystBounds, AgingRecipeUI.catalystTooltip()));

        // Arrow, animated over the actual recipe duration.
        final Rectangle arrowBounds = new Rectangle(startPoint.x + 52, startPoint.y - 1, 24, 17);
        widgets.add(Widgets.createArrow(new Point(arrowBounds.x, arrowBounds.y))
                           .animationDurationTicks(display.getTime()));
        widgets.add(Widgets.createTooltip(arrowBounds, AgingRecipeUI.timeTooltip(display.getTime())));

        // Result.
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 100, startPoint.y)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 100, startPoint.y))
                           .entries(display.getOutputEntries().get(0))
                           .disableBackground()
                           .markOutput());

        // Always-visible lines. The last one is the whole point of this category.
        widgets.add(Widgets.createLabel(
                new Point(bounds.getCenterX(), startPoint.y + 23),
                AgingRecipeUI.timeLabel(display.getTime())
        ).centered().noShadow().color(0xFF404040, 0xFFBBBBBB));

        // Like a furnace, the result carries experience that is handed out when a player takes it.
        if (AgingRecipeUI.hasExperience(display.getExperience())) {
            widgets.add(Widgets.createLabel(
                    new Point(bounds.getCenterX(), startPoint.y + 34),
                    AgingRecipeUI.experienceLabel(display.getExperience())
            ).centered().noShadow().color(0xFF404040, 0xFFBBBBBB));
        }

        widgets.add(Widgets.createLabel(
                new Point(bounds.getCenterX(), startPoint.y + 45),
                AgingRecipeUI.catalystLabel()
        ).centered().noShadow().color(0xFF404040, 0xFFBBBBBB));

        return widgets;
    }
}
