package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.network.SelectWhisperMessage;
import de.ambertation.wunderreich.rei.ImprinterRecipe;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ru.bclib.api.dataexchange.DataExchangeAPI;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class WhispererScreen
        extends AbstractContainerScreen<WhispererMenu> {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation(
            "textures/gui/container/villager2.png");
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
    private static final Component ENCHANTS_LABEL = new TranslatableComponent("title.whisperer.enchant");
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
        this.menu.setSelectionHint(this.shopItem);
        this.menu.tryMoveItems(this.shopItem);
        DataExchangeAPI.send(new SelectWhisperMessage(this.shopItem));
    }

    @Override
    protected void init() {
        super.init();
        final int paddingX = (this.width - this.imageWidth) / 2;
        final int paddingY = (this.height - this.imageHeight) / 2;
        int top = paddingY + TOP_MARGIN + BORDER_WIDTH;
        for (int idx = 0; idx < NUMBER_OF_OFFER_BUTTONS; ++idx) {
            this.enchantButtons[idx] = this.addRenderableWidget(new WhispersButton(paddingX + TRADE_BUTTON_X,
                    top,
                    idx,
                    button -> {
                        if (button instanceof WhispersButton) {
                            this.shopItem = ((WhispersButton) button).getIndex() + this.scrollOff;
                            this.postButtonClick();
                        }
                    }));
            top += TRADE_BUTTON_HEIGHT;
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        this.font.draw(poseStack,
                this.title,
                (float) (49 + this.imageWidth / 2 - this.font.width(this.title) / 2),
                6.0f,
                0x404040);

        this.font.draw(poseStack,
                this.playerInventoryTitle,
                (float) this.inventoryLabelX,
                (float) this.inventoryLabelY,
                0x404040);
        int component = this.font.width(ENCHANTS_LABEL);
        this.font.draw(poseStack, ENCHANTS_LABEL, (float) (TRADE_BUTTON_X - component / 2 + 48), 6.0f, 0x404040);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);

        final int paddingX = (this.width - this.imageWidth) / 2;
        final int paddingY = (this.height - this.imageHeight) / 2;

        blit(poseStack,
                paddingX,
                paddingY,
                this.getBlitOffset(),
                0.0f,
                0.0f,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }


    private void renderScroller(PoseStack poseStack, int x, int y, List<ImprinterRecipe> enchants) {
        final int pageCount = enchants.size() - NUMBER_OF_OFFER_BUTTONS;
        if (pageCount > 0) {
            final int SCROLLER_MAX_Y = SCROLL_BAR_HEIGHT - SCROLLER_HEIGHT + 1; //113;
            final float STEP_PER_PAGE = (float) SCROLLER_MAX_Y / pageCount;

            int scrollerOffset = Math.min(SCROLLER_MAX_Y, (int) (this.scrollOff * STEP_PER_PAGE));
            if (this.scrollOff == pageCount) {
                scrollerOffset = SCROLLER_MAX_Y;
            }
            WhispererScreen.blit(poseStack,
                    x + SCROLL_BAR_START_X,
                    y + SCROLL_BAR_TOP_POS_Y + scrollerOffset,
                    this.getBlitOffset(),
                    0.0f,
                    199.0f,
                    SCROLLER_WIDTH,
                    SCROLLER_HEIGHT,
                    TEXTURE_WIDTH,
                    TEXTURE_HEIGHT);
        } else {
            WhispererScreen.blit(poseStack,
                    x + SCROLL_BAR_START_X,
                    y + SCROLL_BAR_TOP_POS_Y,
                    this.getBlitOffset(),
                    6.0f,
                    199.0f,
                    SCROLLER_WIDTH,
                    SCROLLER_HEIGHT,
                    TEXTURE_WIDTH,
                    TEXTURE_HEIGHT);
        }
    }


    /*
     * BEGIN: From ItemRenderer by Mojang
     * --------------------------------
     */
    public void renderAndDecorateItemScaled(ItemStack itemStack, int i, int j, float scale) {
        this.tryRenderGuiItemScaled(Minecraft.getInstance().player, itemStack, i, j, 0, 0, scale);
    }

    private void tryRenderGuiItemScaled(@Nullable LivingEntity livingEntity,
                                        ItemStack itemStack,
                                        int i,
                                        int j,
                                        int k,
                                        int l,
                                        float scale) {
        if (!itemStack.isEmpty()) {
            BakedModel bakedModel = this.itemRenderer.getModel(itemStack, (Level) null, livingEntity, k);
            this.itemRenderer.blitOffset = bakedModel.isGui3d()
                    ? this.itemRenderer.blitOffset + 50.0F + (float) l
                    : this.itemRenderer.blitOffset + 50.0F;

            try {
                this.renderGuiItem(itemStack, i, j, bakedModel, scale);
            } catch (Throwable var11) {
                CrashReport crashReport = CrashReport.forThrowable(var11, "Rendering item");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
                crashReportCategory.setDetail("Item Type", () -> {
                    return String.valueOf(itemStack.getItem());
                });
                crashReportCategory.setDetail("Item Damage", () -> {
                    return String.valueOf(itemStack.getDamageValue());
                });
                crashReportCategory.setDetail("Item NBT", () -> {
                    return String.valueOf(itemStack.getTag());
                });
                crashReportCategory.setDetail("Item Foil", () -> {
                    return String.valueOf(itemStack.hasFoil());
                });
                throw new ReportedException(crashReport);
            }

            this.itemRenderer.blitOffset = bakedModel.isGui3d()
                    ? this.itemRenderer.blitOffset - 50.0F - (float) l
                    : this.itemRenderer.blitOffset - 50.0F;
        }
    }

    protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel, float scale) {
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate((double) i, (double) j, (double) (100.0F + this.itemRenderer.blitOffset));
        poseStack.translate(8.0D * scale, 8.0D * scale, 0.0D);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean bl = !bakedModel.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }

        this.itemRenderer.render(itemStack,
                ItemTransforms.TransformType.GUI,
                false,
                poseStack2,
                bufferSource,
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (bl) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    /*
     * END: From ItemRenderer by Mojang
     * --------------------------------
     */


    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        var enchants = this.menu.getEnchants();
        if (!enchants.isEmpty()) {
            final int paddingX = (this.width - this.imageWidth) / 2;
            final int paddingY = (this.height - this.imageHeight) / 2;
            int top = paddingY + TOP_MARGIN + 1;
            int left = paddingX + TRADE_BUTTON_X + 5;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
            this.renderScroller(poseStack, paddingX, paddingY, enchants);
            int o = 0;
            for (WhisperRule rule : enchants) {
                if (this.canScroll(enchants.size()) && (o < this.scrollOff || o >= 7 + this.scrollOff)) {
                    ++o;
                    continue;
                }

                ItemStack costA = rule.getInputA();
                ItemStack costB = rule.getInputB();
                ItemStack result = rule.output;
                this.itemRenderer.blitOffset = 100.0f;

                int decorateY = top + BORDER_WIDTH;
                poseStack.pushPose();
                poseStack.scale(0.5f, 1, 0.5f);
                renderAndDecorateItemScaled(rule.type, left - 2, decorateY + 7, 0.5f);
                poseStack.popPose();
                this.renderAndDecorateCostA(poseStack, costA, left + 12, decorateY);
                if (!costB.isEmpty()) {
                    this.itemRenderer.renderAndDecorateFakeItem(costB,
                            paddingX + TRADE_BUTTON_X + SELL_ITEM_2_X,
                            decorateY);
                    this.itemRenderer.renderGuiItemDecorations(this.font,
                            costB,
                            paddingX + TRADE_BUTTON_X + SELL_ITEM_2_X,
                            decorateY);
                }
                this.renderButtonArrows(poseStack, rule, paddingX, decorateY);
                this.itemRenderer.renderAndDecorateFakeItem(result, paddingX + TRADE_BUTTON_X + BUY_ITEM_X, decorateY);
                this.itemRenderer.renderGuiItemDecorations(this.font,
                        result,
                        paddingX + TRADE_BUTTON_X + BUY_ITEM_X,
                        decorateY);
                this.itemRenderer.blitOffset = 0.0f;
                top += TRADE_BUTTON_HEIGHT;
                ++o;
            }

            for (WhispersButton tradeOfferButton : this.enchantButtons) {
                if (tradeOfferButton.isHoveredOrFocused()) {
                    tradeOfferButton.renderToolTip(poseStack, i, j);
                }
                tradeOfferButton.visible = tradeOfferButton.index < this.menu.getEnchants().size();
            }
            RenderSystem.enableDepthTest();
        }
        this.renderTooltip(poseStack, i, j);
    }

    private void renderButtonArrows(PoseStack poseStack, WhisperRule rule, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
        WhispererScreen.blit(poseStack,
                x + TRADE_BUTTON_X + SELL_ITEM_2_X + TRADE_BUTTON_HEIGHT,
                y + 3,
                this.getBlitOffset(),
                15.0f,
                171.0f,
                10,
                9,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    private void renderAndDecorateCostA(PoseStack poseStack, ItemStack costA, int x, int y) {
        this.itemRenderer.renderAndDecorateFakeItem(costA, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, costA, x, y);
    }

    private boolean canScroll(int i) {
        return i > NUMBER_OF_OFFER_BUTTONS;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        int i = this.menu.getEnchants().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_OFFER_BUTTONS;
            this.scrollOff = (int) ((double) this.scrollOff - f);
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
            super(x, y, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, TextComponent.EMPTY, onPress);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderToolTip(PoseStack poseStack, int i, int j) {
            if (this.isHovered && WhispererScreen.this.menu
                    .getEnchants()
                    .size() > this.index + WhispererScreen.this.scrollOff) {
                if (i < this.x + TRADE_BUTTON_HEIGHT) {
                    var typeName = new TranslatableComponent("enchantment.type." + WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff)
                            .getCategory());
                    WhispererScreen.this.renderTooltip(poseStack, typeName, i, j);
                } else if (i < this.x + 50 && i > this.x + 30) {
                    ItemStack itemStack = WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff)
                            .getInputA();
                    if (!itemStack.isEmpty()) {
                        WhispererScreen.this.renderTooltip(poseStack, itemStack, i, j);
                    }
                } else if (i > this.x + 65) {
                    ItemStack itemStack = WhispererScreen.this.menu
                            .getEnchants()
                            .get(this.index + WhispererScreen.this.scrollOff).output;
                    WhispererScreen.this.renderTooltip(poseStack, itemStack, i, j);
                }
            }
        }
    }
}

