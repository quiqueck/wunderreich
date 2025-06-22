package de.ambertation.wunderreich.datagen.modelBuilder;

import de.ambertation.wunderreich.datagen.BlockModelProvider;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

public class StairBuilder {
    public static void createStairs(
            BlockModelGenerators vanillaGenerator,
            Block materialBlock,
            Block stairBlock
    ) {
        createStairs(
                vanillaGenerator,
                stairBlock, BlockModelProvider
                        .getTextureModels(stairBlock, TexturedModel.CUBE.get(materialBlock))
                        .getMapping()
        );
    }

    public static void createStairs(
            BlockModelGenerators vanillaGenerator, Block stairBlock,
            ResourceLocation topTextureLocation,
            ResourceLocation sideTextureLocation,
            ResourceLocation bottomTextureLocation
    ) {
        createStairs(
                vanillaGenerator,
                stairBlock, new TextureMapping()
                        .put(TextureSlot.TOP, topTextureLocation)
                        .put(TextureSlot.SIDE, sideTextureLocation)
                        .put(TextureSlot.BOTTOM, bottomTextureLocation)
        );
    }


    public static void createStairsWithModels(
            BlockModelGenerators vanillaGenerator, Block stairBlock,
            ResourceLocation stair,
            ResourceLocation outer,
            ResourceLocation inner
    ) {
        BlockModelProvider.acceptBlockState(
                vanillaGenerator, BlockModelGenerators.createStairs(
                        stairBlock,
                        BlockModelGenerators.plainVariant(inner),
                        BlockModelGenerators.plainVariant(stair),
                        BlockModelGenerators.plainVariant(outer)
                )
        );
        BlockModelProvider.delegateItemModel(vanillaGenerator, stairBlock, stair);
    }

    public static void createStairs(BlockModelGenerators vanillaGenerator, Block stairBlock, TextureMapping mapping) {
        final List<ResourceLocation> locations = Stream
                .of(
                        ModelTemplates.STAIRS_INNER,
                        ModelTemplates.STAIRS_STRAIGHT,
                        ModelTemplates.STAIRS_OUTER
                )
                .map(template -> template.create(stairBlock, mapping, vanillaGenerator.modelOutput)).toList();

        BlockModelProvider.acceptBlockState(
                vanillaGenerator, BlockModelGenerators.createStairs(
                        stairBlock,
                        BlockModelGenerators.plainVariant(locations.get(0)),
                        BlockModelGenerators.plainVariant(locations.get(1)),
                        BlockModelGenerators.plainVariant(locations.get(2))
                )
        );
        BlockModelProvider.delegateItemModel(vanillaGenerator, stairBlock, locations.get(1));
    }
}
