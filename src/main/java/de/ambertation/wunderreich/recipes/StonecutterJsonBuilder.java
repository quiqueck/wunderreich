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
    private static final ThreadLocal<StonecutterJsonBuilder> BUILDER = ThreadLocal.withInitial(StonecutterJsonBuilder::new);
    private ResourceLocation ID;
    private boolean canBuild;
    private ItemLike resultItem;
    private ItemLike ingredient;
    private int count = 1;

    private StonecutterJsonBuilder() {
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

    public static StonecutterJsonBuilder create(String name) {
        ResourceLocation id = Wunderreich.ID(name + "_stonecutter");
        StonecutterJsonBuilder b = BUILDER.get().reset(id);
        return b;
    }

    private StonecutterJsonBuilder reset(ResourceLocation ID) {
        this.ID = ID;
        canBuild = Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get();
        resultItem = null;
        ingredient = null;
        count = 1;

        return this;
    }

    public StonecutterJsonBuilder result(ItemLike item) {
        canBuild &= isEnabled(item);
        this.resultItem = item;
        return this;
    }


    public StonecutterJsonBuilder ingredient(ItemLike item) {
        canBuild &= isEnabled(item);
        ingredient = item;
        return this;
    }

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
        if (ingredient == null) {
            throw new IllegalStateException("A Recipe needs an Ingredient (" + ID + ")");
        }

        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");


        JsonObject ing = new JsonObject();
        final ResourceLocation ingredientLoc = getKey(ingredient);
        if (ingredientLoc == null) {
            Wunderreich.LOGGER.info("Ignoring Stonecutter-Recipe for " + this.ID + " due to missing ingredient.");
            return null;
        }
        ing.addProperty("item", ingredientLoc.toString());
        json.add("ingredient", ing);


        final ResourceLocation resLoc = getKey(resultItem);
        if (resLoc == null) {
            Wunderreich.LOGGER.info("Ignoring Stonecutter-Recipe for " + this.ID + " due to missing result item.");
            return null;
        }
        json.addProperty("result", resLoc.toString());
        json.addProperty("count", count);

        //System.out.println(json);
        return json;
    }
}
