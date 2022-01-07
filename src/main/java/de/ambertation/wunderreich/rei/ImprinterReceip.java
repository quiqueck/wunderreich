package de.ambertation.wunderreich.rei;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhisperContainer;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import ru.bclib.recipes.BCLRecipeManager;
import ru.bclib.util.ItemUtil;

public record ImprinterReceip(ResourceLocation id,
                              Ingredient inputA,
                              Ingredient inputB,
                              ItemStack output) implements Recipe<Container> {
    static final int COST_A_SLOT = 0;
    static final int COST_B_SLOT = 1;



    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, inputA, inputB);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return this.inputA.test(inv.getItem(COST_A_SLOT)) && this.inputB.test(inv.getItem(COST_B_SLOT)) ||
                this.inputA.test(inv.getItem(COST_B_SLOT)) && this.inputB.test(inv.getItem(COST_A_SLOT));
    }

    @Override
    public ItemStack assemble(Container container) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
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

    public static ImprinterReceip of(WhisperRule rule) {
        return new ImprinterReceip(
                Wunderreich.makeID(Type.ID + "/" + rule.enchantment.getDescriptionId()),
                Ingredient.of(rule.cost),
                Ingredient.of(rule.costB),
                rule.result
        );
    }

    public static void register() {
        WhisperContainer.getAllEnchants().forEach(rule ->
                BCLRecipeManager.addRecipe(Type.INSTANCE, of(rule))
        );
    }

    public static class Type implements RecipeType<ImprinterReceip> {
        public static final String ID = "imprinter";
        public static final RecipeType<ImprinterReceip> INSTANCE = Registry.register(Registry.RECIPE_TYPE, Wunderreich.makeID(Type.ID), new Type());

        Type(){ }

        @Override
        public String toString() {
            return "Imprinter Recipe";
        }
    }



    private static class Serializer implements RecipeSerializer<ImprinterReceip> {
        public final static Serializer INSTANCE = Registry.register(Registry.RECIPE_SERIALIZER, Wunderreich.makeID(Type.ID), new Serializer());

        static class ImprinterRecipeJsonFormat {
            JsonObject inputA;
            JsonObject inputB;
            JsonObject output;
        }

        @Override
        public ImprinterReceip fromNetwork(ResourceLocation id, FriendlyByteBuf packetBuffer) {
            Ingredient costA = Ingredient.fromNetwork(packetBuffer);
            Ingredient costB = Ingredient.fromNetwork(packetBuffer);
            ItemStack output = packetBuffer.readItem();

            return new ImprinterReceip(id, costA, costB, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetBuffer, ImprinterReceip recipe) {
            recipe.inputA.toNetwork(packetBuffer);
            recipe.inputB.toNetwork(packetBuffer);
            packetBuffer.writeItem(recipe.output);
        }

        @Override
        public ImprinterReceip fromJson(ResourceLocation id, JsonObject json) {
            ImprinterRecipeJsonFormat recipeJson = new Gson().fromJson(json, ImprinterRecipeJsonFormat.class);

            if (recipeJson.inputA == null) {
                throw new JsonSyntaxException("The Attribute 'inputA' is missing.");
            }
            if (recipeJson.inputB == null) {
                throw new JsonSyntaxException("The Attribute 'inputB' is missing.");
            }
            if (recipeJson.output == null) {
                throw new JsonSyntaxException("The Attribute 'output' is missing.");
            }


            Ingredient inputA = Ingredient.fromJson(recipeJson.inputA);
            Ingredient inputB = Ingredient.fromJson(recipeJson.inputB);
            ItemStack result = ItemUtil.fromJsonRecipe(recipeJson.output);

            if (result == null) {
                throw new IllegalStateException("The output item does not exists!");
            }

            return new ImprinterReceip(id, inputA, inputB, result);
        }

    }
}
