package de.ambertation.wunderreich.datagen.modelBuilder;

import de.ambertation.wunderreich.Wunderreich;
import de.ambertation.wunderreich.datagen.BlockModelProvider;

import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SlabBuilder {
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

    private static final ModelTemplate GRASS_SLAB_BOTTOM = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/layered_slab")),
            Optional.empty(),
            TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.LAYER0
    );

    private static final ModelTemplate GRASS_SLAB_TOP = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/layered_slab_top")),
            Optional.of("_top"),
            TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.LAYER0
    );

    private static final ModelTemplate GRASS_SLAB_SNOW_BOTTOM = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/layered_slab_snow")),
            Optional.empty(),
            TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE
    );

    private static final ModelTemplate GRASS_SLAB_SNOW_TOP = new ModelTemplate(
            Optional.of(Wunderreich.ID("template/layered_slab_top_snow")),
            Optional.of("_top"),
            TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE
    );

    public static void createSlab(BlockModelGenerators vanillaGenerator, Block slabBlock, Block baseBlock) {
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

    public static void createPathSlab(
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
                        .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(baseBlock, "_side"))
                        .put(TextureSlot.BOTTOM, res)
                        .put(TextureSlot.TOP, TextureMapping.getBlockTexture(baseBlock, "_top"))
                        .put(TextureSlot.PARTICLE, res),
                Stream.of(PATH_SLAB_BOTTOM, PATH_SLAB_TOP)
        );
    }

    private static void createSlab(
            BlockModelGenerators vanillaGenerator,
            Block slabBlock,
            Block baseBlock,
            TextureMapping mapping,
            Stream<ModelTemplate> models
    ) {
        final var fullBlockLocation = ModelLocationUtils.getModelLocation(baseBlock);
        final var modelOutput = BlockModelProvider.modelOutputFor(vanillaGenerator, slabBlock);
        final List<Identifier> locations = models.map(template -> template.create(
                slabBlock,
                mapping,
                modelOutput
        )).toList();

        BlockModelProvider.acceptBlockState(
                vanillaGenerator,
                BlockModelGenerators.createSlab(
                        slabBlock,
                        BlockModelGenerators.plainVariant(locations.get(0)),
                        BlockModelGenerators.plainVariant(locations.get(1)),
                        BlockModelGenerators.plainVariant(fullBlockLocation)
                )
        );
        BlockModelProvider.delegateItemModel(vanillaGenerator, slabBlock, locations.get(0));
    }

    public static void createGrassSlab(
            BlockModelGenerators vanillaGenerator,
            Block slabBlock,
            Block baseBlock,
            Block topBlock
    ) {
        var topRes = TextureMapping.getBlockTexture(baseBlock);
        var res = TextureMapping.getBlockTexture(topBlock);


        TextureMapping mappingSnow = new TextureMapping()
                .put(TextureSlot.BOTTOM, res)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SNOW))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));

        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(baseBlock, "_side"))
                .put(TextureSlot.BOTTOM, res)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(baseBlock, "_top"))
                .put(TextureSlot.LAYER0, TextureMapping.getBlockTexture(baseBlock, "_side_overlay"));
        final var fullBlockLocation = ModelLocationUtils.getModelLocation(baseBlock);
        final var snowBlockLocation = fullBlockLocation.withSuffix("_snow");
        final var modelOutput = BlockModelProvider.modelOutputFor(vanillaGenerator, slabBlock);
        Identifier BOTTOM = GRASS_SLAB_BOTTOM.create(slabBlock, mapping, modelOutput);
        Identifier TOP = GRASS_SLAB_TOP.create(slabBlock, mapping, modelOutput);
        Identifier BOTTOM_SNOW = GRASS_SLAB_SNOW_BOTTOM.createWithSuffix(
                slabBlock,
                "_snow",
                mappingSnow,
                modelOutput
        );
        Identifier TOP_SNOW = GRASS_SLAB_SNOW_TOP.createWithSuffix(
                slabBlock,
                "_snow",
                mappingSnow,
                modelOutput
        );

        BlockModelProvider.acceptBlockState(
                vanillaGenerator,
                MultiVariantGenerator
                        .dispatch(slabBlock)
                        .with(PropertyDispatch
                                .initial(BlockStateProperties.SLAB_TYPE, BlockStateProperties.SNOWY)

                                .select(
                                        SlabType.BOTTOM,
                                        true,
                                        BlockModelGenerators.plainVariant(BOTTOM_SNOW)
                                )
                                .select(
                                        SlabType.TOP,
                                        true,
                                        BlockModelGenerators.plainVariant(TOP_SNOW)
                                )
                                .select(
                                        SlabType.DOUBLE,
                                        true,
                                        BlockModelGenerators.plainVariant(snowBlockLocation)
                                )
                                .select(
                                        SlabType.BOTTOM,
                                        false,
                                        BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(
                                                BOTTOM))
                                )
                                .select(
                                        SlabType.TOP,
                                        false,
                                        BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(TOP))
                                )
                                .select(
                                        SlabType.DOUBLE,
                                        false,
                                        BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(
                                                fullBlockLocation))
                                )
                        )
        );

        vanillaGenerator.registerSimpleTintedItemModel(slabBlock, BOTTOM, new GrassColorSource());
    }


}
