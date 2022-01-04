/**
 * This class is adapted from "Easy Villagers"
 */
package de.ambertation.wunderreich.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.network.CycleTradesMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CycleTradesButton extends Button {
	
	private static final ResourceLocation ARROW_BUTTON = new ResourceLocation(Wunderreich.MOD_ID, "textures/gui/reroll.png");
	
	public static final int WIDTH = 18;

	private static final int HALF_HEIGHT = 13;
	public static final int HEIGHT = HALF_HEIGHT*2;
	
	private MerchantScreen screen;
	private MerchantMenu menu;

	@NotNull
	public static CycleTradesButton getCycleTradesButton(AbstractContainerScreen<MerchantMenu> merchantScreenMixin, int imageWidth, int imageHeight, MerchantScreen merchantScreen, MerchantMenu menu) {
		final int left = (merchantScreenMixin.width - imageWidth) / 2;
		final int top = (merchantScreenMixin.height - imageHeight) / 2;

		CycleTradesButton button = new CycleTradesButton( left - CycleTradesButton.WIDTH - 1, top + 2, b -> {
			CycleTradesMessage.send();
		}, merchantScreen, menu);

		return button;
	}
	
	public CycleTradesButton(int x, int y, OnPress pressable, MerchantScreen screen, MerchantMenu menu) {
		super(x, y, WIDTH, HEIGHT, TextComponent.EMPTY, pressable);
		this.screen = screen;
		this.menu = menu;
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		visible = screen.getMenu().showProgressBar() && screen.getMenu().getTraderXp() <= 0;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, BookViewScreen.BOOK_LOCATION);
		final int u = isHovered?26:3;

		blit(matrixStack, x, y+HALF_HEIGHT, u, 207, WIDTH, HALF_HEIGHT, 256, 256);
		matrixStack.pushPose();
		matrixStack.scale(1, -1, 1);
		blit(matrixStack, x, -y, u, 207, WIDTH, -HALF_HEIGHT, 256, 256);
		matrixStack.popPose();

		if (isHovered) {
			List<Component> components = new ArrayList<>(2);
			components.add(new TranslatableComponent("tooltip.wunderreich.cycle_trades"));
			MerchantOffers offers = this.menu.getOffers();
			for (MerchantOffer offer : offers) {
				if (offer.getResult().is(Items.ENCHANTED_BOOK)) {
					var enchantments = EnchantedBookItem.getEnchantments(offer.getResult());

					for (int i=0; i<enchantments.size(); i++) {
						var tag = enchantments.getCompound(i);
						Enchantment e = TrainedVillagerWhisperer.findEnchantment(tag);
						int level = EnchantmentHelper.getEnchantmentLevel(tag);


						components.add(e.getFullname(level));
					}
				}
			}


			screen.renderTooltip(matrixStack, components, Optional.empty(), mouseX, mouseY);
		}
	}
}
