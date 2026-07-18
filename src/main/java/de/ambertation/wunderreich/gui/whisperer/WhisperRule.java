package de.ambertation.wunderreich.gui.whisperer;

import de.ambertation.wunderreich.items.TrainedVillagerWhisperer;
import de.ambertation.wunderreich.recipes.ImprinterRecipe;
import de.ambertation.wunderreich.registries.WunderreichItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;


public class WhisperRule {
    private static ItemStack BLANK;
    private static Ingredient BLANK_INGREDIENT;

    // Lazily built: constructing ItemStack/Ingredient at class-load happens before the item
    // data-component registry is bound in 26.1 ("Components not bound yet").
    public static ItemStack blank() {
        if (BLANK == null) {
            BLANK = new ItemStack(WunderreichItems.BLANK_WHISPERER);
        }
        return BLANK;
    }

    public static Ingredient blankIngredient() {
        if (BLANK_INGREDIENT == null) {
            BLANK_INGREDIENT = Ingredient.of(WunderreichItems.BLANK_WHISPERER);
        }
        return BLANK_INGREDIENT;
    }
    public final Holder<Enchantment> enchantment;

    // Everything derived from the enchantment (input/output/icon stacks AND baseXP) is computed
    // lazily. Constructing an ItemStack in 26.1 eagerly reads the item's bound data components,
    // and EnchantmentInfo/LegacyEnchantmentCategories build sample ItemStacks to classify the
    // enchantment. Imprinter recipes are generated during the datapack reload's async prepare
    // phase, before components are bound, so all of this must be deferred until the values are
    // actually read (at runtime, e.g. when the GUI opens or a recipe is synced to a client).
    private final java.util.function.Supplier<ItemStack> inputSupplier;
    private final java.util.function.Supplier<ItemStack> outputSupplier;
    private final java.util.function.Supplier<ItemStack> iconSupplier;
    private final java.util.function.IntSupplier baseXPSupplier;
    private ItemStack input;
    private ItemStack output;
    private ItemStack icon;
    private Integer baseXP;

    protected WhisperRule(
            Holder<Enchantment> enchantment,
            ItemStack input,
            ItemStack output,
            int baseXP,
            ItemStack icon
    ) {
        this.enchantment = enchantment;
        this.inputSupplier = () -> input;
        this.outputSupplier = () -> output;
        this.iconSupplier = () -> icon;
        this.baseXPSupplier = () -> baseXP;
    }

    protected WhisperRule(Holder<Enchantment> enchantment) {
        this.enchantment = enchantment;
        final java.util.function.Supplier<EnchantmentInfo> nfo = memoize(() -> new EnchantmentInfo(enchantment));
        this.inputSupplier = () -> nfo.get().input();
        this.outputSupplier = () -> TrainedVillagerWhisperer.createForEnchantment(enchantment);
        this.iconSupplier = () -> nfo.get().type();
        this.baseXPSupplier = () -> nfo.get().baseXP;
    }

    private static <T> java.util.function.Supplier<T> memoize(java.util.function.Supplier<T> delegate) {
        return new java.util.function.Supplier<>() {
            private T value;
            private boolean computed;

            @Override
            public T get() {
                if (!computed) {
                    value = delegate.get();
                    computed = true;
                }
                return value;
            }
        };
    }

    public static Component getFullname(Holder<Enchantment> e) {
        return getFullname(e, e.value().getMaxLevel());
    }

    public static Component getFullname(Holder<Enchantment> eh, int lvl) {
        final Enchantment e = eh.value();
        final Identifier loc = eh.unwrapKey().orElseThrow().identifier();
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
        return isRequiredItem(itemStack, this.getInput())
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
        return isRequiredItem(recipeInput.ingredient(), this.getInput())
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
        return this.getOutput().copy();
    }

    public ItemStack getInput() {
        if (input == null) input = inputSupplier.get();
        return input;
    }

    public ItemStack getOutput() {
        if (output == null) output = outputSupplier.get();
        return output;
    }

    public ItemStack getIcon() {
        if (icon == null) icon = iconSupplier.get();
        return icon;
    }

    public int getBaseXP() {
        if (baseXP == null) baseXP = baseXPSupplier.getAsInt();
        return baseXP;
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
