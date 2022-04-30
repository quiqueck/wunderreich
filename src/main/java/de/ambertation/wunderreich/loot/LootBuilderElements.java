package de.ambertation.wunderreich.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

interface EntryList {
    void addEntry(Entry e);
}

class EntryFunction {
    private final String function;

    protected EntryFunction(String f) {
        this.function = f;
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.add("function", new JsonPrimitive(function));
        return root;
    }
}

class ExplosionDecayFunction extends EntryFunction {
    ExplosionDecayFunction() {
        super("minecraft:explosion_decay");
    }
}

class SetCountFunction extends EntryFunction {
    private final int count;
    private final boolean add;

    SetCountFunction(int count, boolean add) {
        super("minecraft:set_count");
        this.count = count;
        this.add = add;
    }


    JsonObject serialize() {
        JsonObject root = super.serialize();
        root.add("count", new JsonPrimitive(count));
        root.add("add", new JsonPrimitive(add));
        return root;
    }
}

class EntryCondition {
    private final String condition;
    private final EntryPredicate predicate;

    EntryCondition(String condition, EntryPredicate predicate) {
        this.condition = condition;
        this.predicate = predicate;
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.add("condition", new JsonPrimitive(condition));
        if (predicate != null) {
            JsonObject obj = new JsonObject();
            predicate.serialize(obj);
            root.add("predicate", obj);
        }
        return root;
    }
}

class MatchToolCondition extends EntryCondition {
    MatchToolCondition(EntryPredicate p) {
        super("minecraft:match_tool", p);
    }
}

class SurviveExplosionCondition extends EntryCondition {
    SurviveExplosionCondition() {
        super("minecraft:survives_explosion", null);
    }
}

class EntryPredicate {
    void serialize(JsonObject obj) {

    }
}

class EnchantmentPredicate extends EntryPredicate {
    private final List<Enchantment> enchantments;

    EnchantmentPredicate() {
        this.enchantments = new ArrayList<>(1);
    }

    void addEnchantment(Enchantment e) {
        enchantments.add(e);
    }

    void serialize(JsonObject obj) {
        super.serialize(obj);
        JsonArray root = new JsonArray();
        enchantments.stream().map(Enchantment::serialize).forEach(root::add);


        obj.add("enchantments", root);
    }
}

class Enchantment {
    private final String enchantment;
    private final int minLevel;

    Enchantment(String enchantment, int minLevel) {
        this.enchantment = enchantment;
        this.minLevel = minLevel;
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.add("enchantment", new JsonPrimitive(enchantment));
        JsonObject levels = new JsonObject();
        if (minLevel >= 0) levels.add("min", new JsonPrimitive(minLevel));
        root.add("levels", levels);
        return root;
    }
}

class AlternativeEntries extends Entry implements EntryList {
    private final List<Entry> children;

    AlternativeEntries() {
        super("minecraft:alternatives", null);
        children = new ArrayList<>(2);
    }

    public void addEntry(Entry c) {
        children.add(c);
    }

    JsonObject serialize() {
        JsonObject root = super.serialize();

        if (children.size() > 0) {
            JsonArray f = new JsonArray();
            children.stream().map(Entry::serialize).forEach(f::add);
            root.add("children", f);
        }
        return root;
    }
}

class Entry {
    private final String type;
    private final String name;
    private final List<EntryFunction> functions = new ArrayList<>(0);
    private final List<EntryCondition> conditions = new ArrayList<>(0);

    Entry(String type, String name) {
        this.type = type;
        this.name = name;
    }

    void addFunction(EntryFunction f) {
        functions.add(f);
    }

    void addCondition(EntryCondition f) {
        conditions.add(f);
    }


    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.add("type", new JsonPrimitive(type));
        if (functions.size() > 0) {
            JsonArray f = new JsonArray();
            functions.stream().map(EntryFunction::serialize).forEach(f::add);
            root.add("functions", f);
        }
        if (conditions.size() > 0) {
            JsonArray f = new JsonArray();
            conditions.stream().map(EntryCondition::serialize).forEach(f::add);
            root.add("conditions", f);
        }
        if (name != null) {
            root.add("name", new JsonPrimitive(name));
        }
        return root;
    }
}

class EntryPool implements EntryList {
    private final double rolls;
    private final double bonusRolls;
    private final List<Entry> entries;
    List<EntryCondition> conditions = new ArrayList<>(0);

    EntryPool(double rolls, double bonusRolls) {
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
        entries = new ArrayList<>(0);
    }

    public void addEntry(Entry e) {
        entries.add(e);
    }

    void addCondition(EntryCondition c) {
        conditions.add(c);
    }

    JsonObject serialize() {
        JsonObject root = new JsonObject();
        root.add("rolls", new JsonPrimitive(rolls));
        root.add("bonus_rolls", new JsonPrimitive(bonusRolls));
        if (entries.size() > 0) {
            JsonArray f = new JsonArray();
            entries.stream().map(Entry::serialize).forEach(f::add);
            root.add("entries", f);
        }
        if (conditions.size() > 0) {
            JsonArray f = new JsonArray();
            conditions.stream().map(EntryCondition::serialize).forEach(f::add);
            root.add("conditions", f);
        }
        return root;
    }
}