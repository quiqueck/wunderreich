package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.advancements.AdvancementsJsonBuilder;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.registries.WunderreichRecipes;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class StonecutterJsonBuilder {
    private final ResourceLocation ID;
    private boolean canBuild;

    private StonecutterJsonBuilder(ResourceLocation ID) {
        this.ID = ID;
        canBuild = Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get();
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
        return new ResourceLocation("failed");
    }


    public static StonecutterJsonBuilder create(String name) {
        ResourceLocation id = Wunderreich.ID(name + "_stonecutter");
        StonecutterJsonBuilder b = new StonecutterJsonBuilder(id);
        return b;
    }

    private ItemLike resultItem;

    public StonecutterJsonBuilder result(ItemLike item) {
        canBuild &= isEnabled(item);
        this.resultItem = item;
        return this;
    }


    private ItemLike ingredient = null;

    public StonecutterJsonBuilder ingredient(ItemLike item) {
        canBuild &= isEnabled(item);
        ingredient = item;
        return this;
    }

    private int count = 1;

    public StonecutterJsonBuilder count(int count) {
        this.count = count;
        return this;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public JsonElement registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType type) {
        List<Item> items = new ArrayList<>(1);
        if (ingredient instanceof Block bl) {
            items.add(bl.asItem());
        } else if (ingredient instanceof Item itm) {
            items.add(itm);
        }
        return registerAndCreateAdvancement(type, items);
    }

    public JsonElement registerAndCreateAdvancement(AdvancementsJsonBuilder.AdvancementType type,
                                                    List<Item> items) {
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
        if (!canBuild) return null;

        JsonElement res = build();
        WunderreichRecipes.RECIPES.put(ID, res);

        return res;
    }


    public JsonElement build() {
        if (!canBuild) return null;

        if (resultItem == null) {
            throw new IllegalStateException("A Recipe needs a Result (" + ID + ")");
        }
        if (ingredient == null) {
            throw new IllegalStateException("A Recipe needs an Ingredient (" + ID + ")");
        }

        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");


        JsonObject ing = new JsonObject();
        ing.addProperty("item", getKey(ingredient).toString());
        json.add("ingredient", ing);


        json.addProperty("result", getKey(resultItem).toString());
        json.addProperty("count", count);

        //System.out.println(json);
        return json;
    }
}
