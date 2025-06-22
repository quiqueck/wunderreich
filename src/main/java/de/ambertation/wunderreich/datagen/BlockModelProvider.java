package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.datagen.modelBuilder.SlabBuilder;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class BlockModelProvider extends FabricModelProvider {
    public BlockModelProvider(FabricDataOutput output) {
        super(output);
    }


    public static void acceptBlockState(
            BlockModelGenerators vanillaGenerator,
            BlockModelDefinitionGenerator blockStateGenerator
    ) {
        vanillaGenerator.blockStateOutput.accept(blockStateGenerator);
    }

    public static void acceptModelOutput(
            BlockModelGenerators vanillaGenerator,
            ResourceLocation id,
            ModelInstance model
    ) {
        vanillaGenerator.modelOutput.accept(id, model);
    }

    public static void delegateItemModel(BlockModelGenerators vanillaGenerator, Block block) {
        vanillaGenerator.registerSimpleItemModel(block, TextureMapping.getBlockTexture(block));
    }

    public static void delegateItemModel(
            BlockModelGenerators vanillaGenerator,
            Block block,
            ResourceLocation resourceLocation
    ) {
        vanillaGenerator.registerSimpleItemModel(block, resourceLocation);
    }


    @Override
    public void generateBlockStateModels(BlockModelGenerators vanillaGenerator) {
        for (Block[] slabBlock : WunderreichSlabBlocks.getSlabBlocks()) {
            if (slabBlock[0] == WunderreichSlabBlocks.DIRT_PATH_SLAB) {
                SlabBuilder.createPathSlab(vanillaGenerator, slabBlock[0], slabBlock[1], Blocks.DIRT);
            } else if (slabBlock[0] == WunderreichSlabBlocks.GRASS_SLAB) {
                SlabBuilder.createGrassSlab(vanillaGenerator, slabBlock[0], slabBlock[1], Blocks.DIRT);
            } else {
                SlabBuilder.createSlab(vanillaGenerator, slabBlock[0], slabBlock[1]);
            }
        }

    }

    @Override
    public void generateItemModels(ItemModelGenerators vanillaGenerator) {

    }
}
