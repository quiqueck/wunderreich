package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.blocks.WunderKisteBlock;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;
import de.ambertation.wunderreich.utils.WunderKisteDomain;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WunderKisteItem extends BlockItem {
    public WunderKisteItem(Block block) {
        super(block, WunderreichItems.makeItemSettings());
    }

    public static WunderKisteDomain getDomain(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            tag = tag.getCompound(BlockItem.BLOCK_STATE_TAG);
            if (tag.contains(WunderKisteBlock.DOMAIN.getName())) {
                String domainName = tag.getString(WunderKisteBlock.DOMAIN.getName());
                return WunderKisteBlock.DOMAIN.getValue(domainName).orElse(WunderKisteBlock.DEFAULT_DOMAIN);
            }
        }
        return WunderKisteBlock.DEFAULT_DOMAIN;
    }

    public static ItemStack setDomain(ItemStack itemStack, WunderKisteDomain domain) {
        CompoundTag tag = new CompoundTag();

        if (!WunderKisteBlock.DEFAULT_DOMAIN.equals(domain)) {
            tag.putString(WunderKisteBlock.DOMAIN.getName(), domain.toString());
            itemStack.addTagElement(BlockItem.BLOCK_STATE_TAG, tag);
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
            @NotNull ItemStack itemStack,
            @Nullable Level level,
            @NotNull List<Component> list,
            @NotNull TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        if (WunderreichRules.Wunderkiste.haveMultiple()) {
            final WunderKisteDomain domain = getDomain(itemStack);

            Component domainComponent = WunderreichRules.Wunderkiste.namedNetworks() && itemStack.hasCustomHoverName()
                    ? itemStack.getHoverName()
                    : getDomainComponent(domain);
            list.add(Component.translatable("wunderreich.wunderkiste.domain.HoverText", domainComponent).withStyle(
                    ChatFormatting.GRAY));
        }
    }
}
