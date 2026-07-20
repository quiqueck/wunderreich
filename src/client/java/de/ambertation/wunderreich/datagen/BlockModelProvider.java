package de.ambertation.wunderreich.datagen;

import de.ambertation.wunderreich.datagen.modelBuilder.SlabBuilder;
import de.ambertation.wunderreich.datagen.modelBuilder.StairBuilder;
import de.ambertation.wunderreich.datagen.modelBuilder.WallBuilder;
import de.ambertation.wunderreich.interfaces.ChangeRenderLayer;
import de.ambertation.wunderreich.registries.WunderreichSlabBlocks;
import de.ambertation.wunderreich.registries.WunderreichStairBlocks;
import de.ambertation.wunderreich.registries.WunderreichWallBlocks;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.BiConsumer;

public class BlockModelProvider extends FabricModelProvider {
    public BlockModelProvider(FabricPackOutput output) {
        super(output);
    }


    public static TexturedModel getTextureModels(Block block, TexturedModel defaultModel) {
        return BlockModelGenerators.TEXTURED_MODELS.getOrDefault(block, defaultModel);
    }

    /**
     * 26.1 removed Fabric's BlockRenderLayerMap, so a block's render layer must live in its model JSON
     * ("render_type"). This wraps the vanilla model output and injects the right render_type for any block
     * that still declares one via {@link ChangeRenderLayer}, keeping the block class as the single source of truth.
     */
    public static BiConsumer<Identifier, ModelInstance> modelOutputFor(BlockModelGenerators vanillaGenerator, Block block) {
        final String renderType = renderTypeString(block);
        if (renderType == null) return vanillaGenerator.modelOutput;
        return (id, instance) -> vanillaGenerator.modelOutput.accept(id, () -> {
            JsonElement el = instance.get();
            if (el instanceof JsonObject obj && !obj.has("render_type")) {
                obj.addProperty("render_type", renderType);
            }
            return el;
        });
    }

    private static String renderTypeString(Block block) {
        if (block instanceof ChangeRenderLayer crl) {
            ChangeRenderLayer.RenderLayer layer = crl.getRenderType();
            if (layer == ChangeRenderLayer.RenderLayer.CUTOUT) return "minecraft:cutout";
            if (layer == ChangeRenderLayer.RenderLayer.TRANSLUCENT) return "minecraft:translucent";
        }
        return null;
    }

    public static void createInventoryModel(
            BlockModelGenerators vanillaGenerator,
            Block wallBlock,
            ModelTemplate inventoryModel,
            TextureMapping mapping
    ) {
        delegateItemModel(
                vanillaGenerator,
                wallBlock,
                inventoryModel.create(wallBlock, mapping, modelOutputFor(vanillaGenerator, wallBlock))
        );
    }

    public static void acceptBlockState(
            BlockModelGenerators vanillaGenerator,
            BlockModelDefinitionGenerator blockStateGenerator
    ) {
        vanillaGenerator.blockStateOutput.accept(blockStateGenerator);
    }

    public static void delegateItemModel(
            BlockModelGenerators vanillaGenerator,
            Block block,
            Identifier resourceLocation
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

        for (Block[] slabBlock : WunderreichStairBlocks.getStairBlocks()) {
            StairBuilder.createStairs(
                    vanillaGenerator,
                    slabBlock[1],
                    slabBlock[0]
            );
        }

        for (Block[] slabBlock : WunderreichWallBlocks.getWallBlocks()) {
            WallBuilder.createWall(
                    vanillaGenerator,
                    slabBlock[1],
                    slabBlock[0]
            );
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators vanillaGenerator) {

    }
}
