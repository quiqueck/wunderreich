package de.ambertation.wunderreich.rei;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.EnchantmentInfo;
import de.ambertation.wunderreich.gui.whisperer.WhisperContainer;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import de.ambertation.wunderreich.registries.WunderreichBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import ru.bclib.recipes.BCLRecipeManager;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ImprinterRecipe extends WhisperRule implements Recipe<WhisperContainer> {
    static final int COST_A_SLOT = 0;
    static final int COST_B_SLOT = 1;
    private static final List<ImprinterRecipe> RECIPES = new LinkedList<>();
    private final ResourceLocation id;

    private ImprinterRecipe(ResourceLocation id, Enchantment enchantment, Ingredient inputA, Ingredient inputB, int baseXP) {
        super(enchantment, inputA, inputB, baseXP);
        this.id = id;
    }

    private ImprinterRecipe(ResourceLocation id, Enchantment enchantment, Ingredient inputA, Ingredient inputB, ItemStack output, int baseXP, ItemStack type) {
        super(enchantment, inputA, inputB, output, baseXP, type);
        this.id = id;
    }

    private ImprinterRecipe(Enchantment e) {
        super(e);

        this.id = Wunderreich.makeID(ImprinterRecipe.Type.ID + "/" + enchantment.getDescriptionId());
    }

    public static List<ImprinterRecipe> getRecipes() {
        return RECIPES;
    }

    public static void register() {
        Registry.register(Registry.RECIPE_SERIALIZER, Serializer.ID, Serializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, Wunderreich.makeID(Type.ID), Type.INSTANCE);

        RECIPES.clear();
        List<Enchantment> enchants = new LinkedList<>();
        Registry.ENCHANTMENT.forEach(e -> {
            enchants.add(e);
        });
        enchants.sort(Comparator.comparing(a -> a.category + ":" + WhisperRule.getFullname(a).getString()));

        enchants.forEach(e -> {
            ImprinterRecipe r = new ImprinterRecipe(e);
            RECIPES.add(r);
            BCLRecipeManager.addRecipe(Type.INSTANCE, r);
        });
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(WunderreichBlocks.WHISPER_IMPRINTER);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, inputA, inputB);
    }

    @Override
    public boolean matches(WhisperContainer inv, Level level) {
        if (inv.getContainerSize() < 2) return false;
        return this.inputA.test(inv.getItem(COST_A_SLOT)) && this.inputB.test(inv.getItem(COST_B_SLOT)) ||
                this.inputA.test(inv.getItem(COST_B_SLOT)) && this.inputB.test(inv.getItem(COST_A_SLOT));
    }

    @Override
    public ItemStack assemble(WhisperContainer container) {
        return this.output.copy();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImprinterRecipe)) return false;
        ImprinterRecipe that = (ImprinterRecipe) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Type implements RecipeType<ImprinterRecipe> {
        public static final String ID = "imprinter";
        public static final RecipeType<ImprinterRecipe> INSTANCE = new Type(); //Registry.register(Registry.RECIPE_TYPE, Wunderreich.makeID(ID+"_recipe"), new Type());

        Type() {
        }

        @Override
        public String toString() {
            return ID;
        }
    }

    private static class Serializer implements RecipeSerializer<ImprinterRecipe> {
        public final static ResourceLocation ID = Wunderreich.makeID(Type.ID);
        public final static Serializer INSTANCE = new Serializer(); //Registry.register(Registry.RECIPE_SERIALIZER, Wunderreich.makeID(Type.ID), new Serializer());

        @Override
        public ImprinterRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf packetBuffer) {
            Ingredient costA = Ingredient.fromNetwork(packetBuffer);
            Ingredient costB = Ingredient.fromNetwork(packetBuffer);
            ItemStack output = packetBuffer.readItem();
            /*byte packetType = */
            packetBuffer.readByte(); //this is a type we currently do not use
            ItemStack type = packetBuffer.readItem();
            int baseXP = packetBuffer.readVarInt();

            ResourceLocation eID = packetBuffer.readResourceLocation();
            var enchantment = Registry.ENCHANTMENT.getOptional(eID);

            return new ImprinterRecipe(
                    id,
                    enchantment.orElseThrow(() -> new RuntimeException("Unknown Enchantment " + eID)),
                    costA,
                    costB,
                    output,
                    baseXP,
                    type
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ImprinterRecipe recipe) {
            recipe.inputA.toNetwork(buf);
            recipe.inputB.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeByte(0); //this is a type we currently do not use
            buf.writeItem(recipe.type);
            buf.writeVarInt(recipe.baseXP);
            buf.writeResourceLocation(EnchantmentHelper.getEnchantmentId(recipe.enchantment));
        }

        @Override
        public ImprinterRecipe fromJson(ResourceLocation id, JsonObject json) {
            ImprinterRecipeJsonFormat recipeJson = new Gson().fromJson(json, ImprinterRecipeJsonFormat.class);

            if (recipeJson.inputA == null) {
                throw new JsonSyntaxException("The Attribute 'inputA' is missing.");
            }
            if (recipeJson.inputB == null) {
                throw new JsonSyntaxException("The Attribute 'inputB' is missing.");
            }
            if (recipeJson.enchantment == null) {
                throw new JsonSyntaxException("The Attribute 'output' is missing.");
            }

            ResourceLocation eID = new ResourceLocation(recipeJson.enchantment);
            var enchantment = Registry.ENCHANTMENT.getOptional(eID);
            if (!enchantment.isPresent()) {
                throw new JsonParseException("Unknown Enchantment " + eID);
            }


            Ingredient inputA = Ingredient.fromJson(recipeJson.inputA);
            Ingredient inputB = Ingredient.fromJson(recipeJson.inputB);

            if (recipeJson.xp <= 0) {
                EnchantmentInfo ei = new EnchantmentInfo(enchantment.get());
                recipeJson.xp = ei.baseXP;
            }


            ImprinterRecipe r = new ImprinterRecipe(id, enchantment.get(), inputA, inputB, recipeJson.xp);

            RECIPES.remove(r);
            RECIPES.add(r);

            return r;
        }

        static class ImprinterRecipeJsonFormat {
            JsonObject inputA;
            JsonObject inputB;
            int xp;
            String enchantment;
        }
    }
}
