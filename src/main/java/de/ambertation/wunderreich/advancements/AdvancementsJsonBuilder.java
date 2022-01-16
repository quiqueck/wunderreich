package de.ambertation.wunderreich.advancements;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.registries.WunderreichAdvancements;

import net.minecraft.advancements.FrameType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AdvancementsJsonBuilder {

    public static class CriteriaBuilder {
        final AdvancementsJsonBuilder base;
        final Criteria criteria;

        CriteriaBuilder(AdvancementsJsonBuilder base, Criteria c) {
            this.base = base;
            criteria = c;
        }

        protected CriteriaBuilder addCondition(Condition c) {
            criteria.addCondition(c);
            return this;
        }

        public CriteriaBuilder itemsCondition(Item... items) {
            ItemCondition cond = new ItemCondition();
            for (Item item : items) {
                var id = Registry.ITEM.getKey(item);
                if (id == null) {
                    base.canBuild = false;
                    continue;
                }
                cond.addItem(id.toString());
            }
            criteria.addCondition(cond);
            return this;
        }

        public CriteriaBuilder recipeCondition(Item item) {
            var id = Registry.ITEM.getKey(item);
            if (id == null) {
                base.canBuild = false;
                return this;
            }
            return recipeCondition(id.toString());
        }

        public CriteriaBuilder recipeCondition(String id) {
            RecipeCondition cond = new RecipeCondition(id);
            criteria.addCondition(cond);
            return this;
        }
    }

    public static class DisplayBuilder {
        final AdvancementsJsonBuilder base;
        final Display display;

        DisplayBuilder(AdvancementsJsonBuilder base, Display display) {
            this.base = base;
            this.display = display;
        }

        protected DisplayBuilder frame(String value) {
            display.frame = value;
            return this;
        }

        public DisplayBuilder background(String value) {
            display.background = value;
            return this;
        }

        public DisplayBuilder showToast() {
            display.showToast = true;
            return this;
        }

        public DisplayBuilder hideToast() {
            display.showToast = false;
            return this;
        }

        public DisplayBuilder hidden() {
            display.hidden = true;
            return this;
        }

        public DisplayBuilder visible() {
            display.hidden = false;
            return this;
        }

        public DisplayBuilder announceToChat() {
            display.announceToChat = true;
            return this;
        }

        public DisplayBuilder hideFromChat() {
            display.announceToChat = false;
            return this;
        }

        public DisplayBuilder frame(FrameType type) {
            return frame(type.getName());
        }

        public DisplayBuilder challenge() {
            return frame(FrameType.CHALLENGE);
        }

        public DisplayBuilder task() {
            return frame(FrameType.TASK);
        }

        public DisplayBuilder goal() {
            return frame(FrameType.GOAL);
        }
    }

    public final ResourceLocation ID;
    public final AdvancementType type;
    private String parent = null;
    private Display display;
    private final Map<String, Criteria> criteria = new HashMap<>();
    private final List<Reward> rewards = new ArrayList<>(0);
    private boolean canBuild = true;

    public enum AdvancementType {
        REGULAR,
        RECIPE_DECORATIONS,
        RECIPE_TOOL
    }

    private AdvancementsJsonBuilder(ResourceLocation id, AdvancementType type) {
        if (type == AdvancementType.RECIPE_DECORATIONS) {
            ID = new ResourceLocation(id.getNamespace(), "recipes/decorations/" + id.getPath());
            parent = "minecraft:recipes/root";
        } else if (type == AdvancementType.RECIPE_TOOL) {
            ID = new ResourceLocation(id.getNamespace(), "recipes/tools/" + id.getPath());
            parent = "minecraft:recipes/root";
        } else {
            ID = id;
        }

        this.type = type;
    }

    public static AdvancementsJsonBuilder create(String name) {
        return new AdvancementsJsonBuilder(Wunderreich.ID(name), AdvancementType.REGULAR);
    }

    public static AdvancementsJsonBuilder create(String name, AdvancementType type) {
        return new AdvancementsJsonBuilder(Wunderreich.ID(name), type);
    }

    public static AdvancementsJsonBuilder create(Item item) {
        return create(item, AdvancementType.REGULAR, (b) -> {
        });
    }

    public static AdvancementsJsonBuilder create(Item item, AdvancementType type) {
        return create(item, type, (b) -> {
        });
    }

    public static AdvancementsJsonBuilder create(Item item, Consumer<DisplayBuilder> builder) {
        return create(item, AdvancementType.REGULAR, builder);
    }

    public static AdvancementsJsonBuilder create(Item item, AdvancementType type, Consumer<DisplayBuilder> builder) {
        var id = Registry.ITEM.getKey(item);
        boolean canBuild = true;
        if (id == null) {
            canBuild = false;
            id = Registry.ITEM.getDefaultKey();
        }

        String baseName = "advancements." + id.getNamespace() + "." + id.getPath() + ".";
        AdvancementsJsonBuilder b = new AdvancementsJsonBuilder(id, type);
        if (builder != null) b.startDisplay(item, baseName + "title", baseName + "description", builder);
        b.canBuild = canBuild;
        return b;
    }

    public static AdvancementsJsonBuilder createRecipe(Item item, AdvancementType type) {
        return create(item, type, null).awardRecipe(item).gotRecipeCriteria("has_the_recipe", item);
    }


    public AdvancementsJsonBuilder parent(AdvancementsJsonBuilder parent) {
        this.parent = parent.ID.toString();
        return this;
    }

    public AdvancementsJsonBuilder parent(String parent) {
        this.parent = parent;
        return this;
    }

    public AdvancementsJsonBuilder startDisplay(Item icon,
                                                Consumer<DisplayBuilder> builder) {
        String baseName = "advancements." + ID.getNamespace() + "." + ID.getPath() + ".";
        return startDisplay(icon, baseName + "title", baseName + "description", builder);
    }

    public AdvancementsJsonBuilder startDisplay(Item icon,
                                                String title,
                                                String description,
                                                Consumer<DisplayBuilder> builder) {
        var id = Registry.ITEM.getKey(icon);
        if (id == null) {
            id = Registry.ITEM.getDefaultKey();
            canBuild = false;
        }
        display = new Display(id.toString(), title, description);
        builder.accept(new DisplayBuilder(this, display));
        return this;
    }

    public AdvancementsJsonBuilder gotRecipeCriteria(String name, Item item) {
        return startCriteria(name, "minecraft:recipe_unlocked", builder -> builder.recipeCondition(item));
    }


    public AdvancementsJsonBuilder inventoryChangedCriteria(String name, Item... items) {
        return startCriteria(name, "minecraft:inventory_changed", builder -> builder.itemsCondition(items));
    }

    public AdvancementsJsonBuilder startCriteria(String name, String trigger, Consumer<CriteriaBuilder> builder) {
        Criteria c = new Criteria(trigger);
        criteria.put(name, c);
        builder.accept(new CriteriaBuilder(this, c));
        return this;
    }

    public AdvancementsJsonBuilder awardRecipe(Item... items) {
        RecipeReward rew = new RecipeReward();
        for (Item item : items) {
            var id = Registry.ITEM.getKey(item);
            if (id == null) continue;
            rew.addRecipe(id.toString());
        }
        rewards.add(rew);
        return this;
    }

    public JsonElement register() {
        if (!canBuild) return null;

        JsonElement res = build();
        WunderreichAdvancements.ADVANCEMENTS.put(ID, res);
        return res;
    }


    public JsonElement build() {
        if (!canBuild) return null;

        JsonObject root = new JsonObject();
        if (parent != null) {
            root.add("parent", new JsonPrimitive(parent));
        }
        if (display != null) {
            root.add("display", display.serialize());
        }

        if (!criteria.isEmpty()) {
            JsonObject critObj = new JsonObject();
            JsonArray requirements = new JsonArray();

            criteria.entrySet().forEach(e -> {
                critObj.add(e.getKey(), e.getValue().serialize());
                requirements.add(new JsonPrimitive(e.getKey()));
            });

            root.add("criteria", critObj);
            JsonArray a = new JsonArray();
            a.add(requirements);
            root.add("requirements", a);
        }

        if (!rewards.isEmpty()) {
            JsonObject obj = new JsonObject();
            rewards.stream().forEach(r -> r.serialize(obj));
            root.add("rewards", obj);
        }

        return root;
    }
}
