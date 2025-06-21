package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.Wunderreich;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockModelProvider extends FabricModelProvider {
    public BlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    // Custom model templates for reduced-height slabs (like dirt path)
    private static final ModelTemplate PATH_SLAB_BOTTOM = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/path_slab")),
            Optional.empty(),
            TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE
    );

    private static final ModelTemplate PATH_SLAB_TOP = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/path_slab_top")),
            Optional.of("_top"),
            TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE
    );

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
                        .put(TextureSlot.PARTICLE, res),
                Stream.of(ModelTemplates.SLAB_BOTTOM, ModelTemplates.SLAB_TOP)
        );
    }

    public void createPathSlab(
            BlockModelGenerators vanillaGenerator,
            Block slabBlock,
            Block baseBlock,
            Block topBlock
    ) {
        var topRes = TextureMapping.getBlockTexture(baseBlock);
        var res = TextureMapping.getBlockTexture(topBlock);
        createSlab(
                vanillaGenerator,
                slabBlock, baseBlock, new TextureMapping()
                        .put(TextureSlot.SIDE, topRes.withSuffix("_side"))
                        .put(TextureSlot.BOTTOM, res)
                        .put(TextureSlot.TOP, topRes.withSuffix("_top"))
                        .put(TextureSlot.PARTICLE, res),
                Stream.of(PATH_SLAB_BOTTOM, PATH_SLAB_TOP)
        );
    }

    private void createSlab(
            BlockModelGenerators vanillaGenerator,
            Block slabBlock,
            Block baseBlock,
            TextureMapping mapping,
            Stream<ModelTemplate> models
    ) {
        final var fullBlockLocation = ModelLocationUtils.getModelLocation(baseBlock);
        final List<ResourceLocation> locations = models.map(template -> template.create(
                slabBlock,
                mapping,
                vanillaGenerator.modelOutput
        )).toList();

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
            if (slabBlock[0] == WunderreichSlabBlocks.DIRT_PATH_SLAB) {
                createPathSlab(vanillaGenerator, slabBlock[0], slabBlock[1], Blocks.DIRT);
            } else {
                createSlab(vanillaGenerator, slabBlock[0], slabBlock[1]);
            }
        }

    }

    @Override
    public void generateItemModels(ItemModelGenerators vanillaGenerator) {

    }
}
