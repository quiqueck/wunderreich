package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.loot.LootTableHelper;
import de.ambertation.wunderreich.registries.WunderreichBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LootTableProvider extends FabricBlockLootSubProvider {
    private LootTableHelper.BlockLootProvider lootProvider;

    protected LootTableProvider(
            FabricPackOutput dataOutput,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        lootProvider = new LootTableHelper.BlockLootProvider(this);
        LootTableHelper.provideAllBlocks(lootProvider);
    }

    /**
     * Every block (and item) of Wunderreich can be disabled in the config, and a disabled one is not registered
     * at all. Since 26.3 loot tables are a datapack registry, and a single loot table that refers to an unknown
     * block or item makes the whole registry load (and with it the server start) fail. So each loot table is
     * only loaded when its block and every Wunderreich block/item it drops are registered.
     */
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
        final Map<ResourceKey<LootTable>, ResourceCondition[]> conditions = new HashMap<>();
        super.generate((key, builder) -> {
            if (conditions.isEmpty()) collectConditions(conditions);
            final ResourceCondition[] tableConditions = conditions.get(key);
            if (tableConditions == null) exporter.accept(key, builder);
            else withConditions(exporter, tableConditions).accept(key, builder);
        });
    }

    private void collectConditions(Map<ResourceKey<LootTable>, ResourceCondition[]> conditions) {
        for (Block block : WunderreichBlocks.getAllBlocks()) {
            block.getLootTable().ifPresent(key -> conditions.put(key, conditionsFor(block)));
        }
    }

    private ResourceCondition[] conditionsFor(Block block) {
        final Set<Identifier> blocks = new LinkedHashSet<>();
        final Set<Identifier> items = new LinkedHashSet<>();
        blocks.add(BuiltInRegistries.BLOCK.getKey(block));
        addOwnItem(items, block);
        for (ItemLike reference : lootProvider.referencesOf(block)) {
            if (reference instanceof Block referencedBlock) {
                final Identifier id = BuiltInRegistries.BLOCK.getKey(referencedBlock);
                if (Wunderreich.MOD_ID.equals(id.getNamespace())) blocks.add(id);
            }
            addOwnItem(items, reference);
        }

        final List<ResourceCondition> result = new ArrayList<>(2);
        result.add(ResourceConditions.registryContains(Registries.BLOCK, blocks.toArray(Identifier[]::new)));
        if (!items.isEmpty()) {
            result.add(ResourceConditions.registryContains(Registries.ITEM, items.toArray(Identifier[]::new)));
        }
        return result.toArray(ResourceCondition[]::new);
    }

    private static void addOwnItem(Set<Identifier> items, ItemLike itemLike) {
        final Item item = itemLike.asItem();
        if (item == null || item == Items.AIR) return;
        final Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (Wunderreich.MOD_ID.equals(id.getNamespace())) items.add(id);
    }
}
