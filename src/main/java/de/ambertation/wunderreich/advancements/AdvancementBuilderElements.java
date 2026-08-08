package de.ambertation.wunderreich.advancements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

class Display {
    static final ThreadLocal<Display> DISPLAY = ThreadLocal.withInitial(Display::new);
    String frame;
    String background;
    boolean showToast;
    boolean announceToChat;
    boolean hidden;
    private String icon;
    private String title;
    private String description;

    private Display() {
    }

    Display reset(String icon, String title, String description) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        frame = "task";
        background = null;
        showToast = true;
        announceToChat = true;
        hidden = false;
        return this;
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();

        JsonObject obj = new JsonObject();
        obj.add("id", new JsonPrimitive(icon));
        obj.add("count", new JsonPrimitive(1));
        root.add("icon", obj);

        obj = new JsonObject();
        obj.add("translate", new JsonPrimitive(title));
        root.add("title", obj);

        obj = new JsonObject();
        obj.add("translate", new JsonPrimitive(description));
        root.add("description", obj);

        if (frame != null) {
            root.add("frame", new JsonPrimitive(frame));
        }

        root.add("show_toast", new JsonPrimitive(showToast));
        root.add("announce_to_chat", new JsonPrimitive(announceToChat));
        root.add("hidden", new JsonPrimitive(hidden));

        if (background != null) {
            root.add("background", new JsonPrimitive(background));
        }
        return root;
    }
}

class Reward {
    void serialize(JsonObject obj) {
    }
}

class RecipeReward extends Reward {
    private final List<String> recipes = new ArrayList<>(1);

    void addRecipe(String item) {
        recipes.add(item);
    }

    void serialize(JsonObject obj) {
        if (recipes.size() > 0) {
            JsonArray f = new JsonArray();
            recipes.stream().map(JsonPrimitive::new).forEach(f::add);
            obj.add("recipes", f);
        }
    }
}


class Criteria {
    private final String trigger;
    private final List<Condition> conditions = new ArrayList<>(1);

    Criteria(String trigger) {
        this.trigger = trigger;
    }

    void addCondition(Condition item) {
        conditions.add(item);
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", trigger);
        if (conditions.size() > 0) {
            JsonObject f = new JsonObject();
            for (Condition c : conditions) {
                c.serialize(f);
            }
            root.add("conditions", f);
        }
        return root;
    }
}

class Condition {
    private final String type;

    Condition(String type) {
        this.type = type;
    }

    void serialize(JsonObject obj) {
    }
}

/**
 * 26.3 changed {@code RecipeUnlockedTrigger.TriggerInstance} from a single {@code "recipe"} id to a
 * required {@code "recipes"} field holding a {@code HolderSet<Recipe<?>>}
 * ({@code Recipe.LIST_CODEC.fieldOf("recipes")}). The holder-set codec accepts a bare id, a list of ids
 * or a {@code "#tag"}; we always emit a list so the shape is unambiguous.
 */
class RecipeCondition extends Condition {
    private final String recipe;

    RecipeCondition(String recipe) {
        super("recipes");
        this.recipe = recipe;
    }

    void serialize(JsonObject obj) {
        super.serialize(obj);

        JsonArray recipes = new JsonArray();
        recipes.add(new JsonPrimitive(recipe));
        obj.add("recipes", recipes);
    }
}

/**
 * Unchanged for 26.3: {@code InventoryChangeTrigger.TriggerInstance} still declares
 * {@code ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of())}, and {@code ItemPredicate}
 * still declares {@code Item.CODEC(HolderSet).optionalFieldOf("items")} - so the nested
 * {@code "items": [ { "items": [ ...ids... ] } ]} shape below is still correct.
 */
class ItemCondition extends Condition {
    private final List<String> items = new ArrayList<>(1);

    ItemCondition() {
        super("items");
    }

    void addItem(String item) {
        items.add(item);
    }

    void serialize(JsonObject obj) {
        super.serialize(obj);
        JsonObject root = new JsonObject();
        if (items.size() > 0) {
            JsonArray f = new JsonArray();
            items.stream().forEach(f::add);
            root.add("items", f);
        }

        JsonArray a = new JsonArray();
        a.add(root);
        obj.add("items", a);
    }
}
