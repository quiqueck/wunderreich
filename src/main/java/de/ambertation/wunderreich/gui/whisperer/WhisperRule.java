package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.registries.WunderreichItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;

public class WhisperRule {
    public final Enchantment enchantment;
    public final String name;
    public final ItemStack cost;
    public final ItemStack costB = new ItemStack(WunderreichItems.BLANK_WHISPERER);
    public final ItemStack result;
    public final ItemStack type;
    public final int baseXP;

    WhisperRule(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.name = getFullname(enchantment).getString();

        int mul;
        if (enchantment.getRarity() == Enchantment.Rarity.VERY_RARE) {
            mul = 6;
        } else if (enchantment.getRarity() == Enchantment.Rarity.RARE) {
            mul = 4;
        } else if (enchantment.getRarity() == Enchantment.Rarity.UNCOMMON) {
            mul = 2;
        } else {
            mul = 1;
        }

        int rarityMultiplicator = 1;
        if (enchantment.category == EnchantmentCategory.ARMOR) {
            cost = new ItemStack(Blocks.IRON_BLOCK.asItem(), 2 * mul);
            type = new ItemStack(Items.ARMOR_STAND);
        } else if (enchantment.category == EnchantmentCategory.ARMOR_HEAD) {
            cost = new ItemStack(Blocks.COPPER_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_HELMET);
        } else if (enchantment.category == EnchantmentCategory.ARMOR_CHEST) {
            cost = new ItemStack(Blocks.OXIDIZED_COPPER.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_CHESTPLATE);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.ARMOR_LEGS) {
            cost = new ItemStack(Blocks.EXPOSED_COPPER.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_LEGGINGS);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.ARMOR_FEET) {
            cost = new ItemStack(Blocks.RAW_COPPER_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_BOOTS);
        } else if (enchantment.category == EnchantmentCategory.BOW) {
            cost = new ItemStack(Blocks.COAL_BLOCK.asItem(), 2 * mul);
            type = new ItemStack(Items.BOW);
        } else if (enchantment.category == EnchantmentCategory.WEAPON) {
            cost = new ItemStack(Items.LAPIS_BLOCK, 4 * mul);
            type = new ItemStack(Items.WOODEN_SWORD);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.DIGGER) {
            cost = new ItemStack(Blocks.SMOOTH_STONE.asItem(), 5 * mul);
            type = new ItemStack(Items.WOODEN_PICKAXE);
        } else if (enchantment.category == EnchantmentCategory.FISHING_ROD) {
            cost = new ItemStack(Blocks.GRAVEL.asItem(), Math.min(64, 32 * mul));
            type = new ItemStack(Items.FISHING_ROD);
        } else if (enchantment.category == EnchantmentCategory.TRIDENT) {
            cost = new ItemStack(Blocks.SEA_LANTERN.asItem(), 4 * mul);
            type = new ItemStack(Items.TRIDENT);
            rarityMultiplicator = 3;
        } else if (enchantment.category == EnchantmentCategory.CROSSBOW) {
            cost = new ItemStack(Blocks.DARK_OAK_LOG.asItem(), 4 * mul);
            type = new ItemStack(Items.CROSSBOW);
        } else if (enchantment.category == EnchantmentCategory.VANISHABLE) {
            cost = new ItemStack(Blocks.REDSTONE_BLOCK.asItem(), 8 * mul);
            type = new ItemStack(Items.COMPASS);
        } else if (enchantment.category == EnchantmentCategory.BREAKABLE) {
            cost = new ItemStack(Blocks.EMERALD_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.SMITHING_TABLE);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.WEARABLE) {
            cost = new ItemStack(Blocks.PINK_WOOL.asItem(), 4 * mul);
            type = new ItemStack(Items.CARVED_PUMPKIN);
        } else {
            cost = new ItemStack(Blocks.COAL_BLOCK.asItem(), 2 * mul);
            type = ItemStack.EMPTY;
        }

        baseXP = 2 * mul * rarityMultiplicator;
        result = TrainedVillagerWhisperer.createForEnchantment(enchantment);
    }

    private boolean isRequiredItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty() && itemStack.isEmpty()) {
            return true;
        } else {
            ItemStack itemStack3 = itemStack.copy();
            if (itemStack3.getItem().canBeDepleted()) {
                itemStack3.setDamageValue(itemStack3.getDamageValue());
            }

            return ItemStack.isSame(itemStack3, itemStack2) && (!itemStack2.hasTag() || itemStack3.hasTag() && NbtUtils.compareNbt(itemStack2.getTag(), itemStack3.getTag(), false));
        }
    }

    public boolean satisfiedBy(ItemStack itemStack, ItemStack itemStack2) {
        return this.isRequiredItem(itemStack, this.cost) && itemStack.getCount() >= this.cost.getCount() && this.isRequiredItem(itemStack2, this.costB) && itemStack2.getCount() >= this.costB.getCount();
    }

    public ItemStack assemble() {
        return this.result.copy();
    }



    public boolean take(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.satisfiedBy(itemStack, itemStack2)) {
            return false;
        } else {
            itemStack.shrink(this.cost.getCount());
            itemStack2.shrink(this.costB.getCount());

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
}
