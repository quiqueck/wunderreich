package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.data_components.WhisperData;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import static de.ambertation.wunderreich.registries.WunderreichDataComponents.WHISPERER;
import de.ambertation.wunderreich.registries.WunderreichItems;
import de.ambertation.wunderreich.registries.WunderreichRules;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class TrainedVillagerWhisperer extends VillagerWhisperer {
    public TrainedVillagerWhisperer(ResourceKey<Item> key) {
        super(WunderreichRules.Whispers.trainedDurability(), key);
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

    /**
     * One trained whisperer per imprinter recipe.
     *
     * @param level the level whose imprinter recipes to enumerate, see
     *              {@link ImprinterRecipe#getAllVariants(Level)}. May be {@code null} before a world
     *              is loaded, which limits the list to the recipes generated in this JVM.
     */
    public static void addAllVariants(List<ItemStack> itemList, @Nullable Level level) {
        ImprinterRecipe.getAllVariants(level)
                       .filter(r -> r != null)
                       .forEach(r -> itemList.add(createForEnchantment(r.enchantment)));
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext tooltipContext,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> consumer,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        final ResourceKey<Enchantment> key = getEnchantment(itemStack);

        final var enchantments = tooltipContext.registries().lookup(Registries.ENCHANTMENT).orElse(null);
        if (enchantments != null) {
            enchantments.get(key).ifPresent((enchantment) -> {
                consumer.accept(WhisperRule.getFullname(enchantment));
            });
        }
    }
}
