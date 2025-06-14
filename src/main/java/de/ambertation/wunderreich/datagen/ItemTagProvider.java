package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.interfaces.BlockTagSupplier;
import de.ambertation.wunderreich.interfaces.ItemTagSupplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends FabricTagProvider<Item> {
    public ItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, BuiltInRegistries.ITEM.key(), registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var registry = provider.lookup(BuiltInRegistries.ITEM.key()).orElseThrow();
        var blockRegistry = provider.lookup(BuiltInRegistries.BLOCK.key()).orElseThrow();
        registry.listElements().forEach(itemRef -> {
            final Item item = itemRef.value();
            if (item instanceof ItemTagSupplier supl) {
                supl.supplyTags(
                        (tag) -> builder(tag).addOptional(itemRef.unwrapKey().orElseThrow())
                );
            }
        });

        blockRegistry.listElements().forEach(blockRef -> {
            final Block block = blockRef.value();
            if (block instanceof BlockTagSupplier supl) {
                Item itm = block.asItem();
                registry.listElements().forEach(itemRef -> {
                    if (itemRef.value() == itm) {
                        supl.supplyTags(
                                tag -> {
                                },
                                (tag) -> builder(tag).addOptional(itemRef.unwrapKey().orElseThrow())
                        );
                    }
                });

            }
        });
    }
}
