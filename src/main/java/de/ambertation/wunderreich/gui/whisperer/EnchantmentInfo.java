package de.ambertation.wunderreich.gui.whisperer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;

public class EnchantmentInfo {
    public final ItemStack inputA;
    public final ItemStack type;
    public final int baseXP;

    public EnchantmentInfo(Enchantment enchantment) {
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
            inputA = new ItemStack(Blocks.IRON_BLOCK.asItem(), 2 * mul);
            type = new ItemStack(Items.ARMOR_STAND);
        } else if (enchantment.category == EnchantmentCategory.ARMOR_HEAD) {
            inputA = new ItemStack(Blocks.COPPER_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_HELMET);
        } else if (enchantment.category == EnchantmentCategory.ARMOR_CHEST) {
            inputA = new ItemStack(Blocks.OXIDIZED_COPPER.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_CHESTPLATE);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.ARMOR_LEGS) {
            inputA = new ItemStack(Blocks.EXPOSED_COPPER.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_LEGGINGS);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.ARMOR_FEET) {
            inputA = new ItemStack(Blocks.RAW_COPPER_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.LEATHER_BOOTS);
        } else if (enchantment.category == EnchantmentCategory.BOW) {
            inputA = new ItemStack(Blocks.COAL_BLOCK.asItem(), 2 * mul);
            type = new ItemStack(Items.BOW);
        } else if (enchantment.category == EnchantmentCategory.WEAPON) {
            inputA = new ItemStack(Items.LAPIS_BLOCK, 4 * mul);
            type = new ItemStack(Items.WOODEN_SWORD);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.DIGGER) {
            inputA = new ItemStack(Blocks.SMOOTH_STONE.asItem(), 5 * mul);
            type = new ItemStack(Items.WOODEN_PICKAXE);
        } else if (enchantment.category == EnchantmentCategory.FISHING_ROD) {
            inputA = new ItemStack(Blocks.GRAVEL.asItem(), Math.min(64, 32 * mul));
            type = new ItemStack(Items.FISHING_ROD);
        } else if (enchantment.category == EnchantmentCategory.TRIDENT) {
            inputA = new ItemStack(Blocks.SEA_LANTERN.asItem(), 4 * mul);
            type = new ItemStack(Items.TRIDENT);
            rarityMultiplicator = 3;
        } else if (enchantment.category == EnchantmentCategory.CROSSBOW) {
            inputA = new ItemStack(Blocks.DARK_OAK_LOG.asItem(), 4 * mul);
            type = new ItemStack(Items.CROSSBOW);
        } else if (enchantment.category == EnchantmentCategory.VANISHABLE) {
            inputA = new ItemStack(Blocks.REDSTONE_BLOCK.asItem(), 8 * mul);
            type = new ItemStack(Items.COMPASS);
        } else if (enchantment.category == EnchantmentCategory.BREAKABLE) {
            inputA = new ItemStack(Blocks.EMERALD_BLOCK.asItem(), 1 * mul);
            type = new ItemStack(Items.SMITHING_TABLE);
            rarityMultiplicator = 2;
        } else if (enchantment.category == EnchantmentCategory.WEARABLE) {
            inputA = new ItemStack(Blocks.PINK_WOOL.asItem(), 4 * mul);
            type = new ItemStack(Items.CARVED_PUMPKIN);
        } else {
            inputA = new ItemStack(Blocks.COAL_BLOCK.asItem(), 2 * mul);
            type = ItemStack.EMPTY;
        }

        baseXP = 2 * mul * rarityMultiplicator;
    }
}
