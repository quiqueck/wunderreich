package de.ambertation.wunderreich.rei;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhisperContainer;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
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
import ru.bclib.interfaces.UnknownReceipBookCategory;
import ru.bclib.recipes.BCLRecipeManager;
import ru.bclib.util.ItemUtil;

public class ImprinterReceip implements Recipe<Container>, UnknownReceipBookCategory {
    private static final int COST_A_SLOT = 0;
    private static final int COST_B_SLOT = 1;
    public final static String GROUP = "imprinter";
    public final static RecipeType<ImprinterReceip> TYPE = BCLRecipeManager.registerType(Wunderreich.MOD_ID, GROUP);
    public final static Serializer SERIALIZER = BCLRecipeManager.registerSerializer(
            Wunderreich.MOD_ID,
            GROUP,
            new Serializer()
    );

    private final Ingredient cost;
    private final Ingredient costB;
    private final ItemStack result;
    private final ResourceLocation id;

    ImprinterReceip(WhisperRule rule){
        this(
            Wunderreich.makeID(GROUP+"/" + rule.enchantment.getDescriptionId()),
            Ingredient.of(rule.cost),
            Ingredient.of(rule.costB),
            rule.result
        );
    }

    ImprinterReceip(ResourceLocation id, Ingredient cost, Ingredient costB, ItemStack result){
        this.cost = cost;
        this.costB = costB;
        this.id = id;
        this.result = result;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return this.cost.test(inv.getItem(COST_A_SLOT)) && this.costB.test(inv.getItem(COST_B_SLOT)) ||
               this.cost.test(inv.getItem(COST_B_SLOT)) && this.costB.test(inv.getItem(COST_A_SLOT));
    }

    @Override
    public ItemStack assemble(Container container) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public static ImprinterReceip of(WhisperRule rule){
        return new ImprinterReceip(rule);
    }

    public static void register(){
        WhisperContainer.getAllEnchants().forEach(rule ->
            BCLRecipeManager.addRecipe(TYPE, of(rule))
        );
    }

    private static class Serializer implements RecipeSerializer<ImprinterReceip> {
        @Override
        public ImprinterReceip fromNetwork(ResourceLocation id, FriendlyByteBuf packetBuffer) {
            Ingredient costA = Ingredient.fromNetwork(packetBuffer);
            Ingredient costB = Ingredient.fromNetwork(packetBuffer);
            ItemStack output = packetBuffer.readItem();

            return new ImprinterReceip(id, costA, costB, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetBuffer, ImprinterReceip recipe) {
            recipe.cost.toNetwork(packetBuffer);
            recipe.costB.toNetwork(packetBuffer);
            packetBuffer.writeItem(recipe.result);
        }

        @Override
        public ImprinterReceip fromJson(ResourceLocation id, JsonObject json) {
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            Ingredient costA = Ingredient.fromJson(ingredients.get(0));
            Ingredient costB = Ingredient.fromJson(ingredients.get(1));

            JsonObject resultObject = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = ItemUtil.fromJsonRecipe(resultObject);

            if (result == null) {
                throw new IllegalStateException("Result item does not exists!");
            }

            return new ImprinterReceip(id, costA, costB, result);
        }

    }
}
