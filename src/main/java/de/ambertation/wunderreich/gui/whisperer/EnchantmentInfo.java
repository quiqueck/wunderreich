package de.ambertation.wunderreich.gui.whisperer;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;

public class EnchantmentInfo {
    public final int baseXP;

    // NOTE: In 26.1 constructing an ItemStack eagerly reads the item's data components from
    // its registry Holder. During the datapack reload's async "prepare" phase (where custom
    // imprinter recipes are generated) those components are not yet bound, so building the
    // stacks at construction throws "Components not bound yet". We therefore only remember the
    // item + count here and materialize the ItemStacks lazily (on first access at runtime).
    private final Item inputItem;
    private final int inputCount;
    private final Item typeItem; // null => ItemStack.EMPTY

    private ItemStack input;
    private ItemStack type;

    public EnchantmentInfo(Holder<Enchantment> enchantment) {
        final var d = enchantment.value().definition();
        int mul = Math.min(6, d.anvilCost());
        var category = LegacyEnchantmentCategories.fromEnchantment(enchantment);
        int rarityMultiplicator = 1;
        if (category == LegacyEnchantmentCategories.ARMOR) {
            inputItem = Blocks.IRON_BLOCK.asItem();
            inputCount = 2 * mul;
            typeItem = Items.ARMOR_STAND;
        } else if (category == LegacyEnchantmentCategories.ARMOR_HEAD) {
            inputItem = Blocks.COPPER_BLOCK.asItem();
            inputCount = 1 * mul;
            typeItem = Items.LEATHER_HELMET;
        } else if (category == LegacyEnchantmentCategories.ARMOR_CHEST) {
            inputItem = Blocks.OXIDIZED_COPPER.asItem();
            inputCount = 1 * mul;
            typeItem = Items.LEATHER_CHESTPLATE;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.ARMOR_LEGS) {
            inputItem = Blocks.EXPOSED_COPPER.asItem();
            inputCount = 1 * mul;
            typeItem = Items.LEATHER_LEGGINGS;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.ARMOR_FEET) {
            inputItem = Blocks.RAW_COPPER_BLOCK.asItem();
            inputCount = 1 * mul;
            typeItem = Items.LEATHER_BOOTS;
        } else if (category == LegacyEnchantmentCategories.BOW) {
            inputItem = Blocks.COAL_BLOCK.asItem();
            inputCount = 2 * mul;
            typeItem = Items.BOW;
        } else if (category == LegacyEnchantmentCategories.WEAPON) {
            inputItem = Items.LAPIS_BLOCK;
            inputCount = 4 * mul;
            typeItem = Items.WOODEN_SWORD;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.DIGGER) {
            inputItem = Blocks.SMOOTH_STONE.asItem();
            inputCount = 5 * mul;
            typeItem = Items.WOODEN_PICKAXE;
        } else if (category == LegacyEnchantmentCategories.FISHING_ROD) {
            inputItem = Blocks.GRAVEL.asItem();
            inputCount = Math.min(64, 32 * mul);
            typeItem = Items.FISHING_ROD;
        } else if (category == LegacyEnchantmentCategories.TRIDENT) {
            inputItem = Blocks.SEA_LANTERN.asItem();
            inputCount = 4 * mul;
            typeItem = Items.TRIDENT;
            rarityMultiplicator = 3;
        } else if (category == LegacyEnchantmentCategories.CROSSBOW) {
            inputItem = Blocks.DARK_OAK_LOG.asItem();
            inputCount = 4 * mul;
            typeItem = Items.CROSSBOW;
        } else if (category == LegacyEnchantmentCategories.VANISHABLE) {
            inputItem = Blocks.REDSTONE_BLOCK.asItem();
            inputCount = 8 * mul;
            typeItem = Items.COMPASS;
        } else if (category == LegacyEnchantmentCategories.BREAKABLE) {
            inputItem = Blocks.EMERALD_BLOCK.asItem();
            inputCount = 1 * mul;
            typeItem = Items.SMITHING_TABLE;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.WEARABLE) {
            inputItem = Blocks.PINK_WOOL.asItem();
            inputCount = 4 * mul;
            typeItem = Items.CARVED_PUMPKIN;
        } else {
            inputItem = Blocks.COAL_BLOCK.asItem();
            inputCount = 2 * mul;
            typeItem = null;
        }

        baseXP = 2 * mul * rarityMultiplicator;
    }

    public ItemStack input() {
        if (input == null) {
            input = new ItemStack(inputItem, inputCount);
        }
        return input;
    }

    public ItemStack type() {
        if (type == null) {
            type = typeItem == null ? ItemStack.EMPTY : new ItemStack(typeItem);
        }
        return type;
    }
}
