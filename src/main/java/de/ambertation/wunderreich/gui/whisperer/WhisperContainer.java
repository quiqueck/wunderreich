package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.rei.ImprinterRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WhisperContainer implements Container {
    private final NonNullList<ItemStack> itemStacks;
    private int lastSelectedRule;
    @Nullable
    private WhisperRule activeRule;

    WhisperContainer() {
        this.itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.itemStacks.stream().allMatch(stack -> stack.isEmpty());

//        Iterator var1 = this.itemStacks.iterator();
//
//        ItemStack itemStack;
//        do {
//            if (!var1.hasNext()) {
//                return true;
//            }
//
//            itemStack = (ItemStack) var1.next();
//        } while (itemStack.isEmpty());
//
//        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.itemStacks.get(i);
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        ItemStack slot = this.itemStacks.get(slotIndex);
        if (slotIndex == WhispererMenu.RESULT_SLOT && !slot.isEmpty()) {
            return ContainerHelper.removeItem(this.itemStacks, slotIndex, slot.getCount());
        } else {
            ItemStack removedItem = ContainerHelper.removeItem(this.itemStacks, slotIndex, count);
            if (!removedItem.isEmpty() && this.isIngredientSlot(slotIndex)) {
                this.updateResultItem();
            }

            return removedItem;
        }
    }

    private boolean isIngredientSlot(int slotIndex) {
        return slotIndex == WhispererMenu.INGREDIENT_SLOT_A || slotIndex == WhispererMenu.INGREDIENT_SLOT_B;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        return ContainerHelper.takeItem(this.itemStacks, slotIndex);
    }

    @Override
    public void setItem(int slotIndex, ItemStack newStack) {
        this.itemStacks.set(slotIndex, newStack);
        if (!newStack.isEmpty() && newStack.getCount() > this.getMaxStackSize()) {
            newStack.setCount(this.getMaxStackSize());
        }

        if (this.isIngredientSlot(slotIndex)) {
            this.updateResultItem();
        }

    }

    public void setChanged() {
        this.updateResultItem();
    }

    public void updateResultItem() {
        this.activeRule = null;
        ItemStack costA;
        ItemStack costB;
        if (this.itemStacks.get(WhispererMenu.INGREDIENT_SLOT_A).isEmpty()) {
            costA = this.itemStacks.get(WhispererMenu.INGREDIENT_SLOT_B);
            costB = ItemStack.EMPTY;
        } else {
            costA = this.itemStacks.get(WhispererMenu.INGREDIENT_SLOT_A);
            costB = this.itemStacks.get(WhispererMenu.INGREDIENT_SLOT_B);
        }

        if (costA.isEmpty()) {
            this.setItem(WhispererMenu.RESULT_SLOT, ItemStack.EMPTY);
        } else {
            var enchantments = ImprinterRecipe.getReceips();
            if (!enchantments.isEmpty()) {
                WhisperRule rule = getIngredientsFor(costA, costB, this.lastSelectedRule);
                if (rule == null) {
                    this.activeRule = rule;
                    rule = getIngredientsFor(costB, costA, this.lastSelectedRule);
                }

                if (rule != null) {
                    this.activeRule = rule;
                    this.setItem(WhispererMenu.RESULT_SLOT, rule.assemble());
                } else {
                    this.setItem(WhispererMenu.RESULT_SLOT, ItemStack.EMPTY);
                }
            }
        }
    }

    @Nullable
    public WhisperRule getIngredientsFor(ItemStack slotA, ItemStack slotB, int preferedIndex) {
        var all = ImprinterRecipe.getReceips();
        WhisperRule rule;
        if (preferedIndex > 0 && preferedIndex < all.size()) {
            rule = all.get(preferedIndex);
            return rule.satisfiedBy(slotA, slotB) ? rule : null;
        } else {
            for (int ruleIndex = 0; ruleIndex < all.size(); ++ruleIndex) {
                rule = all.get(ruleIndex);
                if (rule.satisfiedBy(slotA, slotB)) {
                    return rule;
                }
            }

            return null;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Nullable
    public WhisperRule getActiveRule() {
        return this.activeRule;
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    public void setLastSelectedRule(int i) {
        this.lastSelectedRule = i;
        this.updateResultItem();
    }
}
