package de.ambertation.wunderreich.loot;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.config.WunderreichConfigs;
import de.ambertation.wunderreich.interfaces.CanDropLoot;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LootTableJsonBuilder {
    public static Stream<Helper> getAllBlocks() {
        return WunderreichBlocks
                .getAllBlocks()
                .stream()
                .filter(bl -> bl instanceof CanDropLoot)
                .filter(WunderreichConfigs.BLOCK_CONFIG::isEnabled)
                .map(bl -> {
                            LootTableJsonBuilder l = ((CanDropLoot) bl).buildLootTable();
                            return new Helper(l.ID, l::build);
                        }
                );
    }

    public record Helper(ResourceLocation id, Supplier<JsonElement> json) {
    }

    public static class EntryBuilder<P extends EntryListBuilder> {
        private final LootTableJsonBuilder base;
        private final P builder;
        private final Entry entry;

        public EntryBuilder(LootTableJsonBuilder base, P builder, Entry entry) {
            this.base = base;
            this.builder = builder;
            this.entry = entry;
        }

        public P finishEntry() {
            return builder;
        }

        public EntryBuilder<P> explosionDecay(){
            entry.addFunction(new ExplosionDecayFunction());
            return this;
        }

        public EntryBuilder<P> setCount(int count, boolean add){
            entry.addFunction(new SetCountFunction(count, add));
            return this;
        }

        public EntryBuilder<P> silkTouch(){
            return this.enchantedTool(Enchantments.SILK_TOUCH, 1);
        }

        public EntryBuilder<P> enchantedTool(Enchantment e, int minLevel){
            var id = Registry.ENCHANTMENT.getKey(e);
            if (id!=null) {
                var predicate = new EnchantmentPredicate();
                predicate.addEnchantment(new de.ambertation.wunderreich.loot.Enchantment(id.toString(), minLevel));
                entry.addCondition(new MatchToolCondition(predicate));
            } else {
                Wunderreich.LOGGER.warn("Unknown Enchantment '{}' in Loot Table '{}'", e.toString(), base.ID);
            }
            return this;
        }
    }

    public abstract static class EntryListBuilder<T extends de.ambertation.wunderreich.loot.EntryList, ME extends EntryListBuilder> {
        protected final LootTableJsonBuilder base;
        protected final T container;

        public EntryListBuilder(LootTableJsonBuilder base, T container) {
            this.base = base;
            this.container = container;
        }

        public ME addSelfEntry() {
            return startSelfEntry((b)->{});
        }

        public ME startSelfEntry(Consumer<EntryBuilder<ME>> builder) {
            return startEntry("minecraft:item", base.sourceID.toString(), builder);
        }

        public ME startItemEntry(Item itm, Consumer<EntryBuilder<ME>> builder) {
            ResourceLocation id = Registry.ITEM.getKey(itm);
            return startEntry("minecraft:item",  id!=null?id.toString():"", builder);
        }

        public ME startItemEntry(String name, Consumer<EntryBuilder<ME>> builder) {
            return startEntry("minecraft:item", name, builder);
        }

        protected abstract EntryBuilder<ME> makeEntryBuilder(Entry e);

        public ME startEntry(String type, String name, Consumer<EntryBuilder<ME>> builder) {
            Entry e = new Entry(type, name);
            this.container.addEntry(e);
            builder.accept(makeEntryBuilder(e));
            return (ME)this;
        }
    }

    public static final class AlternativeEntryBuilder extends EntryListBuilder<AlternativeEntries, AlternativeEntryBuilder> {
        public AlternativeEntryBuilder(LootTableJsonBuilder base, AlternativeEntries alt) {
            super(base, alt);
        }

        @Override
        protected EntryBuilder makeEntryBuilder(Entry e) {
            return new EntryBuilder(base, this, e);
        }
    }

    public static final class PoolBuilder extends EntryListBuilder<EntryPool, PoolBuilder> {
        public PoolBuilder(LootTableJsonBuilder base, EntryPool pool) {
            super(base, pool);
        }

        @Override
        protected EntryBuilder makeEntryBuilder(Entry e) {
            return new EntryBuilder(base, this, e);
        }

        public PoolBuilder survivesExplosion() {
            container.conditions.add(new SurviveExplosionCondition());
            return this;
        }

        public PoolBuilder startAlternatives(Consumer<AlternativeEntryBuilder> builder) {
            AlternativeEntries alt = new AlternativeEntries();
            AlternativeEntryBuilder res = new AlternativeEntryBuilder(base, alt);
            container.addEntry(alt);
            builder.accept(res);
            return this;
        }
    }

    public final ResourceLocation ID;
    public final ResourceLocation sourceID;
    private final LootTypes type;
    private final List<EntryPool> pools = new ArrayList<>(1);

    public enum LootTypes {
        UNKNOWN,
        BLOCK
    }

    private LootTableJsonBuilder(ResourceLocation sourceID, LootTypes type) {
        this.sourceID = sourceID;
        if (type == LootTypes.BLOCK) {
            this.ID = new ResourceLocation(sourceID.getNamespace(), "blocks/" + sourceID.getPath());
        } else {
            this.ID = sourceID;
        }

        this.type = type;
    }

    public static LootTableJsonBuilder create(Block bl) {
        return new LootTableJsonBuilder(Registry.BLOCK.getKey(bl), LootTypes.BLOCK);

    }

    public static LootTableJsonBuilder create(String name, LootTypes type) {
        return new LootTableJsonBuilder(Wunderreich.ID(name), type);
    }

    public LootTableJsonBuilder dropSelf() {
        return this.startPool(
                1,
                0,
                builder -> builder
                        .survivesExplosion()
                        .addSelfEntry()
        );
    }

    public LootTableJsonBuilder startPool(double rolls, double bonusRolls, Consumer<PoolBuilder> builder) {
        PoolBuilder res = new PoolBuilder(this, new EntryPool(rolls, bonusRolls));
        pools.add(res.container);
        builder.accept(res);
        return this;
    }

    protected String getLootType() {
        if (type == LootTypes.BLOCK) {
            return "minecraft:block";
        }
        return null;
    }

    public JsonElement build() {
        final String tt = getLootType();

        if (tt == null) {
            throw new IllegalStateException("A LootTable needs a Type (" + ID + ")");
        }

        JsonObject root = new JsonObject();

        root.add("type", new JsonPrimitive(tt));

        JsonArray f = new JsonArray();
        pools.stream().map(EntryPool::serialize).forEach(f::add);
        root.add("pools", f);

        return root;
    }
}
