package de.ambertation.wunderreich.gui.whisperer;

import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public enum LegacyEnchantmentCategories {
    ARMOR, ARMOR_FEET, ARMOR_LEGS, ARMOR_CHEST,
    ARMOR_HEAD,
    WEAPON,
    DIGGER,
    FISHING_ROD,
    TRIDENT,
    BREAKABLE,
    BOW,
    WEARABLE,
    CROSSBOW,
    VANISHABLE, Other;

    private static boolean hasSlot(Enchantment.EnchantmentDefinition d, EquipmentSlotGroup slot) {
        return d.slots().contains(slot);
    }

    private static boolean hasTag(Enchantment.EnchantmentDefinition d, TagKey<Item> tag) {
        return d.supportedItems().unwrapKey().map(t -> t.equals(tag)).orElse(false);
    }


    public static LegacyEnchantmentCategories fromEnchantment(Holder<Enchantment> h) {
        if (h.is(Enchantments.VANISHING_CURSE)) return VANISHABLE;
        if (h.is(Enchantments.BINDING_CURSE)) return WEARABLE;
        if (h.is(Enchantments.MENDING)) return BREAKABLE;
        if (h.is(Enchantments.UNBREAKING)) return BREAKABLE;

        final Enchantment e = h.value();
        final Enchantment.EnchantmentDefinition d = e.definition();
        if (hasSlot(d, EquipmentSlotGroup.HEAD)) return ARMOR_HEAD;
        if (hasSlot(d, EquipmentSlotGroup.FEET)) return ARMOR_FEET;
        if (hasSlot(d, EquipmentSlotGroup.LEGS)) return ARMOR_LEGS;
        if (e.canEnchant(new ItemStack(Items.IRON_BOOTS)) &&
                e.canEnchant(new ItemStack(Items.IRON_LEGGINGS)) &&
                e.canEnchant(new ItemStack(Items.IRON_CHESTPLATE)) &&
                e.canEnchant(new ItemStack(Items.IRON_HELMET))) return ARMOR;
        if (hasTag(d, ItemTags.DURABILITY_ENCHANTABLE)) return BREAKABLE;

        if (hasTag(d, ItemTags.LEG_ARMOR_ENCHANTABLE)) return ARMOR_LEGS;
        if (e.canEnchant(new ItemStack(Items.IRON_LEGGINGS))) return ARMOR_LEGS;
        if (hasTag(d, ItemTags.FOOT_ARMOR_ENCHANTABLE)) return ARMOR_FEET;
        if (e.canEnchant(new ItemStack(Items.IRON_BOOTS))) return ARMOR_FEET;
        if (hasTag(d, ItemTags.MELEE_WEAPON_ENCHANTABLE)) return WEAPON;
        if (hasTag(d, ItemTags.WEAPON_ENCHANTABLE)) return WEAPON;
        if (hasTag(d, ItemTags.FIRE_ASPECT_ENCHANTABLE)) return WEAPON;
        if (hasTag(d, ItemTags.SHARP_WEAPON_ENCHANTABLE)) return WEAPON;
        if (e.canEnchant(new ItemStack(Items.IRON_SWORD))) return WEAPON;
        if (hasTag(d, ItemTags.MINING_LOOT_ENCHANTABLE)) return DIGGER;
        if (hasTag(d, ItemTags.MINING_ENCHANTABLE)) return DIGGER;
        if (e.canEnchant(new ItemStack(Items.IRON_PICKAXE))) return DIGGER;
        if (hasTag(d, ItemTags.TRIDENT_ENCHANTABLE)) return TRIDENT;
        if (e.canEnchant(new ItemStack(Items.TRIDENT))) return TRIDENT;
        if (hasTag(d, ItemTags.BOW_ENCHANTABLE)) return BOW;
        if (e.canEnchant(new ItemStack(Items.BOW))) return BOW;
        if (hasTag(d, ItemTags.CROSSBOW_ENCHANTABLE)) return CROSSBOW;
        if (e.canEnchant(new ItemStack(Items.CROSSBOW))) return CROSSBOW;
        if (hasTag(d, ItemTags.FISHING_ENCHANTABLE)) return WEARABLE;
        if (e.canEnchant(new ItemStack(Items.FISHING_ROD))) return FISHING_ROD;
        if (hasTag(d, ItemTags.MACE_ENCHANTABLE)) return WEAPON;
        if (e.canEnchant(new ItemStack(Items.MACE))) return WEAPON;
        if (hasTag(d, ItemTags.CHEST_ARMOR_ENCHANTABLE)) return ARMOR_CHEST;
        if (e.canEnchant(new ItemStack(Items.IRON_CHESTPLATE))) return ARMOR_CHEST;
        if (hasTag(d, ItemTags.ARMOR_ENCHANTABLE)) return ARMOR_CHEST;
        if (hasSlot(d, EquipmentSlotGroup.ARMOR)) return ARMOR;

        if (hasTag(d, ItemTags.VANISHING_ENCHANTABLE)) return VANISHABLE;
        if (hasTag(d, ItemTags.EQUIPPABLE_ENCHANTABLE)) return WEARABLE;
        return Other;
    }
}
