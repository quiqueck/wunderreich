package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;


public class WhisperRule {
    public static final ItemStack BLANK = new ItemStack(WunderreichItems.BLANK_WHISPERER);
    public static final Ingredient BLANK_INGREDIENT = Ingredient.of(WunderreichItems.BLANK_WHISPERER);
    public final Holder<Enchantment> enchantment;
    public final ItemStack input;
    public final ItemStack output;
    public final ItemStack icon;
    public final int baseXP;

    private WhisperRule(Holder<Enchantment> enchantment, EnchantmentInfo nfo) {
        this(enchantment, nfo.input, nfo.baseXP, nfo.type);
    }

    protected WhisperRule(Holder<Enchantment> enchantment, ItemStack input, int baseXP) {
        this(enchantment, input, baseXP, new EnchantmentInfo(enchantment).type);
    }

    protected WhisperRule(Holder<Enchantment> enchantment, ItemStack input, int baseXP, ItemStack icon) {
        this(enchantment, input, TrainedVillagerWhisperer.createForEnchantment(enchantment), baseXP, icon);
    }

    protected WhisperRule(
            Holder<Enchantment> enchantment,
            ItemStack input,
            ItemStack output,
            int baseXP,
            ItemStack icon
    ) {
        this.enchantment = enchantment;
        this.baseXP = baseXP;
        this.output = output;
        this.input = input;
        this.icon = icon;
    }

    protected WhisperRule(Holder<Enchantment> enchantment) {
        this(enchantment, new EnchantmentInfo(enchantment));
    }

    public static Component getFullname(Holder<Enchantment> e) {
        return getFullname(e, e.value().getMaxLevel());
    }

    public static Component getFullname(Holder<Enchantment> eh, int lvl) {
        final Enchantment e = eh.value();
        final ResourceLocation loc = eh.unwrapKey().orElseThrow().location();
        MutableComponent mutableComponent = Component.translatable("enchantment." + loc.getNamespace() + "." + loc.getPath());
        if (eh.is(Enchantments.BINDING_CURSE)) {
            mutableComponent.withStyle(ChatFormatting.RED);
        } else {
            mutableComponent.withStyle(ChatFormatting.GRAY);
        }

        if (lvl != 1 || e.getMaxLevel() != 1) {
            mutableComponent
                    .append(" (")
                    .append(Component.translatable("tooltip.fragment.max"))
                    .append(" ")
                    .append(Component.translatable("enchantment.level." + lvl))
                    .append(")");
        }

        return mutableComponent;
    }

    public static boolean isRequiredItem(ItemStack itemToTest, ItemStack requiredItem) {
        if (requiredItem == null || itemToTest == null) return false;
        if (requiredItem.isEmpty() || itemToTest.isEmpty()) return false;
        return itemToTest.is(requiredItem.getItem());
    }

    public static boolean isRequiredItem(ItemStack itemToTest, Item requiredItem) {
        if (requiredItem == null || itemToTest == null) return false;
        if (itemToTest.isEmpty()) return false;
        return itemToTest.is(requiredItem);
    }

    public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
        return isRequiredItem(itemStack, this.input)
                && itemStack.getCount() >= this.getInput().getCount()
                && isRequiredItem(itemStack2, WunderreichItems.BLANK_WHISPERER)
                && itemStack2.getCount() >= 1;
    }

    public boolean take(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.satisfiedBy(itemStack, itemStack2)) {
            return false;
        } else {
            itemStack.shrink(this.getInput().getCount());
            itemStack2.shrink(1);

            return true;
        }
    }

    public boolean satisfiedBy(ImprinterRecipe.ImprinterInput recipeInput) {
        return isRequiredItem(recipeInput.ingredient(), this.input)
                && recipeInput.ingredient().getCount() >= this.getInput().getCount()
                && isRequiredItem(recipeInput.whisperer(), WunderreichItems.BLANK_WHISPERER)
                && recipeInput.whisperer().getCount() >= 1;
    }

    public boolean take(ImprinterRecipe.ImprinterInput recipeInput) {
        if (!this.satisfiedBy(recipeInput)) {
            return false;
        } else {
            recipeInput.ingredient().shrink(this.getInput().getCount());
            recipeInput.whisperer().shrink(1);

            return true;
        }
    }

    public ItemStack assemble() {
        return this.output.copy();
    }

    public ItemStack getInput() {
        return input;
    }

    public Component getNameComponent() {
        return getFullname(enchantment);
    }

    public String getName() {
        return getFullname(enchantment).getString();
    }

    public String getCategory() {
        return LegacyEnchantmentCategories.fromEnchantment(enchantment).name();
    }
}
