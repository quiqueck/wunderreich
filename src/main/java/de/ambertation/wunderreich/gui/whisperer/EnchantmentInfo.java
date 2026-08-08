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
        int baseCount;
        Item blockItem;
        // The 9:1 block/ingot-or-gem-or-dust pair vanilla has for iron, lapis, coal, redstone and
        // emerald. Left null for categories with no clean equivalent (copper's weathering stages
        // don't have one, nor do stone/gravel/logs/wool) - those just keep using blockItem.
        Item smallItem = null;
        if (category == LegacyEnchantmentCategories.ARMOR) {
            blockItem = Blocks.IRON_BLOCK.asItem();
            smallItem = Items.IRON_INGOT;
            baseCount = 2 * mul;
            typeItem = Items.ARMOR_STAND;
        } else if (category == LegacyEnchantmentCategories.ARMOR_HEAD) {
            blockItem = Blocks.COPPER_BLOCK.asItem();
            baseCount = 1 * mul;
            typeItem = Items.LEATHER_HELMET;
        } else if (category == LegacyEnchantmentCategories.ARMOR_CHEST) {
            blockItem = Blocks.OXIDIZED_COPPER.asItem();
            baseCount = 1 * mul;
            typeItem = Items.LEATHER_CHESTPLATE;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.ARMOR_LEGS) {
            blockItem = Blocks.EXPOSED_COPPER.asItem();
            baseCount = 1 * mul;
            typeItem = Items.LEATHER_LEGGINGS;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.ARMOR_FEET) {
            blockItem = Blocks.RAW_COPPER_BLOCK.asItem();
            baseCount = 1 * mul;
            typeItem = Items.LEATHER_BOOTS;
        } else if (category == LegacyEnchantmentCategories.BOW) {
            blockItem = Blocks.COAL_BLOCK.asItem();
            smallItem = Items.COAL;
            baseCount = 2 * mul;
            typeItem = Items.BOW;
        } else if (category == LegacyEnchantmentCategories.WEAPON) {
            blockItem = Items.LAPIS_BLOCK;
            smallItem = Items.LAPIS_LAZULI;
            baseCount = 4 * mul;
            typeItem = Items.WOODEN_SWORD;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.DIGGER) {
            blockItem = Blocks.SMOOTH_STONE.asItem();
            baseCount = 5 * mul;
            typeItem = Items.WOODEN_PICKAXE;
        } else if (category == LegacyEnchantmentCategories.FISHING_ROD) {
            blockItem = Blocks.GRAVEL.asItem();
            baseCount = Math.min(64, 32 * mul);
            typeItem = Items.FISHING_ROD;
        } else if (category == LegacyEnchantmentCategories.TRIDENT) {
            blockItem = Blocks.SEA_LANTERN.asItem();
            baseCount = 4 * mul;
            typeItem = Items.TRIDENT;
            rarityMultiplicator = 3;
        } else if (category == LegacyEnchantmentCategories.CROSSBOW) {
            blockItem = Blocks.DARK_OAK_LOG.asItem();
            baseCount = 4 * mul;
            typeItem = Items.CROSSBOW;
        } else if (category == LegacyEnchantmentCategories.VANISHABLE) {
            blockItem = Blocks.REDSTONE_BLOCK.asItem();
            smallItem = Items.REDSTONE;
            baseCount = 8 * mul;
            typeItem = Items.COMPASS;
        } else if (category == LegacyEnchantmentCategories.BREAKABLE) {
            blockItem = Blocks.EMERALD_BLOCK.asItem();
            smallItem = Items.EMERALD;
            baseCount = 1 * mul;
            typeItem = Items.SMITHING_TABLE;
            rarityMultiplicator = 2;
        } else if (category == LegacyEnchantmentCategories.WEARABLE) {
            blockItem = Blocks.PINK_WOOL.asItem();
            baseCount = 4 * mul;
            typeItem = Items.CARVED_PUMPKIN;
        } else {
            blockItem = Blocks.COAL_BLOCK.asItem();
            smallItem = Items.COAL;
            baseCount = 2 * mul;
            typeItem = null;
        }

        // Categories bucket many enchantments behind the same fixed item, and anvilCost only has a
        // handful of distinct values, so `mul` alone collapses most of a category onto identical
        // input counts (e.g. several weapon enchantments all costing the same amount of Lapis
        // Block). Nudge the *input count* apart with a small, flat bonus from the enchantment's max
        // level - added after the category multiplier (not before) so it isn't itself amplified by
        // categories with a large per-unit factor (e.g. VANISHABLE's 8x).
        int levelBonus = Math.min(4, enchantment.value().getMaxLevel() - 1);

        // Where a block/small-item pair exists, use enchantment weight (rarity, independent of
        // anvilCost and maxLevel) to also swap the item itself: rarer enchantments (low weight)
        // keep the block, more common ones (higher weight) use the cheaper small-item form. This is
        // a third, independent axis, so two enchantments now have to share rarity tier, max level
        // AND commonality-tier to still collide - much rarer than sharing just the first two.
        boolean useSmall = smallItem != null && d.weight() > 2;
        inputItem = useSmall ? smallItem : blockItem;

        // Final tiebreak for enchantments that still land on the same rarity/level/weight bucket
        // (e.g. bane_of_arthropods/density/smite). String.hashCode() is specified by the JDK to be
        // stable across runs/JVMs, so this offset is deterministic - the same enchantment always
        // gets the same cost, it's just no longer forced to exactly match its bucket-mates. Vanilla
        // blocks are worth 9x their small-item form, so a small-item recipe can take a 9x wider
        // offset range (27 vs 3) for the same proportional value swing.
        int idHash = enchantment.unwrapKey().orElseThrow().location().toString().hashCode();
        int hashRange = useSmall ? 3 * 9 : 3;
        int hashOffset = 1 + Math.floorMod(idHash, hashRange);
        inputCount = Math.min(64, baseCount + levelBonus + hashOffset);

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
