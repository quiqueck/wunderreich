package de.ambertation.wunderreich.gui.suctionTube;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.blockentities.SuctionTubeBlockEntity;
import static de.ambertation.wunderreich.gui.suctionTube.SuctionTubeMenu.SLOTS_PER_DIRECTION;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side screen for the Suction Tube configuration GUI.
 * Displays filter slots for each input direction with clear labels.
 */
public class SuctionTubeScreen extends AbstractContainerScreen<SuctionTubeMenu> {
    private static final ResourceLocation TEXTURE = Wunderreich.ID("textures/gui/suction_tube.png");
    private static final int TEXTURE_WIDTH = 306;
    private static final int TEXTURE_HEIGHT = 256;
    // Direction labels for display
    private static final String[] DIRECTION_LABELS = {
            "Bottom", "North", "East", "South", "West"
    };

    public SuctionTubeScreen(SuctionTubeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = SuctionTubeMenu.GUI_WIDTH;
        this.imageHeight = SuctionTubeMenu.GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // No need for RenderSystem.setShaderColor in 1.21.6 - GuiGraphics handles this
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
            boolean hasContainer = this.menu.hasConnectedContainer(direction);

            // Draw direction label above the slots
            String label = DIRECTION_LABELS[dirIndex];
            int labelWidth = this.font.width(label);
            int labelX = baseX + (SuctionTubeMenu.FILTER_SLOTS_WIDTH - labelWidth) / 2; // Center label over 4 slots (72px wide)
            int labelY = baseY - 12;

            guiGraphics.drawString(this.font, label, labelX, labelY, 0x404040, false);

            // Draw filter slot backgrounds for this direction
            for (int slotIndex = 0; slotIndex < SLOTS_PER_DIRECTION; slotIndex++) {
                int slotX = baseX + (slotIndex * SuctionTubeMenu.SLOT_SIZE);

                // Draw transparent red background for disconnected containers
                if (!hasContainer) {
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

            // Draw container icon if connected
            if (hasContainer) {
                ItemStack representativeItem = this.menu.getConnectedContainerItem(direction);
                if (representativeItem != null) {
                    // If no items found, show a generic chest icon or container block
                    if (representativeItem.isEmpty()) {
                        representativeItem = new ItemStack(net.minecraft.world.item.Items.CHEST);
                    }

                    // Draw the item icon next to the slots
                    int iconX = x + dirPositions[dirIndex][2]; // Add GUI offset

                    //guiGraphics.fill(iconX - 1, baseY - 1, iconX + 17, baseY + 17, 0x800000FF); // Semi-transparent blue
                    guiGraphics.renderItem(representativeItem, iconX, baseY);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Draw inventory label
        guiGraphics.drawString(
                this.font,
                this.playerInventoryTitle,
                this.inventoryLabelX,
                this.inventoryLabelY,
                0x404040,
                false
        );

        // Draw filter instructions
        String instruction = "Place items to filter by direction";
        int instructionX = (this.imageWidth - this.font.width(instruction)) / 2;
        guiGraphics.drawString(this.font, instruction, instructionX, 130, 0x666666, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

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
                break; // Only show one tooltip at a time
            }
        }
    }
}
