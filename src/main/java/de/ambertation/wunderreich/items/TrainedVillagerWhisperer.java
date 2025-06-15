package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.data_components.WhisperData;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;

import static de.ambertation.wunderreich.registries.WunderreichDataComponents.WHISPERER;

import java.util.List;

public class TrainedVillagerWhisperer extends VillagerWhisperer {
    public TrainedVillagerWhisperer() {
        super(WunderreichRules.Whispers.trainedDurability());
    }

    public static ItemStack createForEnchantment(Holder<Enchantment> enchantment) {
        ItemStack itemStack = new ItemStack(WunderreichItems.WHISPERER);
        setEnchantment(itemStack, enchantment);
        return itemStack;
    }

    public static ResourceKey<Enchantment> getEnchantment(ItemStack itemStack) {
        final WhisperData data = itemStack.get(WHISPERER);
        return data != null ? data.enchantmentKey() : null;
    }

    public static void setEnchantment(ItemStack itemStack, Holder<Enchantment> enchantment) {
        if (enchantment != null && enchantment.unwrapKey().isPresent()) {
            itemStack.set(WHISPERER, new WhisperData(enchantment.unwrapKey().get()));
        }
    }

    public static void addAllVariants(List<ItemStack> itemList) {
        ImprinterRecipe.getAllVariants()
                       .filter(r -> r != null)
                       .forEach(r -> itemList.add(createForEnchantment(r.enchantment)));
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext tooltipContext,
            List<Component> list,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        final ResourceKey<Enchantment> key = getEnchantment(itemStack);

        final var enchantments = tooltipContext.registries().lookup(Registries.ENCHANTMENT).orElse(null);
        if (enchantments != null) {
            enchantments.get(key).ifPresent((enchantment) -> {
                list.add(WhisperRule.getFullname(enchantment));
            });
        }
    }
}
