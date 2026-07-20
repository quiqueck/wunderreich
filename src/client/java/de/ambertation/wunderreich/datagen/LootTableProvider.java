package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.loot.LootTableHelper;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends FabricBlockLootTableProvider {
    protected LootTableProvider(
            FabricDataOutput dataOutput,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        LootTableHelper.provideAllBlocks(new LootTableHelper.BlockLootProvider(this));
    }
}
