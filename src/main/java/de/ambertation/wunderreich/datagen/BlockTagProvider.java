package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.registries.WunderreichTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends FabricTagsProvider<Block> {
    public BlockTagProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, BuiltInRegistries.BLOCK.key(), registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(WunderreichTags.MINEABLE_TROWEL).addOptionalTag(BlockTags.MINEABLE_WITH_SHOVEL);
        var registry = provider.lookup(BuiltInRegistries.BLOCK.key()).orElseThrow();
        registry.listElements().forEach(blockRef -> {
            final Block block = blockRef.value();
            if (block instanceof de.ambertation.wunderreich.interfaces.BlockTagSupplier supl) {
                Item itm = block.asItem();
                supl.supplyTags(
                        (tag) -> builder(tag).addOptional(blockRef.unwrapKey().orElseThrow()),
                        (tag) -> {
                        }
                );
            }
        });
    }
}
