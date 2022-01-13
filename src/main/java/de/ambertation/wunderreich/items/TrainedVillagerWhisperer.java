package de.ambertation.wunderreich.items;

import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.CreativeTabs;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class TrainedVillagerWhisperer extends VillagerWhisperer {
    public final static String TAG_NAME = "WhisperedEnchantment";

    public TrainedVillagerWhisperer() {
        super(40);
    }

    public static Enchantment findEnchantment(CompoundTag tag) {
        var oEnchantment = Registry.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(tag));
        if (oEnchantment.isPresent()) {
            return oEnchantment.get();
        }
        return null;
    }

    public static String findEnchantmentID(ItemStack itemStack) {
        CompoundTag tag = getEnchantment(itemStack);
        Enchantment e = findEnchantment(tag);
        if (e != null) {
            return e.getDescriptionId();
        }
        return Items.AIR.getDescriptionId();
    }

    public static ItemStack createForEnchantment(Enchantment enchantment) {
        ItemStack itemStack = new ItemStack(WunderreichItems.WHISPERER);
        setEnchantment(itemStack, enchantment);
        return itemStack;
    }

    public static CompoundTag getEnchantment(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        return tag != null ? tag.getCompound(TAG_NAME) : new CompoundTag();
    }

    public static void setEnchantment(ItemStack itemStack, Enchantment enchantment) {
        ResourceLocation resourceLocation = EnchantmentHelper.getEnchantmentId(enchantment);
        CompoundTag tag = EnchantmentHelper.storeEnchantment(resourceLocation, enchantment.getMaxLevel());
        itemStack.getOrCreateTag().put(TAG_NAME, tag);
    }

    public static void addAllVariants(List<ItemStack> itemList) {
        ImprinterRecipe.getRecipes().forEach(r -> itemList.add(createForEnchantment(r.enchantment)));
    }

    public ResourceLocation getEnchantmentID(ItemStack itemStack) {
        CompoundTag tag = getEnchantment(itemStack);
        return EnchantmentHelper.getEnchantmentId(tag);
    }

    public void appendHoverText(ItemStack itemStack,
                                @Nullable Level level,
                                List<Component> list,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        var tag = getEnchantment(itemStack);

        Registry.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(tag)).ifPresent((enchantment) -> {
            list.add(WhisperRule.getFullname(enchantment));
        });
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> itemList) {
        if (creativeModeTab == CreativeModeTab.TAB_SEARCH) {
            addAllVariants(itemList);
        } else if (creativeModeTab.getEnchantmentCategories().length != 0) {
            ImprinterRecipe.getRecipes().forEach(r -> {
                if (creativeModeTab.hasEnchantmentCategory(r.enchantment.category)) {
                    itemList.add(createForEnchantment(r.enchantment));
                }
            });
        } else if (creativeModeTab == CreativeTabs.TAB_ITEMS) {
            addAllVariants(itemList);
        }
    }
}
