package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.data_components.WunderKisteData;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import static de.ambertation.wunderreich.registries.WunderreichDataComponents.WUNDERKISTE;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WunderKisteItem extends BlockItem {
    public WunderKisteItem(Block block) {
        super(block, WunderreichItems.makeItemSettings());
    }

    public static WunderKisteDomain getDomain(ItemStack itemStack) {
        final WunderKisteData data = itemStack.get(WUNDERKISTE);
        if (data != null) return data.domain();

        return WunderKisteBlock.DEFAULT_DOMAIN;
    }

    public static ItemStack setDomain(ItemStack itemStack, WunderKisteDomain domain) {
        if (WunderKisteBlock.DEFAULT_DOMAIN.equals(domain)) {
            itemStack.remove(WUNDERKISTE);
        } else {
            itemStack.set(WUNDERKISTE, new WunderKisteData(domain));
        }

        return itemStack;
    }

    public static Component getDomainComponent(WunderKisteDomain domain) {
        return Component.translatable("wunderreich.domain." + domain.toString()).setStyle(Style.EMPTY.withColor(
                domain.textColor).withBold(true));
    }

    public static void addAllVariants(List<ItemStack> itemList) {
        for (WunderKisteDomain domain : WunderKisteDomain.values()) {
            ItemStack stack = createForDomain(domain);
            itemList.add(stack);
        }
    }

    @NotNull
    private static ItemStack createForDomain(WunderKisteDomain domain) {
        assert WunderreichBlocks.WUNDER_KISTE != null;
        return setDomain(new ItemStack(WunderreichBlocks.WUNDER_KISTE.asItem(), 1), domain);
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext tooltipContext,
            List<Component> list,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        if (WunderreichRules.Wunderkiste.haveMultiple()) {
            final WunderKisteDomain domain = getDomain(itemStack);

            Component domainComponent = WunderreichRules.Wunderkiste.namedNetworks() && itemStack.has(DataComponents.CUSTOM_NAME)
                    ? itemStack.getHoverName()
                    : getDomainComponent(domain);
            list.add(Component.translatable("wunderreich.wunderkiste.domain.HoverText", domainComponent).withStyle(
                    ChatFormatting.GRAY));
        }
    }
}
