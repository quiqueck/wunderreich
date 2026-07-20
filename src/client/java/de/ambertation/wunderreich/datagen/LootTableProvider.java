package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.loot.LootTableHelper;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;

import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends FabricBlockLootSubProvider {
    protected LootTableProvider(
            FabricPackOutput dataOutput,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        LootTableHelper.provideAllBlocks(new LootTableHelper.BlockLootProvider(this));
    }
}
