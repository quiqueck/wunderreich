package de.ambertation.wunderreich.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class WunderreichDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(LootTableProvider::new);
    }

}