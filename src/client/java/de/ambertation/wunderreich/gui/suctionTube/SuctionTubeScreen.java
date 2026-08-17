package de.ambertation.wunderreich.gui.suctionTube;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import static de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu.SLOTS_PER_DIRECTION;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Client-side screen for the Suction Tube configuration GUI.
 * Displays filter slots for each input direction with clear labels.
 */
public class SuctionTubeScreen extends AbstractContainerScreen<SuctionTubeMenu> {
    private static final Identifier TEXTURE = Wunderreich.ID("textures/gui/suction_tube.png");
    private static final int TEXTURE_WIDTH = 306;
    private static final int TEXTURE_HEIGHT = 256;
    // Direction labels for display
    private static final String[] DIRECTION_LABELS = {
            "Bottom", "North", "East", "South", "West"
    };
    // Single-letter stand-ins for DIRECTION_LABELS, overlaid on each intake's connection icon -
    // there is no room anywhere in this panel for the full word (see the "no on-screen direction
    // label" comment in extractBackground), but one character fits inside the icon itself.
    private static final String[] DIRECTION_LETTERS = {"D", "N", "E", "S", "W"};
    private static final int DARK_AREA_TEXT_COLOR = 0xFFFFFFFF;

    public SuctionTubeScreen(SuctionTubeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SuctionTubeMenu.GUI_WIDTH, SuctionTubeMenu.GUI_HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        // GuiGraphicsExtractor handles the shader color state
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // (1) Draw main background using the correct blit signature
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0f, 0.0f,
                this.imageWidth, this.imageHeight,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
//        guiGraphics.renderOutline(// Light gray outline
//                x, y, this.imageWidth, this.imageHeight, 0xFFFFFF8B
//        );

        // Draw filter slots in cross pattern:
        //       NNNN
        // WWWW  BBBB  EEEE
        //       SSSS

        // Use Menu's position calculation methods for exact alignment
        int[][] dirPositions = SuctionTubeMenu.getAllFilterPositions();

        for (int dirIndex = 0; dirIndex < 5; dirIndex++) {
            int baseX = x + dirPositions[dirIndex][0]; // Add GUI offset
            int baseY = y + dirPositions[dirIndex][1]; // Add GUI offset

            Direction direction = SuctionTubeBlockEntity.DIRECTIONS[dirIndex];
            final boolean hasConnectedItem = this.menu.hasConnectedItem(direction);
            final boolean hasContainer = hasConnectedItem && this.menu.hasConnectedContainer(direction);
            final boolean isLocked = this.menu.isLockedDirection(direction);

            // No on-screen direction label: there is nowhere in this panel a 12px label can go
            // without colliding with a neighbouring row (verified by hand - every row here is only
            // 4px apart, not the 12+ a label needs), and the texture's own painted background
            // already has to stay exactly where it is - see SuctionTubeMenu's GUI_HEIGHT comment.
            // The direction is named in a hover tooltip instead - see #extractTooltip.

            // Draw filter slot backgrounds for this direction
            for (int slotIndex = 0; slotIndex < SLOTS_PER_DIRECTION; slotIndex++) {
                int slotX = baseX + (slotIndex * SuctionTubeMenu.SLOT_SIZE);
                // Draw transparent red background for disconnected containers
                if (!hasContainer || isLocked) {
                    guiGraphics.fill(slotX, baseY, slotX + 16, baseY + 16, 0x20FF0000); // Semi-transparent red
                }

                // (2) Draw slot background using correct blit signature
//                guiGraphics.blit(
//                        RenderPipelines.GUI_TEXTURED, TEXTURE,
//                        slotX - 1, baseY - 1,
//                        0.0f, 0.0f,
//                        SuctionTubeMenu.SLOT_SIZE, SuctionTubeMenu.SLOT_SIZE,
//                        TEXTURE_WIDTH, TEXTURE_HEIGHT
//                );

                // Draw outline rectangle around each slot
//                guiGraphics.renderOutline(// Light gray outline
//                        slotX - 1, baseY - 1, 18, 18, 0xFF8B8B8B
//                );
            }

            int iconX = x + dirPositions[dirIndex][2]; // Add GUI offset
            drawConnectionIcon(guiGraphics, direction, iconX, baseY, DIRECTION_LETTERS[dirIndex]);
        }

        // Output: centered at the top of the panel, above the cross - see
        // SuctionTubeMenu.getOutputIconPosition(). No room for a text label of its own up there
        // (that is what freed NORTH's label above); the tooltip on hover names it instead. No
        // letter either - it is not one of the five intake directions, and its position alone
        // (separate from the cross) already tells it apart from them.
        int[] outputPos = SuctionTubeMenu.getOutputIconPosition();
        int outputX = x + outputPos[0];
        int outputY = y + outputPos[1];
        drawConnectionIcon(guiGraphics, Direction.UP, outputX, outputY, null);
    }

    /**
     * Draws a direction's connected-container icon: the block's own item icon, a red overlay if a
     * comparator is holding that side locked, a signal-strength badge if it currently has one, and
     * (for the five intakes, not the output) a single-letter direction badge in the corner - see
     * {@link #DIRECTION_LETTERS}. Full direction names are hover tooltips instead - see
     * {@link #extractTooltip}.
     */
    private void drawConnectionIcon(
            GuiGraphicsExtractor guiGraphics, Direction direction, int iconX, int iconY, @Nullable String letter
    ) {
        final boolean hasConnectedItem = this.menu.hasConnectedItem(direction);
        final boolean isLocked = this.menu.isLockedDirection(direction);

        if (hasConnectedItem) {
            ItemStack representativeItem = this.menu.getConnectedContainerItem(direction);
            if (representativeItem != null) {
                // If no items found, show a generic chest icon or container block
                if (representativeItem.isEmpty()) {
                    representativeItem = new ItemStack(net.minecraft.world.item.Items.CHEST);
                }
                guiGraphics.item(representativeItem, iconX, iconY);
                if (isLocked) {
                    guiGraphics.fill(iconX, iconY, iconX + 16, iconY + 16, 0x50FF0000); // Semi-transparent red
                }

                final int signalStrength = this.menu.signalStrengthForDirection(direction);
                if (signalStrength > 0) {
                    guiGraphics.itemDecorations(
                            this.font,
                            representativeItem.copyWithCount(signalStrength),
                            iconX,
                            iconY
                    ); // Semi-transparent green
                }
            }
        } else if (isLocked) {
            guiGraphics.fill(iconX, iconY, iconX + 16, iconY + 16, 0x50FF0000); // Semi-transparent red
        }

        if (letter != null) {
            // Top-left corner: the signal-strength badge above already claims the bottom-right.
            // A small dark backing chip keeps the letter legible over a light-colored item icon.
            guiGraphics.fill(iconX - 1, iconY - 1, iconX + 7, iconY + 8, 0x90000000);
            guiGraphics.text(this.font, letter, iconX + 1, iconY, DARK_AREA_TEXT_COLOR, true);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // No title: it has nowhere to go that doesn't intersect the panel's top border (confirmed
        // against an actual in-game screenshot), and there is no room to shrink anything else to
        // make space for it - see SuctionTubeMenu's GUI_HEIGHT comment on this panel's fixed
        // budget. The window's own title bar / the item's name already say what this is.
        //
        // No "Place items to filter by direction" instruction either: it was hardcoded to a fixed
        // y that lands inside the inventory grid regardless of anything else on this screen.
        //
        // No "Inventory" label either - the grid's own position already makes it obvious what it
        // is, same reasoning as dropping the other two.
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.extractTooltip(guiGraphics, x, y);

        // Show tooltips for filter slots using the same positioning logic as the slots
        int relativeX = x - this.leftPos;
        int relativeY = y - this.topPos;

        // Check each direction's filter area
        int[][] dirPositions = SuctionTubeMenu.getAllFilterPositions();

        for (int dirIndex = 0; dirIndex < 5; dirIndex++) {
            int baseX = dirPositions[dirIndex][0];
            int baseY = dirPositions[dirIndex][1];

            // Check if mouse is within this direction's 4-slot area
            // Each direction has 4 slots of 18px width = 72px total width
            if (relativeX >= baseX && relativeX <= baseX + (SLOTS_PER_DIRECTION * SuctionTubeMenu.SLOT_SIZE) &&
                    relativeY >= baseY && relativeY <= baseY + SuctionTubeMenu.SLOT_SIZE) {

                String directionName = DIRECTION_LABELS[dirIndex];
                Component tooltip = Component.literal("Filter for " + directionName + " side");
                guiGraphics.setTooltipForNextFrame(this.font, tooltip, x, y);
                return; // Only show one tooltip at a time
            }

            // The connection icon (the letter badge) has no room for a text label of its own
            // either - see the D/N/E/S/W badges in drawConnectionIcon - so it is named here too.
            int iconX = dirPositions[dirIndex][2];
            if (relativeX >= iconX && relativeX <= iconX + 16 &&
                    relativeY >= baseY && relativeY <= baseY + 16) {
                Component tooltip = Component.literal(DIRECTION_LABELS[dirIndex] + " Input");
                guiGraphics.setTooltipForNextFrame(this.font, tooltip, x, y);
                return;
            }
        }

        // Output icon: it has no text label of its own (no room for one above it - see
        // SuctionTubeMenu.OUTPUT_ICON_Y), so this is the only place it is named.
        int[] outputPos = SuctionTubeMenu.getOutputIconPosition();
        if (relativeX >= outputPos[0] && relativeX <= outputPos[0] + 16 &&
                relativeY >= outputPos[1] && relativeY <= outputPos[1] + 16) {
            guiGraphics.setTooltipForNextFrame(this.font, Component.literal("Output"), x, y);
        }
    }
}
