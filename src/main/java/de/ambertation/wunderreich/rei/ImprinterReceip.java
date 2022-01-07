package de.ambertation.wunderreich.rei;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.gui.whisperer.WhisperContainer;
import de.ambertation.wunderreich.gui.whisperer.WhisperRule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import ru.bclib.interfaces.UnknownReceipBookCategory;
import ru.bclib.recipes.BCLRecipeManager;

public class ImprinterReceip implements Recipe<Container>, UnknownReceipBookCategory {
    private static final int COST_A_SLOT = 0;
    private static final int COST_B_SLOT = 1;
    public final static RecipeType<ImprinterReceip> TYPE = BCLRecipeManager.registerType(Wunderreich.MOD_ID, "imprinter");

    private final Ingredient cost;
    private final Ingredient costB;
    private final WhisperRule rule;
    private final ResourceLocation id;

    ImprinterReceip(WhisperRule rule){
        this.cost = Ingredient.of(rule.cost);
        this.costB = Ingredient.of(rule.costB);
        this.rule = rule;
        this.id = Wunderreich.makeID("imprinter/" + rule.enchantment.getDescriptionId());
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
        return rule.result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
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
}
