package de.ambertation.wunderreich.gui.modmenu;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.Image;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;
import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Value;
import de.ambertation.wunderlib.ui.vanilla.ConfigScreen;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MainScreen extends ConfigScreen {
    static final ResourceLocation ICON_LINE = new ResourceLocation(Wunderreich.MOD_ID, "hline.png");
    static final Rectangle ICON_LINE_UV = new Rectangle(0, 0, 94, 15);
    static final ResourceLocation TEXT_LINE = new ResourceLocation(Wunderreich.MOD_ID, "text.png");
    static final Rectangle TEXT_LINE_UV = new Rectangle(0, 0, 376, 57);
    static final ResourceLocation BOX = new ResourceLocation(Wunderreich.MOD_ID, "box.png");
    static final Rectangle BOX_UV = new Rectangle(0, 0, 400, 440);

    public MainScreen(@Nullable Screen parent) {
        super(parent, Component.translatable("title.wunderreich.modmenu.main"), List.of(Configs.MAIN), 3, 2, 10, 7);
    }

    @Override
    protected LayoutComponent<?, ?> createTitle() {
        var hline = new Image(
                Value.fixed(94),
                Value.fixed(15),
                ICON_LINE,
                ICON_LINE_UV.size()
        ).centerHorizontal();

        var text = new Image(
                Value.fixed(94),
                Value.fixed(14),
                TEXT_LINE,
                TEXT_LINE_UV.size()
        ).centerHorizontal();

        return new VerticalStack(fixed(94), fixed(29)).centerHorizontal().add(hline).addSpacer(2).add(text);
    }

    @Override
    protected void renderBackground(PoseStack poseStack, int i, int j, float f) {
        GuiComponent.fill(poseStack, 0, 0, width, height, ColorHelper.SCREEN_BACKGROUND);

        RenderHelper.renderImage(
                poseStack,
                300, 8, 200, 220,
                BOX, BOX_UV.size(), BOX_UV, 1
        );
    }

    @Override
    public void onClose() {
        Configs.MAIN.save();
        super.onClose();
    }
}
