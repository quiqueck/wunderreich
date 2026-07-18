package de.ambertation.wunderreich.datagen.modelBuilder;

import de.ambertation.wunderreich.datagen.BlockModelProvider;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

public class WallBuilder {
    public static void createWall(BlockModelGenerators vanillaGenerator, Block materialBlock, Block wallBlock) {
        createWall(
                vanillaGenerator,
                wallBlock, BlockModelProvider
                        .getTextureModels(wallBlock, TexturedModel.CUBE.get(materialBlock))
                        .getMapping()
        );
    }

    public static void createWall(BlockModelGenerators vanillaGenerator, Block wallBlock, TextureMapping mapping) {
        final List<Identifier> locations = Stream.of(
                ModelTemplates.WALL_POST,
                ModelTemplates.WALL_LOW_SIDE,
                ModelTemplates.WALL_TALL_SIDE
        ).map(template -> template.create(wallBlock, mapping, BlockModelProvider.modelOutputFor(vanillaGenerator, wallBlock))).toList();

        BlockModelProvider.acceptBlockState(
                vanillaGenerator, BlockModelGenerators.createWall(
                        wallBlock,
                        BlockModelGenerators.plainVariant(locations.get(0)),
                        BlockModelGenerators.plainVariant(locations.get(1)),
                        BlockModelGenerators.plainVariant(locations.get(2))
                )
        );
        BlockModelProvider.createInventoryModel(vanillaGenerator, wallBlock, ModelTemplates.WALL_INVENTORY, mapping);
    }
}
