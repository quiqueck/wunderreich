package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.network.SelectWhisperMessage;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class WhispererScreen
        extends AbstractContainerScreen<WhispererMenu> {
    private static final ResourceLocation VILLAGER_LOCATION = ResourceLocation.withDefaultNamespace(
            "textures/gui/container/villager.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/villager/scroller_disabled");

    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int MERCHANT_MENU_PART_X = 99;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 7;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    private static final int TRADE_BUTTON_WIDTH = 89;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 94;
    private static final int TOP_MARGIN = 16;
    private static final int BORDER_WIDTH = 2;
    private static final Component ENCHANTS_LABEL = Component.translatable("title.whisperer.enchant");
    private final WhispersButton[] enchantButtons = new WhispersButton[7];
    int scrollOff;
    private int shopItem;
    private boolean isDragging;

    public WhispererScreen(WhispererMenu merchantMenu, Inventory inventory, Component component) {
        super(merchantMenu, inventory, component);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        ImprinterRecipe selectedRule = this.menu.selectByIndex(this.shopItem);
        SelectWhisperMessage.send(selectedRule.id);
    }

    @Override
    protected void init() {
        super.init();
        final int paddingX = (this.width - this.imageWidth) / 2;
        final int paddingY = (this.height - this.imageHeight) / 2;
        int top = paddingY + TOP_MARGIN + BORDER_WIDTH;
        for (int idx = 0; idx < NUMBER_OF_OFFER_BUTTONS; ++idx) {
            this.enchantButtons[idx] = this.addRenderableWidget(new WhispersButton(
                    paddingX + TRADE_BUTTON_X,
                    top,
                    idx,
                    button -> {
                        if (button instanceof WhispersButton) {
                            this.shopItem = ((WhispersButton) button).getIndex() + this.scrollOff;
                            this.postButtonClick();
                        }
                    }
            ));
            top += TRADE_BUTTON_HEIGHT;
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawString(
                this.font,
                this.title,
                (49 + this.imageWidth / 2 - this.font.width(this.title) / 2),
                6,
                0x404040,
                false
        );

        guiGraphics.drawString(
                this.font,
                this.playerInventoryTitle,
                this.inventoryLabelX,
                this.inventoryLabelY,
                0x404040,
                false
        );
        int component = this.font.width(ENCHANTS_LABEL);
        guiGraphics.drawString(
                this.font,
                ENCHANTS_LABEL,
                (TRADE_BUTTON_X - component / 2 + 48),
                6,
                0x404040,
                false
        );
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float f, int i, int j) {
        // No need for RenderSystem.setShaderColor in 1.21.6 - GuiGraphics handles this
        final int paddingX = (this.width - this.imageWidth) / 2;
        final int paddingY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_LOCATION,
                paddingX,
                paddingY,
                0.0f,
                0.0f,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderScroller(GuiGraphics guiGraphics, int x, int y, List<ImprinterRecipe> enchants) {
        final int pageCount = enchants.size() - NUMBER_OF_OFFER_BUTTONS;
        if (pageCount > 0) {
            final int SCROLLER_MAX_Y = SCROLL_BAR_HEIGHT - SCROLLER_HEIGHT + 1; //113;
            final float STEP_PER_PAGE = (float) SCROLLER_MAX_Y / pageCount;

            int scrollerOffset = Math.min(SCROLLER_MAX_Y, (int) (this.scrollOff * STEP_PER_PAGE));
            if (this.scrollOff == pageCount) {
                scrollerOffset = SCROLLER_MAX_Y;
            }
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    SCROLLER_SPRITE,
                    x + SCROLL_BAR_START_X,
                    y + SCROLL_BAR_TOP_POS_Y + scrollerOffset,
                    SCROLLER_WIDTH,
                    SCROLLER_HEIGHT
            );
        } else {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    SCROLLER_DISABLED_SPRITE,
                    x + SCROLL_BAR_START_X,
                    y + SCROLL_BAR_TOP_POS_Y,
                    SCROLLER_WIDTH,
                    SCROLLER_HEIGHT
            );
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
        super.render(guiGraphics, i, j, f);
        var enchants = this.menu.getEnchants();
        if (!enchants.isEmpty()) {
            final int paddingX = (this.width - this.imageWidth) / 2;
            final int paddingY = (this.height - this.imageHeight) / 2;
            int top = paddingY + TOP_MARGIN + 1;
            int left = paddingX + TRADE_BUTTON_X + 5;

            // No need for RenderSystem.setShader or setShaderTexture in 1.21.6
            this.renderScroller(guiGraphics, paddingX, paddingY, enchants);

            int o = 0;
            for (WhisperRule rule : enchants) {
                if (this.canScroll(enchants.size()) && (o < this.scrollOff || o >= 7 + this.scrollOff)) {
                    ++o;
                    continue;
                }

                ItemStack costA = rule.getInput();
                ItemStack result = rule.output;

                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(0.0F, 0.0F); // Remove z-coordinate for 2D

                int decorateY = top + BORDER_WIDTH;
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(0.5f, 0.5f);

                guiGraphics.renderFakeItem(rule.icon, 2 * (left - 2), 2 * (decorateY + 7));
                guiGraphics.pose().popMatrix();

                this.renderAndDecorateCostA(guiGraphics, costA, left + 12, decorateY);

                guiGraphics.renderFakeItem(
                        WhisperRule.BLANK,
                        paddingX + TRADE_BUTTON_X + SELL_ITEM_2_X,
                        decorateY
                );
                guiGraphics.renderItemDecorations(
                        this.font,
                        WhisperRule.BLANK,
                        paddingX + TRADE_BUTTON_X + SELL_ITEM_2_X,
                        decorateY
                );

                this.renderButtonArrows(guiGraphics, rule, paddingX, decorateY);
                guiGraphics.renderFakeItem(
                        result,
                        paddingX + TRADE_BUTTON_X + BUY_ITEM_X,
                        decorateY
                );
                guiGraphics.renderItemDecorations(
                        this.font,
                        result,
                        paddingX + TRADE_BUTTON_X + BUY_ITEM_X,
                        decorateY
                );
                guiGraphics.pose().popMatrix();
                top += TRADE_BUTTON_HEIGHT;
                ++o;
            }

            for (WhispersButton tradeOfferButton : this.enchantButtons) {
                if (tradeOfferButton.isHoveredOrFocused()) {
                    tradeOfferButton.renderToolTip(guiGraphics, i, j);
                }
                tradeOfferButton.visible = tradeOfferButton.index < this.menu.getEnchants().size();
            }
            // RenderSystem.enableDepthTest() not needed in 1.21.6 - handled automatically
        }
        this.renderTooltip(guiGraphics, i, j);
    }

    private void renderButtonArrows(GuiGraphics guiGraphics, WhisperRule rule, int x, int y) {
        // No need for RenderSystem.enableBlend() in 1.21.6
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_LOCATION,
                x + TRADE_BUTTON_X + SELL_ITEM_2_X + TRADE_BUTTON_HEIGHT,
                y + 3,
                15.0f,
                171.0f,
                10,
                9,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderAndDecorateCostA(GuiGraphics guiGraphics, ItemStack costA, int x, int y) {
        guiGraphics.renderFakeItem(costA, x, y);
        guiGraphics.renderItemDecorations(this.font, costA, x, y);
    }

    private boolean canScroll(int i) {
        return i > NUMBER_OF_OFFER_BUTTONS;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        int i = this.menu.getEnchants().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_OFFER_BUTTONS;
            this.scrollOff = (int) ((double) this.scrollOff - g); // Changed f to g for deltaY parameter
            this.scrollOff = Mth.clamp(this.scrollOff, 0, j);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        int j = this.menu.getEnchants().size();
        if (this.isDragging) {
            int k = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int l = k + SCROLL_BAR_HEIGHT;
            int m = j - NUMBER_OF_OFFER_BUTTONS;
            float h = ((float) e - (float) k - 13.5f) / ((float) (l - k) - 27.0f);
            h = h * (float) m + 0.5f;
            this.scrollOff = Mth.clamp((int) h, 0, m);
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int i) {
        this.isDragging = false;
        final int paddingX = (this.width - this.imageWidth) / 2;
        final int paddingY = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu
                .getEnchants()
                .size()) && mx > (paddingX + SCROLL_BAR_START_X) && mx < (paddingX + SCROLL_BAR_START_X + 6) && my > (paddingY + SCROLL_BAR_TOP_POS_Y) && my <= (paddingY + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }
        return super.mouseClicked(mx, my, i);
    }

    @Environment(value = EnvType.CLIENT)
    class WhispersButton
            extends Button {
        final int index;

        public WhispersButton(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(@NotNull GuiGraphics guiGraphics, int i, int j) {
            if (this.isHovered && WhispererScreen.this.menu
                    .getEnchants()
                    .size() > this.index + WhispererScreen.this.scrollOff) {
                if (i < this.getX() + TRADE_BUTTON_HEIGHT) {
                    var typeName = Component.translatable("enchantment.type." + WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff)
                            .getCategory());
                    guiGraphics.setTooltipForNextFrame(font, typeName, i, j);
                } else if (i < this.getX() + 50 && i > this.getX() + 30) {
                    ItemStack itemStack = WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff)
                            .getInput();
                    if (!itemStack.isEmpty()) {
                        guiGraphics.setTooltipForNextFrame(font, itemStack, i, j);
                    }
                } else if (i > this.getX() + 65) {
                    ItemStack itemStack = WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff).output;
                    guiGraphics.setTooltipForNextFrame(font, itemStack, i, j);
                }
            }
        }
    }
}