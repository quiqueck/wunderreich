package de.ambertation.wunderreich.recipes;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.Configs;
import de.ambertation.wunderreich.registries.WunderreichRecipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Builds the JSON for a {@link AgingRecipe} at runtime, in the same way
 * {@link StonecutterJsonBuilder} does for stonecutting.
 * <p>
 * The emitted object matches {@link AgingRecipe.Serializer#CODEC}:
 * <pre>{@code
 * {
 *   "type": "wunderreich:aging",
 *   "input": "minecraft:cobblestone",
 *   "catalyst": ["minecraft:vine", "minecraft:moss_block"],
 *   "result": {"id": "minecraft:mossy_cobblestone", "count": 1},
 *   "time": 400,
 *   "experience": 0.16
 * }
 * }</pre>
 * {@code input} and {@code catalyst} are read by {@code Ingredient.CODEC}, which accepts a single
 * item id, an array of item ids or a {@code "#namespace:tag"} string &mdash; but <b>not</b> an
 * object.
 * <p>
 * There is deliberately no {@code registerAndCreateAdvancement(...)}: aging happens inside a
 * machine and the recipe is not shown in the recipe book (see
 * {@link de.ambertation.wunderreich.recipes.catalyst.CatalystRecipe#display()}), so a recipe
 * unlock advancement would have nothing to unlock.
 */
public class AgingRecipeJsonBuilder {
    private static final ThreadLocal<AgingRecipeJsonBuilder> BUILDER = ThreadLocal.withInitial(AgingRecipeJsonBuilder::new);

    private Identifier ID;
    private boolean canBuild;
    private ItemLike[] inputItems;
    private TagKey<Item> inputTag;
    private ItemLike[] catalystItems;
    private TagKey<Item> catalystTag;
    private ItemLike resultItem;
    private int count = 1;
    private int time = AgingRecipe.DEFAULT_TIME;
    private float experience = 0f;

    private AgingRecipeJsonBuilder() {
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

    private static Identifier getKey(ItemLike item) {
        if (item instanceof Block bl) {
            return BuiltInRegistries.BLOCK.getKey(bl);
        } else if (item instanceof Item itm) {
            return BuiltInRegistries.ITEM.getKey(itm);
        }
        return null;
    }

    public static AgingRecipeJsonBuilder create(String name) {
        Identifier id = Wunderreich.ID(name + "_aging");
        AgingRecipeJsonBuilder b = BUILDER.get().reset(id);
        return b;
    }

    private AgingRecipeJsonBuilder reset(Identifier ID) {
        this.ID = ID;
        this.canBuild = Configs.RECIPE_CONFIG.newBooleanFor(ID.getPath(), ID).get();
        this.inputItems = null;
        this.inputTag = null;
        this.catalystItems = null;
        this.catalystTag = null;
        this.resultItem = null;
        this.count = 1;
        this.time = AgingRecipe.DEFAULT_TIME;
        this.experience = 0f;

        return this;
    }

    /**
     * The item that is consumed when the recipe finishes. Passing more than one item makes any of
     * them a valid input.
     */
    public AgingRecipeJsonBuilder input(ItemLike... items) {
        for (ItemLike item : items) canBuild &= isEnabled(item);
        this.inputItems = items;
        this.inputTag = null;
        return this;
    }

    public AgingRecipeJsonBuilder input(TagKey<Item> tag) {
        this.inputItems = null;
        this.inputTag = tag;
        return this;
    }

    /**
     * The item that has to be present but is never consumed.
     */
    public AgingRecipeJsonBuilder catalyst(ItemLike... items) {
        for (ItemLike item : items) canBuild &= isEnabled(item);
        this.catalystItems = items;
        this.catalystTag = null;
        return this;
    }

    public AgingRecipeJsonBuilder catalyst(TagKey<Item> tag) {
        this.catalystItems = null;
        this.catalystTag = tag;
        return this;
    }

    public AgingRecipeJsonBuilder result(ItemLike item) {
        canBuild &= isEnabled(item);
        this.resultItem = item;
        return this;
    }

    public AgingRecipeJsonBuilder count(int count) {
        if (count < 1 || count > 99) {
            throw new IllegalArgumentException("An aging result count must be in [1, 99] (" + ID + ")");
        }
        this.count = count;
        return this;
    }

    /**
     * The number of ticks the Chronarium needs for this recipe.
     */
    public AgingRecipeJsonBuilder time(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("An aging time must be at least 1 tick (" + ID + ")");
        }
        this.time = ticks;
        return this;
    }

    /**
     * The experience awarded per completed result, exactly like the {@code experience} of a vanilla
     * smelting recipe. Defaults to {@code 0}.
     */
    public AgingRecipeJsonBuilder experience(float experience) {
        if (experience < 0 || !Float.isFinite(experience)) {
            throw new IllegalArgumentException("An aging experience must be a finite value >= 0 (" + ID + ")");
        }
        this.experience = experience;
        return this;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public JsonElement register() {
        if (!canBuild) {
            Wunderreich.LOGGER.info("Discarding Recipe for " + this.ID);
            return null;
        }

        JsonElement res = build();
        if (res == null) return null;
        WunderreichRecipes.RECIPES.put(ID, res);

        return res;
    }

    public JsonElement build() {
        if (!canBuild) {
            Wunderreich.LOGGER.info("Discarding Recipe for " + this.ID);
            return null;
        }

        if (resultItem == null) {
            throw new IllegalStateException("An Aging-Recipe needs a Result (" + ID + ")");
        }
        if (inputItems == null && inputTag == null) {
            throw new IllegalStateException("An Aging-Recipe needs an Input (" + ID + ")");
        }
        if (catalystItems == null && catalystTag == null) {
            throw new IllegalStateException("An Aging-Recipe needs a Catalyst (" + ID + ")");
        }

        final JsonElement input = ingredient(inputItems, inputTag, "input");
        if (input == null) return null;

        final JsonElement catalyst = ingredient(catalystItems, catalystTag, "catalyst");
        if (catalyst == null) return null;

        final Identifier resLoc = getKey(resultItem);
        if (resLoc == null) {
            Wunderreich.LOGGER.info("Ignoring Aging-Recipe for " + this.ID + " due to missing result item.");
            return null;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", AgingRecipe.Type.ID.toString());
        json.add("input", input);
        json.add("catalyst", catalyst);

        JsonObject stack = new JsonObject();
        stack.addProperty("id", resLoc.toString());
        stack.addProperty("count", count);
        json.add("result", stack);

        json.addProperty("time", time);
        // Optional in the codec (default 0), but always written so the emitted recipe is
        // self describing when it is dumped for debugging.
        json.addProperty("experience", experience);

        return json;
    }

    /**
     * {@code Ingredient.CODEC} is a {@code HolderSetCodec}: a tag is written as {@code "#ns:path"},
     * a single item as its plain id and everything else as an array of ids.
     */
    private JsonElement ingredient(ItemLike[] items, TagKey<Item> tag, String what) {
        if (tag != null) {
            return new JsonPrimitive("#" + tag.location());
        }

        if (items.length == 0) {
            throw new IllegalStateException("The " + what + " of an Aging-Recipe must not be empty (" + ID + ")");
        }

        if (items.length == 1) {
            final Identifier loc = getKey(items[0]);
            if (loc == null) {
                Wunderreich.LOGGER.info("Ignoring Aging-Recipe for " + this.ID + " due to missing " + what + ".");
                return null;
            }
            return new JsonPrimitive(loc.toString());
        }

        JsonArray array = new JsonArray();
        for (ItemLike item : items) {
            final Identifier loc = getKey(item);
            if (loc == null) {
                Wunderreich.LOGGER.info("Ignoring Aging-Recipe for " + this.ID + " due to missing " + what + ".");
                return null;
            }
            array.add(loc.toString());
        }
        return array;
    }
}
