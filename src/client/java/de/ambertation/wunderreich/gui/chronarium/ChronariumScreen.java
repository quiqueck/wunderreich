package de.ambertation.wunderreich.gui.chronarium;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Furnace-like screen for the {@link de.ambertation.wunderreich.blocks.Chronarium}.
 * <p>
 * The whole GUI lives on a single 256x256 sheet: the 176x166 panel at (0,0) plus two "lit"
 * indicator sprites on the right that are blitted, clipped to the current progress, over the
 * greyed-out versions that are already baked into the panel.
 */
public class ChronariumScreen extends AbstractContainerScreen<ChronariumMenu> {
    private static final Identifier TEXTURE = Wunderreich.ID("textures/gui/chronarium.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    // Progress arrow: drawn at (79,34), lit sprite at (176,17) on the sheet.
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 17;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    // Hourglass: drawn at (56,36), lit sprite at (176,48) on the sheet.
    private static final int GLASS_X = 56;
    private static final int GLASS_Y = 36;
    private static final int GLASS_U = 176;
    private static final int GLASS_V = 48;
    private static final int GLASS_SIZE = 14;

    public ChronariumScreen(ChronariumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = 72;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        final int x = this.leftPos;
        final int y = this.topPos;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0f, 0.0f,
                this.imageWidth, this.imageHeight,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Progress arrow: grows left to right.
        final int arrow = this.menu.getProgressScaled(ARROW_WIDTH);
        if (arrow > 0) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x + ARROW_X, y + ARROW_Y,
                    (float) ARROW_U, (float) ARROW_V,
                    arrow, ARROW_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }

        // Hourglass: fills bottom up, like sand piling up in the lower bulb.
        final int sand = this.menu.getProgressScaled(GLASS_SIZE);
        if (sand > 0) {
            final int offset = GLASS_SIZE - sand;
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x + GLASS_X, y + GLASS_Y + offset,
                    (float) GLASS_U, (float) (GLASS_V + offset),
                    GLASS_SIZE, sand,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }
    }
}
