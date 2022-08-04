package de.ambertation.wunderreich.recipes;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.registries.WunderreichRecipes;

import java.util.*;

public class RecipeJsonBuilder {
    private static final ThreadLocal<RecipeJsonBuilder> BUILDER = ThreadLocal.withInitial(RecipeJsonBuilder::new);
    final Map<Character, Ingredient> materials = new HashMap<>();
    ResourceLocation ID;
    boolean canBuild;
    ItemLike resultItem;
    String[] pattern;
    String group = "";
    int count;

    private RecipeJsonBuilder() {
    }

    public static void invalidate() {
        BUILDER.remove();
    }

    private static boolean isEnabled(ItemLike item) {
        if (item instanceof Block bl) {
            return Configs.BLOCK_CONFIG.isEnabled(bl);
        } else if (item instanceof Item itm) {
            return Configs.ITEM_CONFIG.isEnabled(itm);
        }
        return false;
    }

    private static ResourceLocation getKey(ItemLike item) {
        if (item instanceof Block bl) {
            return Registry.BLOCK.getKey(bl);
        } else if (item instanceof Item itm) {
            return Registry.ITEM.getKey(itm);
        }
        return null;
    }

    public static RecipeJsonBuilder create(String name) {
        ResourceLocation id = Wunderreich.ID(name);
        RecipeJsonBuilder b = BUILDER.get().reset(id);
        return b;
    }

    private RecipeJsonBuilder reset(ResourceLocation ID) {
        this.ID = ID;
        this.canBuild = Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get();
        this.resultItem = null;
        this.pattern = new String[0];
        this.materials.clear();
        this.count = 1;
        return this;
    }

    public RecipeJsonBuilder result(ItemLike item) {
        canBuild &= isEnabled(item);
        this.resultItem = item;
        return this;
    }


    public RecipeJsonBuilder pattern(String row1, String row2, String row3) {
        this.pattern = new String[]{row1, row2, row3};
        return this;
    }

    public RecipeJsonBuilder pattern(String row1, String row2) {
        this.pattern = new String[]{row1, row2};
        return this;
    }

    public RecipeJsonBuilder pattern(String row1) {
        this.pattern = new String[]{row1};
        return this;
    }

    public RecipeJsonBuilder material(Character c, ItemLike... items) {
        return material(c, Ingredient.of(items));
    }

    public RecipeJsonBuilder material(Character c, ItemStack... items) {
        return material(c, Ingredient.of(items));
    }

    public RecipeJsonBuilder material(Character c, Ingredient ing) {
        canBuild &= Arrays
                .stream(ing.getItems())
                .map(ItemStack::getItem)
                .map(RecipeJsonBuilder::isEnabled)
                .noneMatch(v -> !v);

        materials.put(c, ing);
        return this;
    }

    public RecipeJsonBuilder count(int count) {
        this.count = count;
        return this;
    }

    public RecipeJsonBuilder group(String grp) {
        this.group = grp;
        return this;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public JsonElement registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType type) {
        List<Item> items = new ArrayList<>(materials.size());
        for (var mat : materials.values()) {
            for (var item : mat.getItems()) {
                items.add(item.getItem());
            }
        }
        return registerAndCreateAdvancement(type, items);
    }

    public JsonElement registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType type, List<Item> items) {
        JsonElement res = register();
        if (res == null) return null;
        if (items.size() == 0) return res;

        AdvancementsJsonBuilder b = null;
        if (resultItem instanceof Block bl) {
            b = AdvancementsJsonBuilder.createRecipe(bl.asItem(), type);
        } else if (resultItem instanceof Item itm) {
            b = AdvancementsJsonBuilder.createRecipe(itm, type);
        }

        if (b != null) {
            int ct = 0;
            for (var item : items) {
                final String name = "has_" + ct++;
                b.inventoryChangedCriteria(name, item);
            }

            b.register();
        }
        return res;
    }

    public JsonElement register() {
        if (!canBuild) {
            Wunderreich.LOGGER.info("Discarding Recipe for " + this.ID);
            return null;
        }

        JsonElement res = build();
        WunderreichRecipes.RECIPES.put(ID, res);

        return res;
    }


    public JsonElement build() {
        if (!canBuild) {
            Wunderreich.LOGGER.info("Discarding Recipe for " + this.ID);
            return null;
        }

        if (resultItem == null) {
            throw new IllegalStateException("A Recipe needs a Result (" + ID + ")");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");

        JsonArray patternArray = new JsonArray();
        for (String s : pattern) patternArray.add(s);
        json.add("pattern", patternArray);

        JsonObject individualKey;
        JsonArray individualContainer;
        JsonObject keyList = new JsonObject();

        for (var mat : materials.entrySet()) {
            Ingredient ing = mat.getValue();
            ItemStack[] items = ing.getItems();

            individualContainer = new JsonArray();
            for (ItemStack stack : items) {
                individualKey = new JsonObject();
                final ResourceLocation il = getKey(stack.getItem());
                if (il == null) {
                    Wunderreich.LOGGER.info("Ignoring Recipe for " + this.ID + " due to missing item.");
                    return null;
                }
                individualKey.addProperty("item", il.toString());
                if (stack.getCount() > 1) {
                    individualKey.addProperty("count", stack.getCount());
                }
                individualContainer.add(individualKey);
            }
            keyList.add(mat.getKey() + "", individualContainer);
        }
        json.add("key", keyList);

        JsonObject result = new JsonObject();
        final ResourceLocation resItem = getKey(resultItem);
        if (resItem == null) {
            Wunderreich.LOGGER.info("Ignoring Recipe for " + this.ID + " due to missing result item.");
            return null;
        }
        result.addProperty("item", resItem.toString());
        result.addProperty("count", count);
        json.add("result", result);

        if (group != null && !group.isBlank()) {
            json.addProperty("group", group);
        }

        //System.out.println(json);
        return json;
    }
}
