package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.stream.Stream;

public class BlockModelProvider extends FabricModelProvider {
    public BlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    public void acceptBlockState(
            BlockModelGenerators vanillaGenerator,
            BlockModelDefinitionGenerator blockStateGenerator
    ) {
        vanillaGenerator.blockStateOutput.accept(blockStateGenerator);
    }

    public void acceptModelOutput(BlockModelGenerators vanillaGenerator, ResourceLocation id, ModelInstance model) {
        vanillaGenerator.modelOutput.accept(id, model);
    }

    public void delegateItemModel(BlockModelGenerators vanillaGenerator, Block block) {
        vanillaGenerator.registerSimpleItemModel(block, TextureMapping.getBlockTexture(block));
    }

    public void delegateItemModel(
            BlockModelGenerators vanillaGenerator,
            Block block,
            ResourceLocation resourceLocation
    ) {
        vanillaGenerator.registerSimpleItemModel(block, resourceLocation);
    }


    public void createSlab(BlockModelGenerators vanillaGenerator, Block slabBlock, Block baseBlock) {
        var res = TextureMapping.getBlockTexture(baseBlock);
        createSlab(
                vanillaGenerator,
                slabBlock, baseBlock, new TextureMapping()
                        .put(TextureSlot.SIDE, res)
                        .put(TextureSlot.BOTTOM, res)
                        .put(TextureSlot.TOP, res)
                        .put(TextureSlot.PARTICLE, res)
        );
    }

    public void createSlab(
            BlockModelGenerators vanillaGenerator,
            Block slabBlock,
            Block baseBlock,
            TextureMapping mapping
    ) {
        final var fullBlockLocation = ModelLocationUtils.getModelLocation(baseBlock);
        final List<ResourceLocation> locations = Stream.of(
                ModelTemplates.SLAB_BOTTOM,
                ModelTemplates.SLAB_TOP
        ).map(template -> template.create(slabBlock, mapping, vanillaGenerator.modelOutput)).toList();

        acceptBlockState(
                vanillaGenerator,
                BlockModelGenerators.createSlab(
                        slabBlock,
                        BlockModelGenerators.plainVariant(locations.get(0)),
                        BlockModelGenerators.plainVariant(locations.get(1)),
                        BlockModelGenerators.plainVariant(fullBlockLocation)
                )
        );
        delegateItemModel(vanillaGenerator, slabBlock, locations.get(0));
    }

    public static BlockModelDefinitionGenerator createSlab(
            Block block,
            MultiVariant multiVariant,
            MultiVariant multiVariant2,
            MultiVariant multiVariant3
    ) {
        return MultiVariantGenerator.dispatch(block)
                                    .with(
                                            PropertyDispatch
                                                    .initial(BlockStateProperties.SLAB_TYPE)
                                                    .select(SlabType.BOTTOM, multiVariant)
                                                    .select(SlabType.TOP, multiVariant2)
                                                    .select(SlabType.DOUBLE, multiVariant3)
                                    );
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators vanillaGenerator) {
        for (Block[] slabBlock : WunderreichSlabBlocks.getSlabBlocks()) {
            createSlab(vanillaGenerator, slabBlock[0], slabBlock[1]);
        }

    }

    @Override
    public void generateItemModels(ItemModelGenerators vanillaGenerator) {

    }
}
