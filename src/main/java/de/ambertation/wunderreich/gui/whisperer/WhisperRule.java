package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.registries.WunderreichItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;


public class WhisperRule {
    public final Enchantment enchantment;
    public final Ingredient inputA;
    public final Ingredient inputB;
    public final ItemStack output;
    public final ItemStack type;
    public final int baseXP;

    private WhisperRule(Enchantment enchantment, EnchantmentInfo nfo) {
        this(enchantment, Ingredient.of(nfo.inputA), Ingredient.of(new ItemStack(WunderreichItems.BLANK_WHISPERER)), nfo.baseXP, nfo.type);
    }

    protected WhisperRule(Enchantment enchantment, Ingredient inputA, Ingredient inputB, int baseXP) {
        this(enchantment, inputA, inputB, baseXP, new EnchantmentInfo(enchantment).type);
    }

    protected WhisperRule(Enchantment enchantment, Ingredient inputA, Ingredient inputB, int baseXP, ItemStack type) {
        this(enchantment, inputA, inputB, TrainedVillagerWhisperer.createForEnchantment(enchantment), baseXP, type);
    }

    protected WhisperRule(Enchantment enchantment, Ingredient inputA, Ingredient inputB, ItemStack output, int baseXP, ItemStack type) {
        this.enchantment = enchantment;
        this.baseXP = baseXP;
        this.output = output;
        this.inputA = inputA;
        this.inputB = inputB;
        this.type = type;
    }

    protected WhisperRule(Enchantment enchantment) {
        this(enchantment, new EnchantmentInfo(enchantment));
    }

    private boolean isRequiredItem(ItemStack itemStack, Ingredient itemStack2) {
        if (itemStack2.isEmpty() && itemStack.isEmpty()) {
            return true;
        } else {
            ItemStack itemStack3 = itemStack.copy();
            if (itemStack3.getItem().canBeDepleted()) {
                itemStack3.setDamageValue(itemStack3.getDamageValue());
            }


            return itemStack2.test(itemStack3) /*&& (!itemStack2.hasTag() || itemStack3.hasTag() && NbtUtils.compareNbt(itemStack2.getTag(), itemStack3.getTag(), false))*/;
        }
    }

    public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
        return this.isRequiredItem(itemStack, this.inputA) && itemStack.getCount() >= this.getInputA().getCount() && this.isRequiredItem(itemStack2, this.inputB) && itemStack2.getCount() >= this.getInputB().getCount();
    }

    public ItemStack assemble() {
        return this.output.copy();
    }

    public ItemStack getInputA(){
        return inputA.getItems()[0];
    }
    public ItemStack getInputB(){
        return inputB.getItems()[0];
    }


    public boolean take(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.satisfiedBy(itemStack, itemStack2)) {
            return false;
        } else {
            itemStack.shrink(this.getInputA().getCount());
            itemStack2.shrink(this.getInputB().getCount());

            return true;
        }
    }

    public Component getNameComponent(){
        return getFullname(enchantment);
    }

    public static Component getFullname(Enchantment e) {
        return getFullname(e, e.getMaxLevel());
    }

    public static Component getFullname(Enchantment e, int lvl) {
        MutableComponent mutableComponent = new TranslatableComponent(e.getDescriptionId());
        if (e.isCurse()) {
            mutableComponent.withStyle(ChatFormatting.RED);
        } else {
            mutableComponent.withStyle(ChatFormatting.GRAY);
        }

        if (lvl != 1 || e.getMaxLevel() != 1) {
            mutableComponent
                    .append(" (")
                    .append(new TranslatableComponent("tooltip.fragment.max"))
                    .append(" ")
                    .append(new TranslatableComponent("enchantment.level." + lvl))
                    .append(")");
        }

        return mutableComponent;
    }

    public String getName() {
        return getFullname(enchantment).getString();
    }

    public String getCategory() {
        return enchantment.category.name();
    }
}
